package com.wafersystems.virsical.push.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.common.core.util.R;
import com.wafersystems.virsical.common.security.annotation.Inner;
import com.wafersystems.virsical.push.common.PushConstants;
import com.wafersystems.virsical.push.config.MessageManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

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
   * 定时打印在线人数
   */
  @Scheduled(fixedDelay = 60000)
  public void sendMessages() {
    Set<SimpUser> set = defaultSimpUserRegistry.getUsers();
    StringBuilder name = new StringBuilder();
    for (SimpUser user : set) {
      name.append(user.getName()).append(",");
    }
    log.info("[" + DateUtil.now() + "]当前在线用户数：" + defaultSimpUserRegistry.getUserCount() + "  " + name);
  }

  /**
   * 发布MQ广播测试推送服务监听消费推送
   *
   * @param product  产品名称
   * @param msgType  消息类型
   * @param clientId 终端id
   * @param msg      消息内容
   * @return R
   */
  @PostMapping("/send/fanout")
  public R sendFanout(@RequestParam String product, @RequestParam String msgType,
                      @RequestParam String clientId, @RequestParam String msg) {
    if (StrUtil.isBlank(msg)) {
      return R.fail();
    }
    log.info("群发广播消息: [{}]", msg);
    messageManager.sendFanout(PushMqConstants.EXCHANGE_FANOUT_PUSH_MESSAGE,
      new MessageDTO(1, clientId, product, msgType, "", "zh-CN",msg));
    return R.ok();
  }

  /**
   * 群发广播消息
   *
   * @param msg 消息内容
   * @return R
   */
  @Inner
  @PostMapping("/send/all")
  public R sendAll(@RequestParam String msg) {
    if (StrUtil.isBlank(msg)) {
      return R.fail();
    }
    log.info("当前用户数量: [{}]，群发广播消息: [{}]", defaultSimpUserRegistry.getUserCount(), msg);
    simpMessagingTemplate.convertAndSend(PushConstants.PUSH_ALL_DESTINATION, "[" + DateUtil.now() + "]群发广播消息：" + msg);
    return R.ok();
  }

  /**
   * 群发广播消息（指定产品主题）
   *
   * @param product product
   * @param msg     消息内容
   * @return R
   */
  @Inner
  @PostMapping("/send/topic")
  public R sendTopic(@RequestParam String product, @RequestParam String msg) {
    if (StrUtil.isBlank(msg)) {
      return R.fail();
    }
    String payload = "[" + DateUtil.now() + "]群发广播消息（指定产品[" + product + "]主题）：" + msg;
    log.info(payload);
    simpMessagingTemplate.convertAndSend(PushConstants.PUSH_TOPIC_DESTINATION + product, payload);
    return R.ok();
  }

  /**
   * 发布客户端消息
   *
   * @param clientId 终端id
   * @param msg      消息内容
   * @return R
   */
  @Inner
  @PostMapping("/send/one")
  public R sendOne(@RequestParam String clientId, @RequestParam String msg) {
    if (StrUtil.isBlank(clientId) && StrUtil.isBlank(msg)) {
      return R.fail();
    }
    log.info("发送指定用户[{}]订阅消息[{}]", clientId, msg);
    simpMessagingTemplate.convertAndSendToUser(clientId, PushConstants.PUSH_ONE_DESTINATION,
      "[" + DateUtil.now() + "]私发订阅消息：" + msg);
    return R.ok();
  }

  /**
   * 客户端发送消息到服务端，服务端再推送给指定人
   *
   * @param message 消息内容
   * @param user    用户
   */
  @MessageMapping("/client-send/one")
  public void clientSendOne(@Payload String message, @Header String user) {
    log.info("客户端发送消息: [{}]", message);
    if (StrUtil.isNotBlank(user) && StrUtil.isNotBlank(message)) {
      simpMessagingTemplate.convertAndSendToUser(user, PushConstants.PUSH_ONE_DESTINATION,
        "[" + DateUtil.now() + "]客户端私发消息：" + message);
    }
  }

}
