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

import java.util.HashMap;
import java.util.Map;

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
    Map<String, Object> map = new HashMap<>(1);
    // 队列中的消息未被消费则一个小时后过期
    map.put("x-message-ttl", 3600000);
    //参数1：队列名称  参数2：是否持久化  参数3：排他性  参数4：是否自动删除  参数5：过期时间
    return new Queue(pushFanoutQueue, true, false, false, map);
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