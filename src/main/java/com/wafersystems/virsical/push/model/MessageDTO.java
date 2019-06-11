package com.wafersystems.virsical.push.model;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息传输对象
 *
 * @author tandk
 * @date 2019/4/3 16:35
 */
@Data
public class MessageDTO {

  /**
   * 消息id
   */
  private String msgId;

  /**
   * 消息发送时间
   */
  private Long msgTime;

  /**
   * 消息类型（ONE单条(点对点)|BATCH批量(广播)）
   */
  private String msgType;

  /**
   * 消息动作（ADD|DELETE|UPDATE|NONE: 增|删|改|无）
   */
  private String msgAction;

  /**
   * 操作人用户id
   */
  private Integer userId;

  /**
   * 终端id
   */
  private String clientId;

  /**
   * 消息体
   */
  private Serializable data;
}
