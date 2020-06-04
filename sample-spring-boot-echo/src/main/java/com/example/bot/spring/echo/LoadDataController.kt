package com.example.bot.spring.echo

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import java.util.concurrent.Semaphore

@RestController
@RequestMapping("/gApi")
class LoadDataController : VerificationCodeReceiver {

    companion object {
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

        private const val TOKENS_DIRECTORY_PATH = "tokens"

        private val logger = LoggerFactory.getLogger(LoadDataController::class.java)
    }

    @Autowired
    private lateinit var cfg: Config

    private var code: String? = null

    var waitUnlessSignaled: Semaphore? = null

    override fun waitForCode(): String {
        waitUnlessSignaled!!.acquireUninterruptibly()
        return code!!
    }

    override fun stop() {
        waitUnlessSignaled?.release()
    }

    override fun getRedirectUri(): String {
        if (waitUnlessSignaled == null) {
            waitUnlessSignaled = Semaphore(0)
        } else {
            throw Exception("Concurrent google API authentication")
        }
        return cfg.callbackUrl
    }

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val `in` = EchoApplication::class.java.getResourceAsStream(cfg.credentialsFilePath)
            ?: throw FileNotFoundException("Resource not found: $${cfg.credentialsFilePath}")
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build()
        return AuthorizationCodeInstalledApp(flow, this).authorize("user")
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam("code", required = false) code: String?,
        @RequestParam("error", required = false) error: String?,
        @RequestParam("scope", required = false) scope: String?
    ): ResponseEntity<*> {
        if (!error.isNullOrBlank()) {
            logger.error(error)
            return ResponseEntity<Any?>("OAuth token not received: error = $error", HttpStatus.OK)
        }
        logger.info("OAuth callback with code=$code and error=$error")
        waitUnlessSignaled?.release()
        this.code = code
        return ResponseEntity<Any?>("OAuth token received successfully", HttpStatus.OK)
    }

    @GetMapping("/loadData")
    @Throws(IOException::class, GeneralSecurityException::class)
    fun loadData(): String {
        logger.info("Starting loadData")
        var errorCount = 0
        try {
            // Build a new authorized API client service.
            val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(cfg.applicationName)
                .build()
            val response = service.spreadsheets().get(cfg.spreadsheetId).execute()
            EchoApplication.questionAndAnswerSets = response.sheets.map { sheet ->
                val range = "${sheet.properties.title}!A2:C"
                val valueRange = service.spreadsheets().values().get(cfg.spreadsheetId, range).execute()
                QuestionAndAnswerSet().also {
                    it.name = sheet.properties.title
                    it.questionAndAnswers = valueRange.getValues()?.mapNotNull { row ->
                        row as List<*>
                        try {
                            if (row.size < 3 || (0..2).any { row[it] == null || (row[it] as? String)?.isBlank() == true }) {
                                return@mapNotNull null
                            }
                            QuestionAndAnswer().also {
                                it.code = row[0].toString()
                                it.question = row[1].toString()
                                it.answer = row[2].toString()
                            }
                        } catch (t: Throwable) {
                            errorCount++
                            t.printStackTrace()
                            null
                        }
                    } ?: listOf()
                }
            }
            val message = "Data loaded success with ${errorCount} errors. Number of question sets = ${EchoApplication.questionAndAnswerSets.size}. Total number of questions = ${EchoApplication.questionAndAnswerSets.sumBy { it.questionAndAnswers.size }}"
            logger.info(message)
            return message
        } catch (t: Throwable) {
            t.printStackTrace()
            val message = t.message ?: t.toString()
            logger.error(message)
            return "Failed: $message"
        }
    }
}
