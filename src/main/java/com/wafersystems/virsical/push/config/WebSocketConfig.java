package com.wafersystems.virsical.push.config;

import com.alibaba.fastjson.JSON;
import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import com.wafersystems.virsical.common.core.constant.enums.MsgActionEnum;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.push.handler.CheckTokenHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

import java.util.Map;

/**
 * WebSocket配置
 *
 * <p>@EnableWebSocketMessageBroker</p>
 * <p>开启使用STOMP协议来传输基于代理(message broker)的消息,</p>
 * <p>此时浏览器支持使用@MessageMapping 就像支持@RequestMapping一样。</p>
 *
 * @author tandk
 * @date 2019/6/10 10:42
 */
@Slf4j
@Configuration
@EnableScheduling
@AllArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Autowired
  private CheckTokenHandler checkTokenHandler;

  /**
   * controller 注册协议节点,并映射指定的URl
   *
   * @param registry stomp端点注册表
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 注册一个STOMP协议的endpoint[/ws]，允许跨域访问，并指定 SockJS协议
    registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS().setClientLibraryUrl("/web/js/socket.min.js").setWebSocketEnabled(true);
  }

  /**
   * 配置消息代理(MessageBroker)
   *
   * @param registry 消息代理注册表
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 服务端发起心跳线程池
    ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
    te.setPoolSize(1);
    te.setThreadNamePrefix("wss-heartbeat-thread-");
    te.initialize();
    // 当我们使用基于WebSocket/SockJS协议的STOMP时，如果STOMP客户端与服务器端要协商心跳交换的时候，SockJS的心跳就不起作用。
    // 定义服务端心跳间隔时间，单位毫秒
    // 第一个参数:server能保证的发送心跳的最小间隔, 如果是0代表server不发送心跳.
    // 第二个参数:server希望收到client心跳的间隔, 如果是0代表server不希望收到client的心跳.
    long[] heartBeat = {20000L, 20000L};
    /*
     * 创建内存中的消息代理，其中包含一个或多个用于发送和接收消息的目标。
     * 定义了两个目标地址前缀： topic和 user。
     * 通过pub-sub模型将以 topic为前缀的消息传递到所有订阅客户端的目标地址。
     * 使用topic来达到群发效果，使用user进行一对一发送，
     *
     * enableSimpleBroker启用简单的消息代理，配置一个或多个代理的目标前缀
     * setHeartbeatValue设置后台向前台发送的心跳频率，这个不能单独设置，不然不起作用
     * 配合后面setTaskScheduler才可以生效，使用一个线程发送心跳。
     */
    registry.enableSimpleBroker("/topic", "/one")
      .setHeartbeatValue(heartBeat)
      .setTaskScheduler(te);
    // 配置用于标识用户目标的前缀。
    registry.setUserDestinationPrefix("/user");
    // 定义前缀 app，用于过滤目标地址，这些地址在 Controller中被 @MessageMapping修饰的方法处理。
    registry.setApplicationDestinationPrefixes("/app");
  }

  /**
   * 配置用于到WebSocket客户端的入站消息的MessageChannel。
   *
   * @param registration 通道注册表
   */
  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new ChannelInterceptor() {
      /**
       * 通道拦截器（在消息实际发送到通道之前调用）
       * @param message 消息
       * @param channel 消息通道
       * @return message
       */
      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
          return null;
        }
        // 连接请求
        String token = accessor.getFirstNativeHeader("token");
        String clientId = accessor.getFirstNativeHeader("clientId");
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
          log.debug("CONNECT >>> {}", message);
          // 验证token
          checkTokenHandler.checkToken(token);
          // 设置当前用户
          WebSocketPrincipal webSocketPrincipal = new WebSocketPrincipal(clientId);
          accessor.setUser(webSocketPrincipal);
        } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
          log.debug("DISCONNECT >>> {}", message);
        }
        return message;
      }

      /**
       * 在发送调用之后立即调用
       * <p>只有当preSend成功时才会调用此命令。</p>
       *
       * @param message 消息
       * @param channel 通道
       * @param sent 该调用的返回值
       */
//      @Override
//      public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        if (accessor != null && !SimpMessageType.HEARTBEAT.equals(accessor.getMessageType())) {
//          log.info("webSocket postSend | Message [{}] | MessageChannel [{}] | sent [{}]", message, channel, sent);
////          if (SimpMessageType.SUBSCRIBE.equals(accessor.getMessageType())) {
////            log.info("当前终端：{}", accessor.getUser().getName());
////          }
//        }
//      }

//      /**
//       * 在发送完成后调用，而不考虑引发的任何异常，从而允许正确的资源清理。
//       * <p>只有当preSend成功时才会调用此命令。</p>
//       *
//       * @param message 消息
//       * @param channel 通道
//       * @param sent 该调用的返回值
//       * @param ex 异常
//       * @since 4.1
//       */
//      @Override
//      public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        if (accessor != null && !SimpMessageType.HEARTBEAT.equals(accessor.getMessageType())) {
//          log.info("webSocket afterSendCompletion | Message [{}] | MessageChannel [{}] | sent [{}]", message,
//            channel, sent);
//          if (SimpMessageType.CONNECT.equals(accessor.getMessageType())) {
//            String clientId = accessor.getFirstNativeHeader("clientId");
//            MessageDTO messageDTO = new MessageDTO(MsgTypeEnum.ONE.name(), MsgActionEnum.ADD.name(), clientId);
//            rabbitTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_CONNECT, "",
//              JSON.toJSONString(messageDTO));
//
//          }
//        }
//      }
//
//      /**
//       * 一旦调用了Receive，在实际检索消息之前调用。如果返回值为“false”，则不会检索任何消息。
//       * <p>这仅适用于可轮询通道。</p>
//       *
//       * @param channel 通道
//       */
//      @Override
//      public boolean preReceive(MessageChannel channel) {
//        log.info("webSocket preReceive");
//        return false;
//      }
//
//      /**
//       * 在检索到消息后立即调用，但在将消息返回给调用方之前调用。
//       * <p>如果需要，可以修改消息；@code null中止进一步的拦截器调用。</p>
//       * <p>这仅适用于可轮询通道。</p>
//       *
//       * @param message 消息
//       * @param channel 通道
//       */
//      @Override
//      public Message<?> postReceive(Message<?> message, MessageChannel channel) {
//        log.info("webSocket postReceive");
//        return null;
//      }
//
//      /**
//       * 在接收完成后调用，而不考虑已引发的任何异常，从而允许正确的资源清理。
//       * <p>请注意，只有当postReceive成功完成并返回true时，才会调用此函数。</p>
//       *
//       * @param message 消息
//       * @param channel 通道
//       * @param ex 异常
//       * @since 4.1
//       */
//      @Override
//      public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
//        log.info("webSocket afterReceiveCompletion");
//      }
    });

    // 配置corePoolSize核心线程数, maxPoolSize最大线程数, queueCapacity队列容积
//    registration.taskExecutor().corePoolSize(32).maxPoolSize(200).queueCapacity(10000);
  }
//
//  /**
//   * 配置用于到WebSocket客户端的出站消息的MessageChannel。
//   * <p>默认情况下，通道由大小为1的线程池支持。建议为生产使用自定义线程池设置</p>
//   *
//   * @param registration 通道注册表
//   */
//  @Override
//  public void configureClientOutboundChannel(ChannelRegistration registration) {
//    // 配置corePoolSize核心线程数, maxPoolSize最大线程数, queueCapacity队列容积
//    registration.taskExecutor().corePoolSize(100).maxPoolSize(400).queueCapacity(20000);
//  }

//  /**
//   * Configure options related to the processing of messages received from and
//   * sent to WebSocket clients.
//   *
//   * @param registry WebSocket传输注册表
//   */
//  @Override
//  public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
//    registry
//      .setSendTimeLimit(15 * 1000)
//      .setSendBufferSizeLimit(512 * 1024);
//  }
}