package com.wafersystems.virsical.push;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wafersystems.virsical.common.core.constant.SecurityConstants;
import com.wafersystems.virsical.common.core.tenant.TenantContextHolder;
import com.wafersystems.virsical.push.filter.PushFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

/**
 * 接口测试基类
 *
 * @author tandk
 * @date 2019-4-19
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(listeners = {WithSecurityContextTestExecutionListener.class
  , MockitoTestExecutionListener.class})
@WithMockUser(authorities = {"asd"})
public class BaseTest extends AbstractTestNGSpringContextTests {
  @Autowired
  private WebApplicationContext wac;

  private MockMvc mockMvc;

  @Autowired
  PushFilter pushFilter;

  /**
   * 在所有方法运行之前运行
   * 初始化mock redis
   */
  @BeforeSuite
  public void init() {
    MockitoAnnotations.initMocks(this);
  }

  /**
   * 在所有方法运行之后运行
   * 关闭mock redis
   */
  @AfterSuite
  public void end() {
  }

  /**
   * 在测试类中的Test开始运行前执行
   */
  @BeforeClass
  public void setup() {
    // 构建mockMvc，配置过滤器，添加Security过滤器链
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
      .addFilter(pushFilter)
      .build();
  }

  /**
   * 初始平台管理员化信息
   */
  private void setPlatformTenantContext() {
    TenantContextHolder.setTenantId(0);
    TenantContextHolder.setUserId(1);
    TenantContextHolder.setUsername("wafer");
    TenantContextHolder.setDeptId(0);
    TenantContextHolder.setTenantDomain("virsical.com");
  }

  /**
   * 初始化企业管理员信息
   */
  private void setAdminTenantContext() {
    TenantContextHolder.setTenantId(1);
    TenantContextHolder.setUserId(2);
    TenantContextHolder.setUsername("admin");
    TenantContextHolder.setDeptId(1);
    TenantContextHolder.setTenantDomain("wafersystems.com");
  }

  /**
   * post requestBuilder
   *
   * @param url     url
   * @param content 请求内容体
   * @return JSONObject
   * @throws Exception Exception
   */
  public JSONObject doMultipartPost(String url, String content, MockMultipartFile mockMultipartFile) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(url).file(mockMultipartFile);
    return doCall(requestBuilder, content, null, false, false);
  }

  /**
   * post requestBuilder
   *
   * @param url        url
   * @param content    请求内容体
   * @param params     参数
   * @param isInner    服务内部调用
   * @param isPlatform 是否平台管理员
   * @return JSONObject
   * @throws Exception Exception
   */
  public JSONObject doPost(String url, String content, MultiValueMap<String, String> params, Boolean isInner,
                           Boolean isPlatform) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(url);
    return doCall(requestBuilder, content, params, isInner, isPlatform);
  }

  /**
   * post requestBuilder（非内部调用，非平台管理员）
   *
   * @param url     url
   * @param content 请求内容体
   * @param params  参数
   * @return JSONObject
   * @throws Exception Exception
   */
  public JSONObject doPost(String url, String content, MultiValueMap<String, String> params) throws Exception {
    return this.doPost(url, content, params, false, false);
  }

  /**
   * get requestBuilder
   *
   * @param url        url
   * @param isInner    服务内部调用
   * @param isPlatform 是否平台管理员
   * @return JSONObject JSONObject
   * @throws Exception Exception
   */
  public JSONObject doGet(String url, Boolean isInner, Boolean isPlatform) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(url);
    return doCall(requestBuilder, null, null, isInner, isPlatform);
  }

  /**
   * get requestBuilder（非内部调用，非平台管理员）
   *
   * @param url url
   * @return JSONObject JSONObject
   * @throws Exception Exception
   */
  public JSONObject doGet(String url) throws Exception {
    return this.doGet(url, false, false);
  }

  /**
   * get requestBuilder（非内部调用，非平台管理员）
   *
   * @param url    url
   * @param params 参数
   * @return JSONObject JSONObject
   * @throws Exception Exception
   */
  public JSONObject doGet(String url, MultiValueMap<String, String> params) throws Exception {
    return this.doGet(url, false, false, params);
  }

  /**
   * get requestBuilder
   *
   * @param url        url
   * @param isInner    服务内部调用
   * @param isPlatform 是否平台管理员
   * @return JSONObject JSONObject
   * @throws Exception Exception
   */
  public JSONObject doGet(String url, Boolean isInner, Boolean isPlatform, MultiValueMap<String, String> params) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get(url);
    return doCall(requestBuilder, null, params, isInner, isPlatform);
  }

  /**
   * put requestBuilder
   *
   * @param url        url
   * @param content    content
   * @param isInner    服务内部调用
   * @param isPlatform 是否平台管理员
   * @return JSONObject
   * @throws Exception Exception
   */
  public JSONObject doPut(String url, String content, Boolean isInner, Boolean isPlatform) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.put(url);
    return doCall(requestBuilder, content, null, isInner, isPlatform);
  }

  /**
   * delete requestBuilder
   *
   * @param url        url
   * @param isInner    服务内部调用
   * @param isPlatform 是否平台管理员
   * @return JSONObject
   * @throws Exception Exception
   */
  public JSONObject doDelete(String url, Boolean isInner, Boolean isPlatform) throws Exception {
    MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.delete(url);
    return doCall(requestBuilder, null, null, isInner, isPlatform);
  }

  /**
   * 设置请求头，发起请求，返回处理结果
   *
   * @param requestBuilder requestBuilder
   * @param content        content
   * @param isInner        服务内部调用
   * @param isPlatform     是否平台管理员
   * @return JSONObject
   * @throws Exception Exception
   */
  private JSONObject doCall(MockHttpServletRequestBuilder requestBuilder,
                            String content, MultiValueMap<String, String> params,
                            Boolean isInner, Boolean isPlatform) throws Exception {
    return JSON.parseObject(doCallForString(requestBuilder, content, params, isInner, isPlatform));
  }

  /**
   * 设置请求头，发起请求，返回处理结果
   *
   * @param requestBuilder requestBuilder
   * @param content        content
   * @param isInner        服务内部调用
   * @param isPlatform     是否平台管理员
   * @return JSONObject
   * @throws Exception Exception
   */
  public String doCallForString(MockHttpServletRequestBuilder requestBuilder,
                                String content, MultiValueMap<String, String> params,
                                Boolean isInner, Boolean isPlatform) throws Exception {
    // 设置用户信息
    if (isPlatform != null) {
      if (isPlatform) {
        setPlatformTenantContext();
      } else {
        setAdminTenantContext();
      }
    }
    // 请求头加入oauth token
//    requestBuilder.header(HttpHeaders.AUTHORIZATION, OAuth2AccessToken.BEARER_TYPE + " " + token);
    if (isInner) {
      requestBuilder.header(SecurityConstants.FROM, SecurityConstants.FROM_IN);
    }
    if (StringUtils.isNotBlank(content)) {
      requestBuilder.contentType(MediaType.APPLICATION_JSON);
      requestBuilder.content(content);
    }
    if (params != null) {
      requestBuilder.params(params);
    }
    // 执行一个请求；
    ResultActions result = mockMvc.perform(requestBuilder);

    // 添加一个结果处理器，表示要对结果做点什么事情，比如此处使用MockMvcResultHandlers.print()输出整个响应结果信息。
    result.andDo(MockMvcResultHandlers.print());
    // 添加执行完成后的断言
//    result.andExpect(MockMvcResultMatchers.status().isOk());

    // 表示执行完成后返回相应的结果。
    MvcResult mvcResult = result.andReturn();
    return mvcResult.getResponse().getContentAsString();
  }

}
