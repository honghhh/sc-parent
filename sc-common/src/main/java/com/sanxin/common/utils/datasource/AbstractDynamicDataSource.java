package com.sanxin.common.utils.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;

/**
 * 动态数据源配置类
 * @author: huangh
 * @since 2019-11-28 9:12
 */
public abstract class AbstractDynamicDataSource<T extends DataSource> extends AbstractRoutingDataSource
        implements ApplicationContextAware {

    /**
     * 日志
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 默认的数据源KEY，和spring配置文件中的id=druidDynamicDataSource的bean中配置的默认数据源key保持一致
     */
    protected static final String DEFAULT_DATASOURCE_KEY = "dataSource";

    /**
     * spring容器上下文
     */
    private static ApplicationContext ctx;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ctx = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return ctx;
    }

    public static Object getBean(String name) {
        return ctx.getBean(name);
    }

    /**
     * 创建数据源
     * @param driverClassName 数据库驱动名称
     * @param url             连接地址
     * @param username        用户名
     * @param password        密码
     * @return 数据源{@link T}
     * @Author : ll. create at 2017年3月27日 下午2:44:34
     */
    public abstract T createDataSource(String driverClassName, String url, String username, String password);
}
