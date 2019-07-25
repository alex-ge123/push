package com.wafersystems.virsical.push.config;

import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置
 *
 * @author tandk
 * @date 2019/6/11 11:09
 */
@Configuration
public class RabbitMqConfig {

  /**
   * 配置消息转换器转Json
   *
   * @return 消息转换器
   */
  @Bean
  public MessageConverter messageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Value("${push.fanout.queue}")
  public String pushFanoutQueue;

  /**
   * 推送消息广播交换机
   *
   * @return FanoutExchange
   */
  @Bean
  public FanoutExchange messageFanoutExchange() {
    return new FanoutExchange(PushMqConstants.EXCHANGE_FANOUT_PUSH_MESSAGE);
  }

  /**
   * WebSocket连接消息广播交换机
   *
   * @return FanoutExchange
   */
  @Bean
  public FanoutExchange connectFanoutExchange() {
    return new FanoutExchange(PushMqConstants.EXCHANGE_FANOUT_PUSH_CONNECT);
  }

  /**
   * 推送消息队列
   *
   * @return Queue
   */
  @Bean
  public Queue messageFanoutQueue() {
    return new Queue(pushFanoutQueue);
  }

  /**
   * 绑定推送消息队列到交换机
   *
   * @return Binding
   */
  @Bean
  public Binding messageFanoutBinding() {
    return BindingBuilder.bind(messageFanoutQueue()).to(messageFanoutExchange());
  }

}