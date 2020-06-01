package com.example.bot.spring.echo

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException

@RestController
@RequestMapping("/loadData")
class LoadDataController {
    @GetMapping("")
    @Throws(IOException::class, GeneralSecurityException::class)
    fun loadData(): String {
        var errorCount = 0
        try {
            // Build a new authorized API client service.
            val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
            val spreadsheetId = "1YXKi-edabERHUg4v1OSBLcylZv7ol0PW69inID7cH7k" //"1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            val service = Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build()
            val response = service.spreadsheets().get(spreadsheetId).execute()
            EchoApplication.questionAndAnswerSets = response.sheets.map { sheet ->
                val range = "${sheet.properties.title}!A2:C"
                val valueRange = service.spreadsheets().values().get(spreadsheetId, range).execute()
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
            return "Success with ${errorCount} errors"
        } catch (t: Throwable) {
            t.printStackTrace()
            return "Failed: ${t.message ?: t.toString()}"
        }
    }

    companion object {
        private const val APPLICATION_NAME = "Google Sheets API Java Quickstart"
        private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
        private const val TOKENS_DIRECTORY_PATH = "tokens"

        /**
         * Global instance of the scopes required by this quickstart.
         * If modifying these scopes, delete your previously saved tokens/ folder.
         */
        private val SCOPES = listOf(SheetsScopes.SPREADSHEETS_READONLY)
        private const val CREDENTIALS_FILE_PATH = "/credentials.json"

        /**
         * Creates an authorized Credential object.
         * @param HTTP_TRANSPORT The network HTTP Transport.
         * @return An authorized Credential object.
         * @throws IOException If the credentials.json file cannot be found.
         */
        @Throws(IOException::class)
        private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
            // Load client secrets.
            val `in` = EchoApplication::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
                ?: throw FileNotFoundException("Resource not found: $CREDENTIALS_FILE_PATH")
            val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))

            // Build flow and trigger user authorization request.
            val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
            val receiver = LocalServerReceiver.Builder()
                .setHost("champ-line-poc-a8898b0f.localhost.run").setPort(443).build()
            return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
        }
    }
}
