package com.wafersystems.virsical.push.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 校验参数与连接是否合法
 *
 * @author tandk
 * @date 2019-6-10
 */
@Setter
@Getter
@RefreshScope
@Configuration
@ConfigurationProperties(prefix = "check")
public class CheckProperties {

  /**
   * 是否开启检查
   */
  private boolean enable;

  /**
   * 参数正则
   */
  private String paramRegex;

  /**
   * url特殊字符过滤
   */
  private List<String> urlFilter;
}
