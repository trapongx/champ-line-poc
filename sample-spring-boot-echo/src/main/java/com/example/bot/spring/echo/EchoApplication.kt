/*
 * Copyright 2016 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.example.bot.spring.echo

import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.FlexMessage
import com.linecorp.bot.model.message.Message
import com.linecorp.bot.model.message.TextMessage
import com.linecorp.bot.model.message.flex.component.Box
import com.linecorp.bot.model.message.flex.component.Text
import com.linecorp.bot.model.message.flex.container.Bubble
import com.linecorp.bot.model.message.flex.container.Carousel
import com.linecorp.bot.model.message.flex.unit.*
import com.linecorp.bot.spring.boot.annotation.EventMapping
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener

@SpringBootApplication
@LineMessageHandler
open class EchoApplication {
    private val log = LoggerFactory.getLogger(EchoApplication::class.java)

    @Autowired
    private lateinit var loadDataController: LoadDataController

    @EventListener
    fun loadDataOnStart(event: ApplicationReadyEvent) {
        try {
            loadDataController.loadData()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): Message {
        log.info("event: $event")
        val text = event.message.text
        for (qnaSet in questionAndAnswerSets) {
            if (text == qnaSet.name) {
                return showMenuForQSelection(qnaSet)
            }
        }
        for (qnaSet in questionAndAnswerSets) {
            for (qna in qnaSet.questionAndAnswers) {
                if (text == qna.code) {
                    return showAnswer(qna)
                }
            }
        }
        return showMenuForQnaSetSelection()
    }

    fun showMenuForQnaSetSelection(): Message {
        if (questionAndAnswerSets.isEmpty()) {
            return TextMessage.builder().text("ขออภัย ระบบตอบกลับอัตโนมัติยังไม่พร้อมใช้งานในขณะนี้").build()
        }

        return FlexMessage.builder()
            .altText("เมนู")
            .contents(
                Carousel.builder()
                    .contents(
                        questionAndAnswerSets.map { qnaSet ->
                            Bubble.builder().direction(FlexDirection.LTR)
                                .body(
                                    Box.builder().layout(FlexLayout.HORIZONTAL)
                                        .content(
                                            Text.builder()
                                                .text(qnaSet.name)
                                                .align(FlexAlign.CENTER)
                                                .wrap(true)
                                                .action(MessageAction(qnaSet.name, qnaSet.name))
                                                .build()
                                        ).build()
                                )
                                .footer(
                                    Box.builder().layout(FlexLayout.HORIZONTAL)
                                        .content(
                                            Text.builder()
                                                .text("หมวดคำถาม")
                                                .wrap(true)
                                                .color("#C3C2BE")
                                                .size(FlexFontSize.XXS)
                                                .align(FlexAlign.CENTER)
                                                .margin(FlexMarginSize.MD)
                                                .build()
                                        ).build()
                                )
                                .build()
                        }
                    ).build()
            )
            .build()
    }

    private fun showMenuForQSelection(qnaSet: QuestionAndAnswerSet): Message {
        if (qnaSet.questionAndAnswers.isEmpty()) {
            return TextMessage.builder().text("ไม่มีรายการคำถามในชุดคำถามนี้").build()
        }
        return FlexMessage.builder()
            .altText("เมนู")
            .contents(
                Carousel.builder()
                    .contents(
                        qnaSet.questionAndAnswers.map { qna ->
                            Bubble.builder().direction(FlexDirection.LTR)
                                .body(
                                    Box.builder().layout(FlexLayout.HORIZONTAL)
                                        .content(
                                            Text.builder()
                                                .text(qna.question)
                                                .wrap(true)
                                                .action(MessageAction(qna.code, qna.code))
                                                .build()
                                        ).build()
                                )
                                .footer(
                                    Box.builder().layout(FlexLayout.HORIZONTAL)
                                        .content(
                                            Text.builder()
                                                .text("คำถามรหัส ${qna.code}")
                                                .wrap(true)
                                                .color("#C3C2BE")
                                                .size(FlexFontSize.XXS)
                                                .align(FlexAlign.CENTER)
                                                .margin(FlexMarginSize.MD)
                                                .build()
                                        ).build()
                                )
                                .build()
                        }
                    ).build()
            )
            .build()
    }

    private fun showAnswer(qna: QuestionAndAnswer): Message {
        return TextMessage.builder().text(qna.answer!!).build()
    }

    companion object {
        var questionAndAnswerSets: List<QuestionAndAnswerSet> = listOf()

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(EchoApplication::class.java, *args)
        }
    }
}
