package com.hs.fastService;

import com.hs.fastService.entities.response.BaseResponse;
import com.hs.fastService.enums.BaseErrorMsg;
import com.hs.fastService.exceptions.ResponseException;
import com.hs.fastService.util.LogUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public BaseResponse handleException(Throwable exception) {
        if (exception instanceof DataIntegrityViolationException) {
            return new BaseResponse(BaseErrorMsg.DATA_ALREADY_EXISTED);
        } else if (exception instanceof MissingServletRequestParameterException) {
            return new BaseResponse(BaseErrorMsg.NOT_REQUIRED_PARAMETER.code, exception.getMessage());
        }else if (exception instanceof BindingResult) {
            return handleNotValidException((BindingResult) exception);
        } else if (exception instanceof MethodArgumentNotValidException) {
            return handleNotValidException(((MethodArgumentNotValidException) exception).getBindingResult());
        } else if (exception instanceof ResponseException) {
            ResponseException e = (ResponseException)exception;
            return new BaseResponse(e.getCode(), e.getMessage());
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return new BaseResponse(BaseErrorMsg.CHECK_FAILURE.code, "不支持该请求方法：" + exception.getMessage());
        } else if (exception instanceof NumberFormatException) {
            LogUtil.error("异常！！：", exception);
            return new BaseResponse(BaseErrorMsg.CHECK_FAILURE.code, "数据格式化异常！");
        }
        LogUtil.error("异常！！：", exception);
        Throwable t = exception;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return new BaseResponse(BaseErrorMsg.SERVER_ERROR.code, t.getMessage());
    }

    private BaseResponse handleNotValidException(BindingResult bindingResult) {
        List<ObjectError> errors = bindingResult.getAllErrors();
        ObjectError error = errors.get(0);
        String defaultMessage = error.getDefaultMessage();
        return new BaseResponse(-1, defaultMessage);
    }
}
