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

package com.example.bot.spring.echo;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.component.Box;
import com.linecorp.bot.model.message.flex.component.Image;
import com.linecorp.bot.model.message.flex.component.Text;
import com.linecorp.bot.model.message.flex.container.Bubble;
import com.linecorp.bot.model.message.flex.container.Carousel;
import com.linecorp.bot.model.message.flex.unit.FlexAlign;
import com.linecorp.bot.model.message.flex.unit.FlexDirection;
import com.linecorp.bot.model.message.flex.unit.FlexGravity;
import com.linecorp.bot.model.message.flex.unit.FlexLayout;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@LineMessageHandler
public class EchoApplication {
    private final Logger log = LoggerFactory.getLogger(EchoApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(EchoApplication.class, args);
    }

    @EventMapping
    public Message handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info("event: " + event);
        final String originalMessageText = event.getMessage().getText();
        if ("flex".equals(originalMessageText)) {
            return FlexMessage
               .builder()
                  .altText("hello")
                  .contents(Bubble.builder().build())
                  .quickReply(null)
              .build();
        } else if("flex2".equals(originalMessageText)) {
            return new ExampleFlexMessageSupplier().get();
        } else if ("flex3".equals(originalMessageText)) {
            try {
                return flex3();
            } catch (Throwable t) {
                return flexError(t);
            }
        } else {
            return new TextMessage(originalMessageText);
        }
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);
    }

    private Message flexError(Throwable t) {
        return FlexMessage.builder()
                   .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
                   .contents(
                           Carousel.builder()
                                   .contents(
                                           Arrays.asList(
                                                   Bubble.builder().direction(FlexDirection.LTR)
                                                         .body(
                                                   Box.builder().layout(FlexLayout.VERTICAL)
                                                      .contents(
                                                              Arrays.asList(Text.builder().text("Error: " + t.getMessage()).build())
                                                      ).build()
                                                    ).build()
                                            )
                                   ).build()
                   ).build();
    }

    private Message flex3() throws URISyntaxException {
        return FlexMessage.builder()
            .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
            .contents(
                    Carousel.builder()
                        .contents(
                                Arrays.asList(
                                    Bubble.builder().direction(FlexDirection.LTR)
                                        .body(
                                            Box.builder().layout(FlexLayout.VERTICAL)
                                                .contents(
                                                    Arrays.asList(
                                                        Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                Image.ImageSize.MD).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                FlexGravity.CENTER).build()
                                                    )
                                                )
                                                .build()
                                        )
                                          .footer(
                                              Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                                                      Arrays.asList(
                                                              Box.builder().layout(FlexLayout.VERTICAL).contents(
                                                                      Text.builder().text("ผู้เข้าร่วมวิ่ง :").align(FlexAlign.END).offsetEnd("10px").color("#78909C").build()
                                                              ).build()
                                                      )
                                              ).build()
                                          )
                                        .build()
                            )
                        )
                        .build()
            )
            .build();
    }
}
