package com.wafersystems.virsical.push.model;

import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

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
   * 消息所属产品（会议-smartmeeting，工位-smartworkspace，地图-map）
   */
  private String product;

  /**
   * 消息类型（ONE单条(点对点)|BATCH批量|ALL(广播)）
   */
  private String msgType;

  /**
   * 消息动作（ADD|DELETE|UPDATE|SHOW|NONE: 增|删|改|展示|无）
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

  public MessageDTO() {
  }

  /**
   * constructor
   *
   * @param userId    userId
   * @param clientId  clientId
   * @param product   消息所属产品（会议-smartmeeting，工位-smartworkspace，地图-map）
   * @param msgType   消息类型（ONE单条(点对点)|BATCH批量|ALL(广播)）
   * @param msgAction 消息动作（ADD|DELETE|UPDATE|NONE: 增|删|改|无）
   * @param data      消息体
   */
  public MessageDTO(Integer userId, String clientId, String product, String msgType, String msgAction,
                    Serializable data) {
    this.msgId = UUID.randomUUID().toString();
    this.msgTime = System.currentTimeMillis();
    this.userId = userId;
    this.clientId = clientId;
    this.product = product;
    this.msgType = msgType;
    this.msgAction = msgAction;
    this.data = data;
  }
}
