package com.sanxin.common.utils.datasource;

import java.util.HashMap;
import java.util.Map;

/**
 * 当前正在使用的数据源信息的线程上线文
 * @author: huangh
 * @since 2019-11-28 9:11
 */
public class DBContextHolder {

    /** 数据源的KEY */
    public static final String DATASOURCE_KEY = "DATASOURCE_KEY";
    /** 数据源的URL */
    public static final String DATASOURCE_URL = "DATASOURCE_URL";
    /** 数据源的驱动 */
    public static final String DATASOURCE_DRIVER = "DATASOURCE_DRIVER";
    /** 数据源的用户名 */
    public static final String DATASOURCE_USERNAME = "DATASOURCE_USERNAME";
    /** 数据源的密码 */
    public static final String DATASOURCE_PASSWORD = "DATASOURCE_PASSWORD";

    private static final ThreadLocal<Map<String, Object>> contextHolder = new ThreadLocal<Map<String, Object>>();

    public static void setDBType(Map<String, Object> dataSourceConfigMap) {
        contextHolder.set(dataSourceConfigMap);
    }

    public static Map<String, Object> getDBType() {
        Map<String, Object> dataSourceConfigMap = contextHolder.get();
        if (dataSourceConfigMap == null) {
            dataSourceConfigMap = new HashMap<String, Object>();
        }
        return dataSourceConfigMap;
    }

    public static void clearDBType() {
        contextHolder.remove();
    }
}
