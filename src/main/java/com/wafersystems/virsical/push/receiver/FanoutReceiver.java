package com.wafersystems.virsical.push.receiver;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.push.common.PushConstants;
import com.wafersystems.virsical.push.config.RabbitMqConfig;
import com.wafersystems.virsical.push.model.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 广播消息消费者
 *
 * @author tandk
 * @date 2019/6/11 11:09
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnExpression("${push.service.cluster} == true")
public class FanoutReceiver {

  private final SimpMessagingTemplate simpMessagingTemplate;

  @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE_PUSH)
  public void receiveTopicPush(@Payload String message) {
    log.info("【fanout.queue.push监听到消息】" + message);
    MessageDTO messageDTO = JSON.parseObject(message, MessageDTO.class);
    if ("BATCH".equals(messageDTO.getMsgType())) {
      simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, messageDTO.getData());
    } else if ("ONE".equals(messageDTO.getMsgType())) {
      simpMessagingTemplate.convertAndSendToUser(messageDTO.getClientId(), PushConstants.PUSH_USER_DESTINATION, messageDTO.getData());
    }
  }
}