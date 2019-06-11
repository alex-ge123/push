package com.wafersystems.virsical.push.config;

import lombok.AllArgsConstructor;

import java.security.Principal;

/**
 * @author tandk
 * @date 2019/6/2 10:53
 */
@AllArgsConstructor
public class WebSocketPrincipal implements Principal {

  private String name;

  /**
   * Returns the name of this principal.
   *
   * @return the name of this principal.
   */
  @Override
  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }
}
