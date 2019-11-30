package com.sanxin.common.exception;

/**
 * 业务异常类 抛页面提示
 * @author: huangh
 * @since 2019-11-26 16:34
 */
public class ThrowPageException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = -100509897248249450L;

    public ThrowPageException(String arg0){
        super(arg0);
    }

    public ThrowPageException(){
        super();
    }

    public ThrowPageException(Throwable arg0){
        super(arg0);
    }
}
