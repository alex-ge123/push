package com.wafersystems.virsical.push.config;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * 消息管理
 *
 * @author tandk
 * @date 2019/4/3 18:21
 */
@Configuration
public class MessageManager {
  private final AmqpTemplate rabbitTemplate;

  @Autowired
  public MessageManager(AmqpTemplate rabbitTemplate) {
    this.rabbitTemplate = rabbitTemplate;
  }

  /**
   * 发送广播
   *
   * @param exchange   交换机
   * @param messageDTO 消息体
   */
  public void sendFanout(String exchange, MessageDTO messageDTO) {
    this.rabbitTemplate.convertAndSend(exchange, "", JSON.toJSONString(messageDTO));
  }
}
