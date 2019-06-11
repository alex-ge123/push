package com.wafersystems.virsical.push.config;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置
 *
 * <br>	@EnableWebSocketMessageBroker
 * <br>		开启使用STOMP协议来传输基于代理(message broker)的消息,
 * <br>		此时浏览器支持使用@MessageMapping 就像支持@RequestMapping一样。
 *
 * @author tandk
 * @date 2019/2/1 10:42
 */
@Slf4j
@Configuration
@AllArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//  private CustomRemoteTokenServices tokenService

  /**
   * controller 注册协议节点,并映射指定的URl
   *
   * @param registry stomp端点注册表
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // 注册一个STOMP协议的endpoint[/ws]，允许跨域访问，并指定 SockJS协议
    registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
  }

  /**
   * 配置消息代理(MessageBroker)
   *
   * @param registry 消息代理注册表
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // 当我们使用基于WebSocket/SockJS协议的STOMP时，如果STOMP客户端与服务器端要协商心跳交换的时候，SockJS的心跳就不起作用。
    // 定义服务端心跳间隔时间，单位毫秒
    // 第一个参数:server能保证的发送心跳的最小间隔, 如果是0代表server不发送心跳.
    // 第二个参数:server希望收到client心跳的间隔, 如果是0代表server不希望收到client的心跳.
    long[] heartBeat = {10000L, 10000L};
    ThreadPoolTaskScheduler te = new ThreadPoolTaskScheduler();
    te.setPoolSize(1);
    te.setThreadNamePrefix("wss-heartbeat-thread-");
    te.initialize();
    // enableSimpleBroker启用简单的消息代理，配置一个或多个代理的目标前缀
    // setHeartbeatValue设置后台向前台发送的心跳频率，这个不能单独设置，不然不起作用
    // 配合后面setTaskScheduler才可以生效，使用一个线程发送心跳。
    registry.enableSimpleBroker("/topic", "/user")
      .setHeartbeatValue(heartBeat)
      .setTaskScheduler(te);
    // 点对点使用的订阅前缀（客户端订阅路径上会体现出来），不设置的话，默认也是/user/
    // 配置用于标识用户目标的前缀。用户目的地为用户提供订阅其唯一队列名称的能力
    // 会话以及其他人向那些唯一的用户发送消息，用户特定的队列。
    registry.setUserDestinationPrefix("/user");
  }

  /**
   * 配置客户端进入通道
   *
   * @param registration 通道注册
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
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
          String tokenHeader = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);
          String product = accessor.getFirstNativeHeader("product");
          String tenant = accessor.getFirstNativeHeader("tenant");
          String terminal = accessor.getFirstNativeHeader("terminal");
          String id = accessor.getFirstNativeHeader("id");
          log.info("webSocket preSend: token=[{}], product=[{}], tenant=[{}], terminal=[{}], id=[{}]", tokenHeader, product, tenant, terminal, id);
//          if (StrUtil.isBlank(tokenHeader) || !tokenHeader.startsWith(OAuth2AccessToken.BEARER_TYPE.toLowerCase())) {
//            log.info("tokenHeader格式异常[{}]", tokenHeader);
//            throw new BusinessException(String.format("tokenHeader格式异常[%s]", tokenHeader));
//          }
//          String token = tokenHeader.replace(OAuth2AccessToken.BEARER_TYPE.toLowerCase(), "").trim();
//          try {
//            OAuth2Authentication auth2Authentication = tokenService.loadAuthentication(token);
//            SecurityContextHolder.getContext().setAuthentication(auth2Authentication);
//            CustomUser user = (CustomUser) auth2Authentication.getPrincipal();
//            accessor.setUser(new WebSocketPrincipal(String.valueOf(user.getId())));
//            log.info("创建WebSocket链接用户：[{}]", Objects.requireNonNull(accessor.getUser()).getName());
//          } catch (Exception e) {
//            log.error("验证token异常", e);
//            throw new BusinessException(String.format("验证token异常[%s]", e.getMessage()));
//          }
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
      @Override
      public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
//        log.info("webSocket postSend");
      }

      /**
       * 在发送完成后调用，而不考虑引发的任何异常，从而允许正确的资源清理。
       * <p>只有当preSend成功时才会调用此命令。</p>
       *
       * @param message 消息
       * @param channel 通道
       * @param sent 该调用的返回值
       * @param ex 异常
       * @since 4.1
       */
      @Override
      public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
//        log.info("webSocket afterSendCompletion");
      }

      /**
       * 一旦调用了Receive，在实际检索消息之前调用。如果返回值为“false”，则不会检索任何消息。
       * <p>这仅适用于可轮询通道。</p>
       *
       * @param channel 通道
       */
      @Override
      public boolean preReceive(MessageChannel channel) {
        log.info("webSocket preReceive");
        return false;
      }

      /**
       * 在检索到消息后立即调用，但在将消息返回给调用方之前调用。
       * <p>如果需要，可以修改消息；@code null中止进一步的拦截器调用。</p>
       * <p>这仅适用于可轮询通道。</p>
       *
       * @param message 消息
       * @param channel 通道
       */
      @Override
      public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        log.info("webSocket postReceive");
        return null;
      }

      /**
       * 在接收完成后调用，而不考虑已引发的任何异常，从而允许正确的资源清理。
       * <p>请注意，只有当postReceive成功完成并返回true时，才会调用此函数。</p>
       *
       * @param message 消息
       * @param channel 通道
       * @param ex 异常
       * @since 4.1
       */
      @Override
      public void afterReceiveCompletion(Message<?> message, MessageChannel channel, Exception ex) {
        log.info("webSocket afterReceiveCompletion");
      }
    });
  }
}