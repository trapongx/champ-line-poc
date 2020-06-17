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

import com.google.common.collect.Queues
import com.linecorp.bot.model.action.MessageAction
import com.linecorp.bot.model.event.Event
import com.linecorp.bot.model.event.MessageEvent
import com.linecorp.bot.model.event.message.TextMessageContent
import com.linecorp.bot.model.message.*
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
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.event.EventListener
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@SpringBootApplication
@LineMessageHandler
@EnableConfigurationProperties
@EnableMongoRepositories
open class EchoApplication {
    private val log = LoggerFactory.getLogger(EchoApplication::class.java)

    @Autowired
    private lateinit var loadDataController: LoadDataController

    @Autowired
    private lateinit var chatLogRepository: ChatLogRepository

    private var loggerThread: Thread? = null
    private val logQueue: Queue<ChatLog> = LinkedList()
    private val logQueueLock = ReentrantLock()

    private fun log(event: MessageEvent<TextMessageContent>) {
        ChatLog().also {
            it.source = event.source
            it.message = event.message
        }.also {
            logQueue.add(it)
        }

        if (logQueueLock.tryLock(1000, TimeUnit.SECONDS)) {
            try {
                if (loggerThread == null || !loggerThread!!.isAlive) {
                    loggerThread = Thread(Runnable {
                        try {
                            while (!logQueue.isEmpty()) {
                                logQueue.poll().also {
                                    chatLogRepository.save(it)
                                }
                            }
                        } catch (t: Throwable) {
                            t.printStackTrace()
                        }
                    }).also {
                        it.start()
                    }
                }
            } finally {
                logQueueLock.unlock()
            }
        }
    }

    @EventListener
    fun loadDataOnStart(event: ApplicationReadyEvent) {
        try {
            loadDataController.loadData(true)
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    @EventMapping
    fun handleTextMessageEvent(event: MessageEvent<TextMessageContent>): List<Message> {
        log(event)
        log.info("event: $event")
        val text = event.message.text
        for (qnaSet in questionAndAnswerSets) {
            if (text == qnaSet.name) {
                return listOf(showMenuForQSelection(qnaSet))
            }
        }
        for (qnaSet in questionAndAnswerSets) {
            for (qna in qnaSet.questionAndAnswers) {
                if (text == qna.code) {
                    return showAnswer(qna)
                }
            }
        }
        return listOf(showMenuForQnaSetSelection())
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

    private fun showAnswer(qna: QuestionAndAnswer): List<Message> {
        return qna.answers.map {
            when {
                it.matches(imageRegex) -> {
                    ImageMessage.builder().originalContentUrl(URI(it)).previewImageUrl(URI(it)).build()
                }
                it.matches(videoRegex) -> {
                    VideoMessage.builder().originalContentUrl(URI(it)).previewImageUrl(URI(it)).build()
                }
                else -> {
                    TextMessage.builder().text(it).build()
                }
            }
        }
    }

    companion object {
        var questionAndAnswerSets: List<QuestionAndAnswerSet> = listOf()
        val imageRegex = Regex("(http|https):.*(\\.)(jpg|JPG|jpeg|JPEG|png|PNG|gif|GIF|tif|TIF|tiff|TIFF).*")
        val videoRegex = Regex("(http|https):.*(\\.)(mp4|MP4|mpg|MPG|avi|AVI|flv|FLV|wmv|WMV|wma|WMA).*")

        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(EchoApplication::class.java, *args)
        }
    }
}
