package com.wafersystems.virsical.push.receiver;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.constant.CommonConstants;
import com.wafersystems.virsical.common.core.constant.enums.EnvEnum;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.push.common.PushConstants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

  @Value("${spring.profiles.active}")
  private String springProfilesActive;

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
    try {
      // 服务首次消费延时10秒，为了避免在服务刚启动后，WebSocket连接还未建立的情况下，进行消费
      if (Boolean.TRUE.equals(isFirst)) {
        log.warn("服务首次启动消费延时10秒，为了避免在服务刚启动后，WebSocket连接还未建立的情况下进行消费。开始...");
        if (!EnvEnum.TESTCASE.getType().equals(springProfilesActive)) {
          Thread.sleep(10000);
        }
        isFirst = false;
        log.warn("延时10秒结束");
      }
      MessageDTO messageDTO = JSON.parseObject(message, MessageDTO.class);
      // 业务追踪id
      MDC.put(CommonConstants.LOG_BIZ_ID, messageDTO.getBizId());
      log.info("【push.fanout.queue监听到消息】{}", message);
      if (MsgTypeEnum.ALL.name().equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, messageDTO);
      } else if (MsgTypeEnum.ONE.name().equals(messageDTO.getMsgType())) {
        simpMessagingTemplate.convertAndSendToUser(messageDTO.getClientId(), PushConstants.PUSH_ONE_DESTINATION,
          messageDTO);
      } else {
        simpMessagingTemplate.convertAndSend(PushConstants.PUSH_TOPIC_DESTINATION + messageDTO.getProduct(),
          messageDTO);
      }
    } catch (InterruptedException e) {
      log.error("消息监听处理异常", e);
    }
  }
}