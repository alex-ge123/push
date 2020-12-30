package com.wafersystems.virsical.push.handler;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import com.wafersystems.virsical.common.core.constant.enums.MsgActionEnum;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.push.config.PushProperties;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
  private final RabbitTemplate rabbitTemplate;
  private final PushProperties pushProperties;

  /**
   * ws事件处理
   *
   * @param accessor accessor
   */
  public void handler(StompHeaderAccessor accessor) {
    if (pushProperties.isSendConnectMq()) {
      MessageDTO messageDTO = new MessageDTO();
      messageDTO.setMsgType(MsgTypeEnum.ONE.name());
      switch (Objects.requireNonNull(accessor.getCommand())) {
        case CONNECT:
          messageDTO.setMsgAction(MsgActionEnum.ADD.name());
          String user = Objects.requireNonNull(accessor.getUser()).getName();
          Map<String, List<String>> headerMap = accessor.toNativeHeaderMap();
          HashMap<String, String> map = new HashMap<>(10);
          if (headerMap.isEmpty()) {
            map.put("clientId", user);
          } else {
            for (String key : headerMap.keySet()) {
              map.put(key, headerMap.get(key).get(0));
            }
          }
          messageDTO.setData(map);
          break;
        case DISCONNECT:
          messageDTO.setMsgAction(MsgActionEnum.DELETE.name());
          break;
        default:
          break;
      }
      String data = JSON.toJSONString(messageDTO);
      rabbitTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_CONNECT, "", data);
      log.debug("发布[{}] MQ消息：{}", accessor.getCommand(), data);
    }
  }
}
