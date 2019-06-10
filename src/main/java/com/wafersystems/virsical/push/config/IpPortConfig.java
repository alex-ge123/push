package com.wafersystems.virsical.push.config;

import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author tandk
 * @date 2019/6/10 15:43
 */
@Component
public class IpPortConfig implements ApplicationListener<WebServerInitializedEvent> {
  private int serverPort;

  private String serverIp;

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    this.serverPort = event.getWebServer().getPort();
  }

  public int getPort() {
    return this.serverPort;
  }

  public String getUrl() {
    InetAddress address = null;
    try {
      address = InetAddress.getLocalHost();
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    return "http://"+address.getHostAddress() +":"+this.serverPort;
  }
}
