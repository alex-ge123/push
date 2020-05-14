package com.wafersystems.virsical.push.filter;

import cn.hutool.core.util.StrUtil;
import com.wafersystems.virsical.common.core.constant.CommonConstants;
import com.wafersystems.virsical.common.core.util.R;
import com.wafersystems.virsical.common.core.util.WebUtils;
import com.wafersystems.virsical.push.common.PushConstants;
import lombok.extern.slf4j.Slf4j;
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

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
    throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    String url = request.getServletPath();
    // 过滤ws
    if (url.startsWith(PushConstants.WS_URL_PREFIX)) {
      String t = request.getParameter("t");
      boolean paramValid = StrUtil.isNotBlank(t) && !t.matches(PushConstants.WS_PARAM_REGEX);
      boolean urlValid = StrUtil.containsAny(url, "%", "_", "-", "(", ")");
      if (paramValid || urlValid) {
        log.error("{} > 参数错误：{}", url, t);
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        WebUtils.renderJson(response,
          R.builder().code(CommonConstants.FAIL).msg("").build());
        return;
      }
    }
    filterChain.doFilter(request, response);
  }
}
