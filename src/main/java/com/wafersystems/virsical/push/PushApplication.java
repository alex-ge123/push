package com.wafersystems.virsical.push;

import com.wafersystems.virsical.common.security.annotation.EnableCustomFeignClients;
import com.wafersystems.virsical.common.security.annotation.EnableCustomResourceServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import javax.annotation.PostConstruct;

/**
 * SpringBoot启动类
 *
 * @author tandk
 */
@SpringCloudApplication
@EnableCustomFeignClients
@EnableCustomResourceServer
public class PushApplication {

  public static void main(String[] args) {
    SpringApplication.run(PushApplication.class, args);
  }

  @Autowired
  private WebSocketMessageBrokerStats webSocketMessageBrokerStats;

  /**
   * 以毫秒为单位设置WebSocket信息日志记录频率，默认30分钟
   */
  @PostConstruct
  public void init() {
    // 1分钟打印WebSocket信息日志
    webSocketMessageBrokerStats.setLoggingPeriod(60000L);
  }
}