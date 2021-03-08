package com.wafersystems.virsical.push.config;

import com.wafersystems.virsical.common.core.constant.CommonConstants;
import com.wafersystems.virsical.common.core.exception.BusinessException;
import com.wafersystems.virsical.common.core.util.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 *
 * @author tandk
 * @date 2019-04-30
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * BusinessException
   *
   * @param e the e
   * @return R
   */
  @ExceptionHandler(BusinessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public R handleBusinessException(BusinessException e) {
    log.warn("【业务异常】{}", e.getMessage());
    return R.builder()
      .msg(e.getLocalizedMessage())
      .code(CommonConstants.FAIL)
      .build();
  }
}
