package com.wafersystems.virsical.push.config;

/**
 * 常量
 *
 * @author tandk
 * @date 2019/6/10 10:45
 */
public interface PushConstants {

  String SIMP_SESSION_ID = "simpSessionId";

  /**
   * 存储推送服务客户端id缓存key
   */
  String PUSH_SERVICE_CLIENTID = "PUSH_SERVICE_CLIENTID:";

  /**
   * 存储推送服务simpSessionId缓存key
   */
  String PUSH_SERVICE_SIMPSESSIONID = "PUSH_SERVICE_SIMPSESSIONID";

  /**
   * 广播群发，推送所有目的地
   */
  String PUSH_ALL_DESTINATION = "/topic/to-all";

  /**
   * 广播订阅，推送用户目的地
   */
  String PUSH_USER_DESTINATION = "/to-user";
}
