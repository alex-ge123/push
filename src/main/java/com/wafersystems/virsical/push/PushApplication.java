package com.wafersystems.virsical.push;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.WebSocketMessageBrokerStats;

import javax.annotation.PostConstruct;

/**
 * SpringBoot启动类
 *
 * @author tandk
 */
@SpringBootApplication
public class PushApplication {

  public static void main(String[] args) {
    SpringApplication.run(PushApplication.class, args);
  }

  @Autowired
  private WebSocketMessageBrokerStats webSocketMessageBrokerStats;

  @PostConstruct
  public void init() {
    // desired time in millis
    webSocketMessageBrokerStats.setLoggingPeriod(10 * 1000);
  }
}