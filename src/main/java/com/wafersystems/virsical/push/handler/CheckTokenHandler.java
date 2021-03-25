package com.wafersystems.virsical.push.handler;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import com.wafersystems.virsical.common.core.exception.BusinessException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestOperations;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 校验token
 *
 * @author tandk
 * @date 2020/5/29 16:57
 */
@Component
public class CheckTokenHandler {
  private final Log logger = LogFactory.getLog(getClass());

  private final RestOperations restTemplate;

  @Value("${security.oauth2.resource.token-info-uri:http://virsical-auth/oauth/check_token}")
  private String checkTokenEndpointUrl;

  @Value("${security.oauth2.client.client-id:test}")
  private String clientId;

  @Value("${security.oauth2.client.client-secret:test}")
  private String clientSecret;

  @Autowired
  public CheckTokenHandler(RestOperations restTemplate) {
    this.restTemplate = restTemplate;
  }

  /**
   * checkToken
   *
   * @param token token
   * @return map
   */
  public Map checkToken(String token) {
    // 验证token
    if (StrUtil.isBlank(token)) {
      throw new BusinessException("[Token不存在]: " + token);
    }
    MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
    formData.add("token", token);
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
    Map map = postForMap(checkTokenEndpointUrl, formData, headers);
    String error = "error";
    String active = "active";
    if (map.containsKey(error) || !Boolean.TRUE.equals(map.get(active))) {
      throw new BusinessException("[CheckToken无效]: " + token + ", [Result]: " + map.toString());
    }
    return map;
  }

  private String getAuthorizationHeader(String clientId, String clientSecret) {

    if (clientId == null || clientSecret == null) {
      logger.warn("Null Client ID or Client Secret detected. "
        + "Endpoint that requires authentication will reject request with 401 error.");
    }
    String creds = String.format("%s:%s", clientId, clientSecret);
    return "Basic " + Base64.encode(creds.getBytes(StandardCharsets.UTF_8));
  }

  private Map postForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers) {
    if (headers.getContentType() == null) {
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }
    return restTemplate.exchange(path, HttpMethod.POST,
      new HttpEntity<>(formData, headers), Map.class).getBody();
  }
}
