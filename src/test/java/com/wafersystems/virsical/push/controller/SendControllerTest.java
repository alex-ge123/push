package com.wafersystems.virsical.push.controller;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.wafersystems.virsical.common.core.constant.CommonConstants;
import com.wafersystems.virsical.common.core.constant.PushMqConstants;
import com.wafersystems.virsical.common.core.constant.enums.MsgTypeEnum;
import com.wafersystems.virsical.common.core.dto.MessageDTO;
import com.wafersystems.virsical.push.BaseTest;
import com.wafersystems.virsical.push.config.WebSocketPrincipal;
import com.wafersystems.virsical.push.handler.CheckTokenHandler;
import com.wafersystems.virsical.push.handler.CustomEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.security.Principal;

/**
 * SendControllerTest
 *
 * @author tandk
 * @date 2019/5/15 15:54
 */
@Slf4j
public class SendControllerTest extends BaseTest {

  @Autowired
  AmqpTemplate amqpTemplate;

  @Autowired
  SendController sendController;

  @Test
  public void sendFanout() throws Exception {
    String url = "/send/fanout";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("product", "map");
    params.add("msgType", "ONE");
    params.add("clientId", "123");
    params.add("msg", "");
    JSONObject jsonObjectFail = doPost(url, null, params);
    Assert.assertEquals(jsonObjectFail.get("code"), CommonConstants.FAIL);

    params.add("msg", "test");
    JSONObject jsonObject = doPost(url, null, params);
    Assert.assertEquals(jsonObject.get("code"), CommonConstants.SUCCESS);
  }

  @Test
  public void sendAll() throws Exception {
    String url = "/send/all";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("msg", "");
    JSONObject jsonObjectFail = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObjectFail.get("code"), CommonConstants.FAIL);

    params.add("msg", "test");
    JSONObject jsonObject = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObject.get("code"), CommonConstants.SUCCESS);
  }

  @Test
  public void sendTopic() throws Exception {
    String url = "/send/topic";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("product", "map");
    params.add("msg", "");
    JSONObject jsonObjectFail = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObjectFail.get("code"), CommonConstants.FAIL);

    params.add("msg", "test");
    JSONObject jsonObject = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObject.get("code"), CommonConstants.SUCCESS);
  }

  @Test
  public void sendOne() throws Exception {
    String url = "/send/one";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("clientId", "");
    params.add("msg", "");
    JSONObject jsonObjectFail = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObjectFail.get("code"), CommonConstants.FAIL);

    params.add("clientId", "123");
    params.add("msg", "test");
    JSONObject jsonObject = doPost(url, null, params, true, false);
    Assert.assertEquals(jsonObject.get("code"), CommonConstants.SUCCESS);
  }

  @Test
  public void testReceiver() {
    MessageDTO messageDTO = new MessageDTO();
    messageDTO.setClientId("123");
    messageDTO.setMsgType(MsgTypeEnum.ALL.name());
    amqpTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_MESSAGE, "", JSONUtil.toJsonStr(messageDTO));

    messageDTO.setMsgType(MsgTypeEnum.ONE.name());
    amqpTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_MESSAGE, "", JSONUtil.toJsonStr(messageDTO));

    messageDTO.setMsgType("TOPIC");
    amqpTemplate.convertAndSend(PushMqConstants.EXCHANGE_FANOUT_PUSH_MESSAGE, "", JSONUtil.toJsonStr(messageDTO));
  }

  @Test
  public void clientSendOne() {
    sendController.clientSendOne("test", "123");
  }

  @Test
  public void testWebSocket() throws Exception {
    String url = "/ws/info";
    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("t", "1614778320258");
    JSONObject jsonObject = doGet(url, params);
    Assert.assertEquals(jsonObject.get("websocket"), true);

    String url1 = "/ws/info-(123)";
    JSONObject jsonObjectFail = doGet(url1, params);
    Assert.assertEquals(jsonObjectFail.get("code"), CommonConstants.FAIL);
  }

  @Test
  public void testWebSocketPrincipal() {
    WebSocketPrincipal webSocketPrincipal = new WebSocketPrincipal("admin");
    Assert.assertEquals(webSocketPrincipal.getName(), "admin");
  }

  @Autowired
  RestOperations restTemplate;

  @Test
  public void testCheckTokenHandler() {
    CheckTokenHandler checkTokenHandler = new CheckTokenHandler(restTemplate);
    try {
      checkTokenHandler.checkToken("123456");
    } catch (IllegalStateException e) {
      Assert.assertTrue(true);
    }

  }

  @Autowired
  CustomEventHandler customEventHandler;

  @MockBean
  StompHeaderAccessor stompHeaderAccessor;

  @Test
  public void testCustomEventHandler(){
    Mockito.when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.CONNECT);

    Principal principal = new WebSocketPrincipal("zhangsan");
    Mockito.when(stompHeaderAccessor.getUser()).thenReturn(principal);
    stompHeaderAccessor.setNativeHeader("token","111");
    stompHeaderAccessor.setNativeHeader("clientId","123");
    customEventHandler.handler(stompHeaderAccessor);

    Mockito.when(stompHeaderAccessor.getCommand()).thenReturn(StompCommand.DISCONNECT);
    customEventHandler.handler(stompHeaderAccessor);
  }
}
