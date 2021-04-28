package com.wafersystems.virsical.push.filter;

import cn.hutool.core.util.StrUtil;
import com.wafersystems.virsical.common.core.constant.CommonConstants;
import com.wafersystems.virsical.common.core.util.R;
import com.wafersystems.virsical.common.core.util.WebUtils;
import com.wafersystems.virsical.push.common.PushConstants;
import com.wafersystems.virsical.push.config.CheckProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 全局过滤器
 *
 * @author tandk
 * @date 2020/5/14
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PushFilter extends GenericFilterBean {

  @Autowired
  private CheckProperties checkProperties;

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    if (checkProperties.isEnable()) {
      String url = request.getServletPath();
      // 过滤ws，校验path、t参数、referer是否合法
      if (url.startsWith(PushConstants.WS_URL_PREFIX)) {
        String t = request.getParameter("t");
        boolean paramInvalid = StrUtil.isNotBlank(t) && !t.matches(checkProperties.getParamRegex());
        boolean urlInvalid = StrUtil.containsAny(url, checkProperties.getUrlFilter().toArray(new String[]{}));
        String referer = request.getHeader("referer");
        String origin = request.getHeader("origin");
        boolean refererInvalid = false;
        if (StrUtil.isNotBlank(referer) && StrUtil.isNotBlank(origin) && !referer.startsWith(origin)) {
          refererInvalid = true;
        }
        if (paramInvalid || urlInvalid || refererInvalid) {
          log.error("地址无效[{}]：[{}]，参数无效[{}]：[{}]，referer无效[{}]：[{}]",
            urlInvalid, url, paramInvalid, t, refererInvalid, referer);
          WebUtils.renderJson(HttpStatus.BAD_REQUEST.value(), response,
            R.builder().code(CommonConstants.FAIL).msg("").build());
          return;
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
