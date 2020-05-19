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

import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.MessageAction;
import com.linecorp.bot.model.action.PostbackAction;
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
import com.linecorp.bot.model.message.imagemap.MessageImagemapAction;
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
        } else if ("flex4".equals(originalMessageText)) {
            try {
                return flex4();
            } catch (Throwable t) {
                return flexError(t);
            }
        } else if ("flex5".equals(originalMessageText)) {
            try {
                return flex5();
            } catch (Throwable t) {
                return flexError(t);
            }
        } else if ("flex6".equals(originalMessageText)) {
            try {
                return flex6();
            } catch (Throwable t) {
                return flexError(t);
            }
        } else if ("flex7".equals(originalMessageText)) {
            try {
                return flex7();
            } catch (Throwable t) {
                return flexError(t);
            }
        } else if ("pd001".equals(originalMessageText)) {
            return pd001();
        } else if ("pd002".equals(originalMessageText)) {
            return pd002();
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
                                            Box.builder().layout(FlexLayout.HORIZONTAL)
                                                .contents(
                                                    Arrays.asList(
                                                        Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                FlexGravity.CENTER).action(new MessageAction("xxx", "yyy")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://storage.googleapis.com/s.race.thai.run/files/ad6ccbef-ade8-4922-99f6-6410af0ec71e.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "bbb")).build(),

                                                            Image.builder().url(new URI("https://app.smartsupportsystems.com/niti/img/pd001.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "pd001")).build(),

                                                            Image.builder().url(new URI("https://app.smartsupportsystems.com/niti/img/pd002.png")).size(
                                                                    Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                    FlexGravity.CENTER).action(new MessageAction("aaa", "pd002")).build()
                                                            )
                                                )
                                                .build()
                                        )
                                        .build()
                            )
                        )
                        .build()
            )
            .build();
    }

    private Message flex4() throws URISyntaxException {
        return FlexMessage.builder()
                          .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
                          .contents(
                                  Carousel.builder()
                                          .contents(
                                                  Arrays.asList(
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL)
                                                                           .contents(
                                                                                   Arrays.asList(
                                                                                           Image.builder().url(new URI("https://app.smartsupportsystems.com/niti/img/pd001.png")).size(
                                                                                                   Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                                                   FlexGravity.CENTER).action(new MessageAction("aaa", "pd001")).build(),

                                                                                           Image.builder().url(new URI("https://app.smartsupportsystems.com/niti/img/pd002.png")).size(
                                                                                                   Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                                                   FlexGravity.CENTER).action(new MessageAction("aaa", "pd002")).build()
                                                                                   )
                                                                           )
                                                                           .build()
                                                                )
                                                                .build()
                                                  )
                                          )
                                          .build()
                          )
                          .build();
    }

    private Message flex5() throws URISyntaxException {
        return FlexMessage.builder()
                          .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
                          .contents(
                                  Carousel.builder()
                                          .contents(
                                                  Arrays.asList(
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("สมัครสมาชิกแล้ว ระบบแจ้ง Email ซ้ำ (New)")
                                                                                .wrap(true)
                                                                              .action(new MessageAction("pd001", "pd001")).build()).build()
                                                                )
                                                                .build(),
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("ลืมรหัสผ่าน/ชื่อผู้ใช้ (New)").wrap(true).action(new MessageAction("pd002", "pd002")).build()).build()
                                                                )
                                                                .build(),
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("ยืนยันตัวตนยังไง").wrap(true).action(new MessageAction("pd003", "pd003")).build()).build()
                                                                )
                                                                .build()
                                                  )
                                          )
                                          .build()
                          )
                          .build();
    }

    private Message flex6() throws URISyntaxException {
        return FlexMessage.builder()
                          .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
                          .contents(
                                  Carousel.builder()
                                          .contents(
                                                  Arrays.asList(
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .hero(
                                                                        Image.builder().url(new URI("https://app.smartsupportsystems.com/niti/img/pd001.png")).size(
                                                                                Image.ImageSize.XXXXXL).aspectMode(Image.ImageAspectMode.Cover).aspectRatio("1:1").gravity(
                                                                                FlexGravity.CENTER).action(new MessageAction("aaa", "pd001")).build()
                                                                )
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("สมัครสมาชิกแล้ว ระบบแจ้ง Email ซ้ำ (New)").maxLines(10)
                                                                                                                                .action(new MessageAction("pd001", "pd001")).build()).build()
                                                                )
                                                                .footer(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("สมัครสมาชิกแล้ว ระบบแจ้ง Email ซ้ำ (New)")
                                                                                                                                .action(new MessageAction("pd001", "pd001")).build()).build()
                                                                )
                                                                .build(),
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("ลืมรหัสผ่าน/ชื่อผู้ใช้ (New)").action(new MessageAction("pd002", "pd002")).build()).build()
                                                                )
                                                                .build(),
                                                          Bubble.builder().direction(FlexDirection.LTR)
                                                                .body(
                                                                        Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("ยืนยันตัวตนยังไง").action(new MessageAction("pd003", "pd003")).build()).build()
                                                                )
                                                                .build()
                                                  )
                                          )
                                          .build()
                          )
                          .build();
    }

    private Message flex7() throws URISyntaxException {
        return FlexMessage.builder()
                          .altText("งาน VRUN ที่กำลังจัดอยู่ทั้งหมด")
                          .contents(

                                  Bubble.builder().direction(FlexDirection.LTR)
                                        .hero(
                                                Box.builder().layout(FlexLayout.HORIZONTAL).content(Text.builder().text("คำถามหมวด Teacher PD").build()).build()
                                        )
                                        .body(
                                                Box.builder().layout(FlexLayout.HORIZONTAL).contents(
                                                        Arrays.asList(
                                                                Text.builder().text("สมัครสมาชิกแล้ว ระบบแจ้ง Email ซ้ำ (New)")
                                                                    .action(new MessageAction("pd001", "pd001")).build(),
                                                                Text.builder().text("ลืมรหัสผ่าน/ชื่อผู้ใช้ (New)")
                                                                    .action(new MessageAction("pd002", "pd002")).build(),
                                                                Text.builder().text("ยืนยันตัวตนยังไง")
                                                                    .action(new MessageAction("pd003", "pd003")).build(),
                                                                Text.builder().text("เรียนจบแล้ว ได้คะแนน ......% (80%ขึ้นไป) ทำไมไม่ได้วุฒิบัตร / ทำไมสถานะยังเป็นกำลังเรียน ? (ระบบเก่า)")
                                                                    .action(new MessageAction("pd004", "pd004")).build()

                                                        )

                                                ).build()
                                        )
                                        .build()
                          )
                          .build();
    }

    private Message pd001() {
        return TextMessage.builder().text("ตอบ จากปัญหาที่แจ้งมาคุณครูสามารถล็อกอินเข้าใช้งานด้วย email ได้ โดยคุณครูต้องทำการกดลืมรัหสผ่าน ตามขั้นตอนดังนี้\n" +
                                                  "1.\tกดที่ปุ่ม “เข้าสู่ระบบ/สมัครสมาชิก”\n" +
                                                  "2.\tกดที่ “ลืมรหัสผ่านใช่หรือไม่”\n" +
                                                  "3.\tกรอก ชื่อผู้ใช้ (username) หรือ อีเมล์ที่สมัครสมาชิก\n" +
                                                  "4.\tตรวจสอบที่อีเมลของคุณครู โดยตรวจสอบที่กล่องขาเข้า (Inbox) และ อีเมลขยะ (Junk Mail)\n" +
                                                  "5.\tคลิกที่ “ลิงก์สำหรับตั้งค่ารหัสผ่านใหม่” (Email ตั้งรหัสใหม่จะหมดอายุภายใน5 นาที) ระบบแสดงหน้าให้กรอกรหัสผ่านใหม่ และยืนยันรหัสผ่าน (การยืนยันรหัสผ่าน คือ การกรอกรหัสผ่านให้เหมือนกับช่องรหัสผ่านใหม่)\n" +
                                                  "6.\t หากกรอกรหัสผ่านทั้ง 2 ช่องไม่ตรงกัน ระบบจะแสดงข้อความเตือน “รหัสผ่านที่ระบุไม่ตรงกัน”\n" +
                                                  "7.\tเมื่อตั้งรหัสใหม่ได้แล้ว ระบบจะ login ให้ทันที ").build();
    }

    private Message pd002() {
        return TextMessage.builder().text("ตอบ คุณครูสามารถล็อกอินเข้าใช้งานด้วย email ได้ค่ะ คุณครูทำการกดลืมรัหสผ่าน ตามขั้นตอนดังนี้\n" +
                                                  "1.\tกดที่ปุ่ม “เข้าสู่ระบบ/สมัครสมาชิก”\n" +
                                                  "2.\tกดที่ “ลืมรหัสผ่านใช่หรือไม่”\n" +
                                                  "3.\tกรอก ชื่อผู้ใช้ (username) หรือ อีเมล์ที่สมัครสมาชิก\n" +
                                                  "4.\tตรวจสอบที่อีเมลของคุณครู โดยตรวจสอบที่กล่องขาเข้า (Inbox) และ อีเมลขยะ (Junk Mail)\n" +
                                                  "5.\tคลิกที่ “ลิงก์สำหรับตั้งค่ารหัสผ่านใหม่” (Email ตั้งรหัสใหม่จะหมดอายุภายใน5 นาที) ระบบแสดงหน้าให้กรอกรหัสผ่านใหม่ และยืนยันรหัสผ่าน (การยืนยันรหัสผ่าน คือ การกรอกรหัสผ่านให้เหมือนกับช่องรหัสผ่านใหม่)\n" +
                                                  "6.\t หากกรอกรหัสผ่านทั้ง 2 ช่องไม่ตรงกัน ระบบจะแสดงข้อความเตือน “รหัสผ่านที่ระบุไม่ตรงกัน”\n" +
                                                  "7.\tเมื่อตั้งรหัสใหม่ได้แล้ว ระบบจะ login ให้ทันที").build();
    }
}
