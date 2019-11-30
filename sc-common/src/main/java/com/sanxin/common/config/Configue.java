package com.sanxin.common.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.sanxin.common.dao.DatasourceMapper;
import com.sanxin.common.entity.Datasource;
import com.sanxin.common.entity.DatasourceExample;
import com.sanxin.common.utils.datasource.DBContextHolder;
import com.sanxin.common.utils.datasource.DruidDynamicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 启动类
 * @author: huangh
 * @since 2019-11-26 16:29
 */
@Component
public class Configue implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private DatasourceMapper datasourceMapper;

    /**
     * 服务器初始化
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 加载所有数据源
        List<Datasource> list =  datasourceMapper.selectByExample(new DatasourceExample());
        Map<Object, Object> targetDataSources = new HashMap<Object, Object>();
        for (Datasource datasource : list) {
            // 初始化数据源不做其他操作 切换数据源时进行建库建表
            DruidDynamicDataSource d = new DruidDynamicDataSource();
            DruidDataSource druidDataSource = d.createDataSource(datasource.getDatadriver(), datasource.getDataurl(), datasource.getDatauser(),
                    datasource.getDatapwd());
            targetDataSources.put(datasource.getDatakey(), druidDataSource);
            d.setTargetDataSources(targetDataSources);
        }

        if(event.getApplicationContext().getParent() == null){
            System.out.println("基础配置加载完毕");
        }
    }
}
