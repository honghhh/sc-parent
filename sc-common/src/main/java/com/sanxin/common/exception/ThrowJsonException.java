package com.sanxin.common.exception;

/**
 * 业务异常类 抛接口提示
 * @author: huangh
 * @since 2019-11-26 16:34
 */
public class ThrowJsonException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = -100509897248249450L;

    public ThrowJsonException(String arg0){
        super(arg0);
    }

    public ThrowJsonException(){
        super();
    }

    public ThrowJsonException(Throwable arg0){
        super(arg0);
    }
}
