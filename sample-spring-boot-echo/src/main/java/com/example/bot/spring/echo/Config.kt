package com.example.bot.spring.echo

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ipst.google")
class Config {
    lateinit var applicationName: String

    lateinit var spreadsheetId: String

    lateinit var callbackUrl: String

    lateinit var credentialsFilePath: String
}
