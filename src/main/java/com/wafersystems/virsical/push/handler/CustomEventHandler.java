package com.wafersystems.virsical.push.handler;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

/**
 * 自定义ws事件处理
 *
 * @author tandk
 * @date 2020/6/4 10:37
 */
@Slf4j
@Component
@AllArgsConstructor
public class CustomEventHandler {

  /**
   * ws事件处理
   *
   * @param accessor accessor
   */
  public void handler(StompHeaderAccessor accessor) {
    log.debug("CustomEventHandler");
  }
}
