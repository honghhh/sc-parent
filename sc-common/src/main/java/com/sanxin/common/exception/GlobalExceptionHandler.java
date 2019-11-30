package com.sanxin.common.exception;

import com.alibaba.fastjson.JSONException;
import com.sanxin.common.rest.GetRest;
import com.sanxin.common.rest.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * 全局异常类
 * @author: huangh
 * @since 2019-11-26 16:31
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * IO异常
     */
    @ExceptionHandler(value = {IOException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResponse exception(Exception e) {
        e.printStackTrace();
        return GetRest.getFail("流处理异常");
    }

    /**
     * 处理json异常
     */
    @ExceptionHandler(JSONException.class)
    @ResponseBody
    public RestResponse handleBusinessException(JSONException e){
        e.printStackTrace();
        return GetRest.getFail(e.getMessage());
    }

    /**
     * 处理接口异常
     */
    @ExceptionHandler(ThrowJsonException.class)
    @ResponseBody
    public RestResponse handleBusinessException(ThrowJsonException e){
        return GetRest.getFail(e.getMessage());
    }

    /**
     * 处理页面异常
     */
    @ExceptionHandler(ThrowPageException.class)
    @ResponseBody
    public ModelAndView showPageException(ThrowPageException e){
        ModelAndView view = new ModelAndView("exception");
        return view;
    }

    /**
     * 所有异常
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RestResponse errorResponse(Exception e) {
        e.printStackTrace();
        return GetRest.getFail("请求错误");
    }
}
