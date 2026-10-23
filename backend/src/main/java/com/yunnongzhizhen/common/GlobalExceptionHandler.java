package com.yunnongzhizhen.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理，统一返回 {code, message, data}。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常；401/403 同步 HTTP 状态，便于前端与集成测试识别。
     */
    @ExceptionHandler(BusinessException.class)
    public org.springframework.http.ResponseEntity<ApiResult<Void>> handleBusiness(BusinessException e) {
        HttpStatus status = HttpStatus.OK;
        if (e.getCode() == 401) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (e.getCode() == 403) {
            status = HttpStatus.FORBIDDEN;
        }
        return org.springframework.http.ResponseEntity.status(status)
                .body(ApiResult.fail(e.getCode(), e.getMessage()));
    }

    /**
     * 处理参数校验异常。
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResult<Void> handleValid(Exception e) {
        String msg = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            msg = manv.getBindingResult().getFieldError().getDefaultMessage();
        } else if (e instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            msg = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return ApiResult.fail(400, msg);
    }

    /**
     * 处理未认证。
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<Void> handleAuth(AuthenticationException e) {
        return ApiResult.fail(401, e.getMessage() != null ? e.getMessage() : "未登录或登录已失效");
    }

    /**
     * 处理凭证错误。
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiResult<Void> handleBadCredentials(BadCredentialsException e) {
        return ApiResult.fail(401, "用户名或密码错误");
    }

    /**
     * 处理无权限。
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResult<Void> handleAccessDenied(AccessDeniedException e) {
        return ApiResult.fail(403, "无权限访问");
    }

    /**
     * 处理未预期异常。
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResult<Void> handleOther(Exception e) {
        log.error("系统异常", e);
        return ApiResult.fail(500, "系统繁忙，请稍后重试");
    }
}
