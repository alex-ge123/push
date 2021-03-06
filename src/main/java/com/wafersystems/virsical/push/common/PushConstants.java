package com.wafersystems.virsical.push.common;

import lombok.experimental.UtilityClass;

/**
 * 常量
 *
 * @author tandk
 * @date 2019/6/10 10:45
 */
@UtilityClass
public final class PushConstants {

  /**
   * 广播群发，推送所有目的地
   */
  public static final String PUSH_ALL_DESTINATION = "/topic/all";

  /**
   * 广播群发，推送指定产品目的地
   */
  public static final String PUSH_TOPIC_DESTINATION = "/topic/";

  /**
   * 广播订阅，推送指定用户目的地
   */
  public static final String PUSH_ONE_DESTINATION = "/one";

  /**
   * ws参数正则
   */
  public static final String WS_PARAM_REGEX = "\\d{13}";

  /**
   * ws前缀
   */
  public static final String WS_URL_PREFIX = "/ws";
}
