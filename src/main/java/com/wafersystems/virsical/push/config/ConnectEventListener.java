package com.wafersystems.virsical.push.config;

import com.wafersystems.virsical.push.handler.CustomEventHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.Objects;

/**
 * ws连接事件监听器
 *
 * @author tandk
 * @date 2019/8/6 17:57
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConnectEventListener implements ApplicationListener<SessionConnectEvent> {
  private final CustomEventHandler customEventHandler;

  /**
   * Handle an application event.
   *
   * @param event the event to respond to
   */
  @Override
  public void onApplicationEvent(SessionConnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

    //判断客户端的连接状态
    switch (Objects.requireNonNull(accessor.getCommand())) {
      case CONNECT:
        customEventHandler.handler(accessor);
        break;
      default:
        break;
    }
  }
}
