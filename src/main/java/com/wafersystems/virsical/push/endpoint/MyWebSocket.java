package com.wafersystems.virsical.push.endpoint;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.timeout.IdleStateEvent;
import org.springframework.stereotype.Component;
import org.yeauty.annotation.*;
import org.yeauty.pojo.ParameterMap;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tandk
 * @date 2019/5/29 18:37
 */
@ServerEndpoint()
@Component
public class MyWebSocket {

  private static Map<String, Session> clientMap = new ConcurrentHashMap<>();

  @OnOpen
  public void onOpen(Session session, HttpHeaders headers, ParameterMap map) throws IOException {
    String userId = map.getParameter("userId");
    System.out.println(userId);
    clientMap.put(userId, session);
    System.out.println("new connection");
  }

  @OnClose
  public void onClose(Session session) throws IOException {
    session.close();
    System.out.println("one connection closed");
  }

  @OnError
  public void onError(Session session, Throwable throwable) {
    throwable.printStackTrace();
  }

  @OnMessage
  public void OnMessage(Session session, String message) {
    System.out.println(message);
    session.sendText("Hello Netty!"+message);
  }

  @OnBinary
  public void onBinary(Session session, byte[] bytes) {
    for (byte b : bytes) {
      System.out.println(b);
    }
    session.sendBinary(bytes);
  }

  @OnEvent
  public void onEvent(Session session, Object evt) {
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
      switch (idleStateEvent.state()) {
        case READER_IDLE:
          System.out.println("read idle");
          break;
        case WRITER_IDLE:
          System.out.println("write idle");
          break;
        case ALL_IDLE:
          System.out.println("all idle");
          break;
        default:
          break;
      }
    }
  }

  public void send(String userId, String msg){
    clientMap.get(userId).sendText(msg);
  }
}
