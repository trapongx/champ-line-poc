package com.example.bot.spring.echo

import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.event.source.Source
import org.springframework.data.mongodb.core.mapping.Document

@Document
class ChatLog : BaseDocument() {
    var source: Source? = null

    var message: TextMessageContent? = null
}
