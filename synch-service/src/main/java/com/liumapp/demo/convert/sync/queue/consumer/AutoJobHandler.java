package com.liumapp.demo.convert.sync.queue.consumer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.liumapp.demo.convert.sync.queue.pattern.AutoJobPattern;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author liumapp
 * @file AutoJobHandler.java
 * @email liumapp.com@gmail.com
 * @homepage http://www.liumapp.com
 * @date 7/13/18
 */
@Component
public class AutoJobHandler implements ChannelAwareMessageListener {

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private static Logger logger = LoggerFactory.getLogger(AutoJobHandler.class);

    @Scheduled(cron = "* /5 * * * * *")
    public void printer () {
        logger.info("handle job per 5 seconds : " + atomicInteger.get());
        atomicInteger = new AtomicInteger(0);
    }

    @Override
    public void onMessage(Message message, Channel channel) throws Exception {
        boolean success = false;
        try {
            AutoJobPattern autoJobPattern = JSON.parseObject(new String(message.getBody()), new TypeReference<AutoJobPattern>() {});
            logger.info("get msg : " + autoJobPattern.toString());
            atomicInteger.incrementAndGet();
            success = true;
        } finally {
            if (success) {
                logger.info("auto job get , using ack ...");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                logger.info("auto job failed, using nack ...");
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        }
    }

}