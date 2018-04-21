package com.fsmeeting;

import com.alibaba.fastjson.JSON;
import com.fsmeeting.analytics.collect.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class OnlineInfoCollectMonitor {

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Test
    public void test() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                OnlineMsg onlineMsg = new OnlineMsg();
                onlineMsg.setBpartnerId(1 + new Random().nextInt(2));
                onlineMsg.setRoomId(1000 + new Random().nextInt(2));
                onlineMsg.setNum(new Random().nextInt(1000));
                onlineMsg.setTime(System.currentTimeMillis());
                onlineMsg.setFormatTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(onlineMsg.getTime()
                )));
                String msg = JSON.toJSONString(onlineMsg);
                System.out.println(msg);
                kafkaTemplate.send("live-online-users", msg);
            }
        }, 5, 3, TimeUnit.SECONDS);
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(new SimpleDateFormat("yyyyMMddHHmm").format(new Date(1524276864771l)));
    }
}
