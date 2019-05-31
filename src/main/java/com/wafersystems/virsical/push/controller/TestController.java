package com.wafersystems.virsical.push.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tandk
 * @date 2019/5/30 09:41
 */
@RestController
public class TestController {

  @Autowired
  private SimpMessagingTemplate simpMessagingTemplate;

  @GetMapping("send-to-all")
  public String convertAndSend(String msg){
    simpMessagingTemplate.convertAndSend("/topic/to-all", "手动广播消息：" + msg);
    return "ok";
  }

  @GetMapping("send-to-user")
  public String convertAndSendToUser(String userId, String msg){
    simpMessagingTemplate.convertAndSendToUser(userId,"/to-user", "手动私发消息：" + msg);
    return "ok";
  }
}
