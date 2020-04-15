package com.wafersystems.virsical.push.receiver;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.push.common.PushConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Receiver {

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  private Boolean isFirst = true;

  /**
   * 监听推送消息队列
   *
   * @param message 消息
   */
  @RabbitListener(queues = "${push.fanout.queue}")
  public void receiveTopicPush(@Payload String message) {
    log.info("【push.fanout.queue监听到消息】{}", message);
    try {
      // 服务首次消费延时5秒，为了避免在服务刚启动后，WebSocket连接还未建立的情况下，进行消费
      if (isFirst) {
        Thread.sleep(5000);
        isFirst = false;
      }
      MessageDTO messageDTO = JSON.parseObject(message, MessageDTO.class);
      if (MsgTypeEnum.ALL.name().equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, messageDTO);
      } else if (MsgTypeEnum.BATCH.name().equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSend(PushConstants.PUSH_TOPIC_DESTINATION + messageDTO.getProduct(),
          messageDTO);
      } else if (MsgTypeEnum.ONE.name().equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSendToUser(messageDTO.getClientId(), PushConstants.PUSH_ONE_DESTINATION,
          messageDTO);
      } else {
        log.info("消息类型未识别，无法推送");
      }
    } catch (Exception e) {
      log.info("消息监听处理异常", e);
    }
  }
}