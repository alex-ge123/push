package com.wafersystems.virsical.push.receiver;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.push.common.PushConstants;
import com.wafersystems.virsical.push.model.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息消费者
 *
 * @author tandk
 * @date 2019/6/11 11:09
 */
@Slf4j
@Component
@AllArgsConstructor
@ConditionalOnExpression("${push.service.cluster} == true")
public class Receiver {

  private final SimpMessagingTemplate simpMessagingTemplate;

  private static Boolean isFirst = true;

  /**
   * 监听推送消息队列
   *
   * @param message 消息
   */
  @RabbitListener(queues = "${push.fanout.queue}")
  public void receiveTopicPush(@Payload String message) {
    try {
      log.info("【push.fanout.queue监听到消息】{}", message);
      // 服务首次消费延时5秒，为了避免在服务刚启动后，WebSocket连接还未建立的情况下，进行消费
      if (isFirst) {
        try {
          Thread.sleep(5000);
          isFirst = false;
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
      MessageDTO messageDTO = JSON.parseObject(message, MessageDTO.class);
      String all = "ALL";
      String one = "ONE";
      if (all.equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, messageDTO.getData());
      } else if (one.equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSendToUser(messageDTO.getClientId(), PushConstants.PUSH_USER_DESTINATION,
          messageDTO.getData());
      }
    } catch (Exception e) {
      log.info("消息监听处理异常", e);
    }
  }
}