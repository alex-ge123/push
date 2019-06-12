package com.wafersystems.virsical.push.controller;

import cn.hutool.core.util.StrUtil;
import com.wafersystems.virsical.push.common.PushConstants;
import com.wafersystems.virsical.push.config.MessageManager;
import com.wafersystems.virsical.push.config.RabbitMqConfig;
import com.wafersystems.virsical.push.model.MessageDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tandk
 * @date 2019/5/30 09:41
 */
@Slf4j
@RestController
public class SendController {

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @Autowired
  private SimpUserRegistry defaultSimpUserRegistry;

  @Autowired(required = false)
  private MessageManager messageManager;

  /**
   * 发布MQ广播测试推送服务监听消费推送
   *
   * @param msgType  消息类型
   * @param clientId 终端id
   * @param msg      消息内容
   * @return ok
   */
  @PostMapping("send-fanout")
  public String sendFanout(@RequestParam String msgType, @RequestParam String clientId, @RequestParam String msg) {
    if (StrUtil.isBlank(msg)) {
      return "fail";
    }
    log.info("群发广播消息: [{}]", msg);
    messageManager.sendFanout(RabbitMqConfig.PUSH_FANOUT_EXCHANGE, new MessageDTO(1, clientId, msgType, "", msg));
    return "ok";
  }

  /**
   * 群发广播消息
   *
   * @param msg 消息内容
   * @return ok
   */
  @PostMapping("send-to-all")
  public String convertAndSend(@RequestParam String msg) {
    if (StrUtil.isBlank(msg)) {
      return "fail";
    }
    log.info("当前用户数量: [{}]，群发广播消息: [{}]", defaultSimpUserRegistry.getUserCount(), msg);
    simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, "群发广播消息：" + msg);
    return "ok";
  }

  /**
   * 发布客户端消息
   *
   * @param userId 用户id
   * @param msg    消息内容
   * @return ok
   */
  @PostMapping("send-to-user")
  public String convertAndSendToUser(@RequestParam String userId, @RequestParam String msg) {
    if (StrUtil.isBlank(userId) && StrUtil.isBlank(msg)) {
      return "fail";
    }
    log.info("私发订阅消息: [{}]", msg);
    simpMessagingTemplate.convertAndSendToUser(userId, PushConstants.PUSH_USER_DESTINATION, "私发订阅消息：" + msg);
    return "ok";
  }

  /**
   * 客户端发送消息到服务端，服务端再推送给指定人
   *
   * @param message 消息内容
   */
  @MessageMapping("/reply")
  public void reply(@Payload String message, @Header String user) {
    log.info("客户端发送消息: [{}]", message);
    if (StrUtil.isNotBlank(user) && StrUtil.isNotBlank(message)) {
      simpMessagingTemplate.convertAndSendToUser(user, PushConstants.PUSH_USER_DESTINATION, "客户端私发消息：" + message);
    }
  }

}
