package com.wafersystems.virsical.push;

import com.wafersystems.virsical.push.endpoint.MyWebSocket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author tandk
 * @date 2019/5/30 09:41
 */
@RestController
public class TestController {

  @Autowired
  MyWebSocket myWebSocket;

  @GetMapping("/test")
  public String test(String userId, String msg){
    myWebSocket.send(userId, msg);
    return "send success : " + msg;
  }
}
