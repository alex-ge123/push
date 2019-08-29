package com.wafersystems.virsical.push.config;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import com.wafersystems.virsical.common.core.constant.enums.MsgActionEnum;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.Objects;

/**
 * 连接事件监听器
 *
 * @author tandk
 * @date 2019/8/6 17:57
 */
@Slf4j
@Component
@AllArgsConstructor
public class ConnectEventListener implements ApplicationListener<SessionConnectEvent> {
  private final RabbitTemplate rabbitTemplate;

  /**
   * Handle an application event.
   *
   * @param event the event to respond to
   */
  @Override
  public void onApplicationEvent(SessionConnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String user = Objects.requireNonNull(accessor.getUser()).getName();
    //判断客户端的连接状态
    switch (Objects.requireNonNull(accessor.getCommand())) {
      case CONNECT:
        log.info(user + " 上线");
        MessageDTO upMessageDTO = new MessageDTO(MsgTypeEnum.ONE.name(), MsgActionEnum.ADD.name(), user);
        rabbitTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_CONNECT, "",
          JSON.toJSONString(upMessageDTO));
        break;
      case DISCONNECT:
        log.info(user + " 下线");
        MessageDTO messageDTO = new MessageDTO(MsgTypeEnum.ONE.name(), MsgActionEnum.DELETE.name(), user);
        rabbitTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_CONNECT, "",
          JSON.toJSONString(messageDTO));
        break;
      case SUBSCRIBE:
        log.info("订阅");
        break;
      case SEND:
        log.info("发送");
        break;
      case UNSUBSCRIBE:
        log.info("取消订阅");
        break;
      default:
        break;
    }
  }
}