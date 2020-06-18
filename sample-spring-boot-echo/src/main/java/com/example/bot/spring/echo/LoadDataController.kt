package com.example.bot.spring.echo

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.auth.oauth2.TokenResponse
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/gApi")
class LoadDataController {

    companion object {
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()

        private const val TOKENS_DIRECTORY_PATH = "tokens"

        private val logger = LoggerFactory.getLogger(LoadDataController::class.java)
    }

    @Autowired
    private lateinit var cfg: Config

    private var code: String? = null

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)

    fun getGoogleAuthorizationFlow(HTTP_TRANSPORT: NetHttpTransport): GoogleAuthorizationCodeFlow {
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

        return flow
    }

    @GetMapping("/callback")
    fun callback(
        @RequestParam("code", required = false) code: String?,
        @RequestParam("error", required = false) error: String?,
        @RequestParam("scope", required = false) scope: String?,
        response: HttpServletResponse
    ): Any {
        if (!error.isNullOrBlank()) {
            logger.error(error)
            return ResponseEntity<Any?>("OAuth token not received: error = $error", HttpStatus.OK)
        }
        logger.info("OAuth callback with code=$code and error=$error")
        this.code = code

        // Build a new authorized API client service.
        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

        val flow = getGoogleAuthorizationFlow(HTTP_TRANSPORT)

        val tokenResponse: TokenResponse = flow.newTokenRequest(code).setRedirectUri(cfg.callbackUrl).execute()
        // store credential and return it
        flow.createAndStoreCredential(tokenResponse, "user")

        response.sendRedirect("loadData")

        return ResponseEntity<Any?>(HttpStatus.OK)
    }

    @GetMapping("/loadData")
    @Throws(IOException::class, GeneralSecurityException::class)
    fun loadData(
        @RequestParam("isOnInit", required = false, defaultValue = "false") isOnInit: Boolean = false,
        response: HttpServletResponse? = null
    ): Any? {

        logger.info("Starting loadData")
        var errorCount = 0
        try {
            // Build a new authorized API client service.
            val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()

            val flow = getGoogleAuthorizationFlow(HTTP_TRANSPORT)

            val credential: Credential? = flow.loadCredential("user")

            val credentialValid = credential != null
                && (credential.refreshToken != null || credential.expiresInSeconds == null || credential.expiresInSeconds > 60)

            if (!credentialValid) {
                if (isOnInit) {
                    logger.info("Application try to load data from google spreadsheet on startup but the credential is invalid so it will load data when there's request to /gApi/loadData")
                    return null
                }
                val authorizationUrl: String = flow.newAuthorizationUrl().setRedirectUri(cfg.callbackUrl).build()
                response!!.sendRedirect(authorizationUrl)
                return ResponseEntity<Any?>(HttpStatus.OK)
            }

            val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential!!)
                .setApplicationName(cfg.applicationName)
                .build()
            val response = service.spreadsheets().get(cfg.spreadsheetId).execute()
            EchoApplication.questionAndAnswerSets = response.sheets.map { sheet ->
                val range = "${sheet.properties.title}!A3:G"
                val valueRange = service.spreadsheets().values().get(cfg.spreadsheetId, range).execute()
                QuestionAndAnswerSet().also {
                    it.name = sheet.properties.title
                    it.imageUrl = service.spreadsheets().values().get(cfg.spreadsheetId, "${sheet.properties.title}!B1").execute().getValues()?.get(0)?.get(0)?.toString()
                    it.questionAndAnswers = valueRange.getValues()?.mapNotNull { row ->
                        row as List<*>
                        try {
                            if (row.size < 3 || (0..2).any { row[it] == null || (row[it] as? String)?.isBlank() == true }) {
                                return@mapNotNull null
                            }
                            QuestionAndAnswer().also {
                                it.code = row[0].toString()
                                it.question = row[1].toString()
                                it.answers = row.subList(2, row.size).mapNotNull { it as? String }.filter { !it.isNullOrBlank() }
                            }
                        } catch (t: Throwable) {
                            errorCount++
                            t.printStackTrace()
                            null
                        }
                    } ?: listOf()
                }
            }
            val message = "<html><body>Data loaded successfully with ${errorCount} errors. Number of question sets = ${EchoApplication.questionAndAnswerSets.size}. Total number of questions = ${EchoApplication.questionAndAnswerSets.sumBy { it.questionAndAnswers.size }}<br/><a href='..'>กลับหน้าแรก</a></body><html>"
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
