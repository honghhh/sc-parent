package com.sanxin.school.controller;

import com.sanxin.common.dao.UserMapper;
import com.sanxin.common.entity.User;
import com.sanxin.common.utils.datasource.DBContextHolder;
import com.sanxin.common.utils.datasource.DruidDynamicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试控制器
 * @author: huangh
 * @since 2019-11-28 9:32
 */
@Controller
public class TestController {

    @Autowired
    private UserMapper userMapper;

    @RequestMapping(value = "/test")
    public void test(){
        User user = userMapper.selectByPrimaryKey(1);
        System.out.println(user.getLogin());

        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DBContextHolder.DATASOURCE_KEY, "localhost");
        map.put(DBContextHolder.DATASOURCE_DRIVER, "com.mysql.jdbc.Driver");
        map.put(DBContextHolder.DATASOURCE_URL,
                "jdbc:mysql://localhost:3306/scschool1data?useUnicode=true&characterEncoding=UTF-8");
        map.put(DBContextHolder.DATASOURCE_USERNAME, "root");
        map.put(DBContextHolder.DATASOURCE_PASSWORD, "123456");
        DBContextHolder.setDBType(map);

        DruidDynamicDataSource d = new DruidDynamicDataSource();
        d.determineCurrentLookupKey();
        user = userMapper.selectByPrimaryKey(1);
        System.out.println(user.getLogin());

        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put(DBContextHolder.DATASOURCE_KEY, "localhost");
        map1.put(DBContextHolder.DATASOURCE_DRIVER, "com.mysql.jdbc.Driver");
        map1.put(DBContextHolder.DATASOURCE_URL,
                "jdbc:mysql://localhost:3306/scschool2data?useUnicode=true&characterEncoding=UTF-8");
        map1.put(DBContextHolder.DATASOURCE_USERNAME, "root");
        map1.put(DBContextHolder.DATASOURCE_PASSWORD, "123456");
        DBContextHolder.clearDBType();
        DBContextHolder.setDBType(map1);


        user = userMapper.selectByPrimaryKey(1);
        System.out.println(user.getLogin());
    }

    @RequestMapping(value = "/test1")
    public void test1(){
        User user = userMapper.selectByPrimaryKey(1);
        System.out.println(user.getLogin());

        // 切换数据源时检测是否已经创建数据库数据表没有则创建
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(DBContextHolder.DATASOURCE_KEY, "localhost");
        map.put(DBContextHolder.DATASOURCE_DRIVER, "com.mysql.jdbc.Driver");
        map.put(DBContextHolder.DATASOURCE_URL,
                "jdbc:mysql://localhost:3306/scschool2data?useUnicode=true&characterEncoding=UTF-8");
        map.put(DBContextHolder.DATASOURCE_USERNAME, "root");
        map.put(DBContextHolder.DATASOURCE_PASSWORD, "123456");
        DBContextHolder.setDBType(map);
        DruidDynamicDataSource d = new DruidDynamicDataSource();
        d.determineCurrentLookupKey();

        user = userMapper.selectByPrimaryKey(1);
        System.out.println(user.getLogin());
    }
}
