package com.wafersystems.virsical.push.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 推送服务部署方式配置属性
 *
 * @author tandk
 * @date 2019-6-10
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "push.service")
public class DeployProperties {

  /**
   * 推送服务部署方式是否为集群
   */
  private boolean cluster;
}
