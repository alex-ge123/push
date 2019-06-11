package com.wafersystems.virsical.push.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rabbitmq配置
 *
 * @author tandk
 * @date 2019/6/11 11:09
 */
@Configuration
@ConditionalOnExpression("${push.service.cluster} == true")
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

  public static final String FANOUT_EXCHANGE_PUSH = "fanout.exchange.push";

  public static final String FANOUT_QUEUE_PUSH = "fanout.queue.push";

  /**
   * Fanout模式
   * Fanout 就是广播模式或者订阅模式，给Fanout交换机发送消息，绑定了这个交换机的所有队列都收到这个消息。
   * 用户服务只负责发消息到交换机，其它服务消费消息需要绑定queue到交换机，并监听队列
   *
   * @return FanoutExchange
   */
  @Bean
  public FanoutExchange fanoutExchangePush() {
    return new FanoutExchange(FANOUT_EXCHANGE_PUSH);
  }

  /**
   * 队列
   *
   * @return Queue
   */
  @Bean
  public Queue fanoutQueuePush() {
    return new Queue(FANOUT_QUEUE_PUSH);
  }

  /**
   * 绑定队列到交换机
   *
   * @return Binding
   */
  @Bean
  public Binding fanoutBindingPush() {
    return BindingBuilder.bind(fanoutQueuePush()).to(fanoutExchangePush());
  }

}