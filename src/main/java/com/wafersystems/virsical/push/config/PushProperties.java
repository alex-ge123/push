package com.wafersystems.virsical.push.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * PUSH配置类
 *
 * @author tandk
 * @date 2019-6-10
 */
@Data
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "push")
public class PushProperties {

  /**
   * 发送建立连接与关闭连接MQ消息开关
   */
  private boolean sendConnectMq;

}
