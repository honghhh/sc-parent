package com.sanxin.common.utils.datasource;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;
import com.sanxin.common.utils.properties.PropertiesUtil;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Druid数据源
 * @author: huangh
 * @since 2019-11-28 9:14
 */
public class DruidDynamicDataSource extends AbstractDynamicDataSource<DruidDataSource> {

    private boolean testWhileIdle = true;
    private boolean testOnBorrow = false;
    private boolean testOnReturn = false;

    /**
     * 是否打开连接泄露自动检测
     */
    private boolean removeAbandoned = false;

    /**
     * 连接长时间没有使用，被认为发生泄露时长
     */
    private long removeAbandonedTimeoutMillis = 300 * 1000;

    /**
     * 发生泄露时是否需要输出 log，建议在开启连接泄露检测时开启，方便排错
     */
    private boolean logAbandoned = false;

    /**
     * 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，使用oracle时可以设定此值。
     */
    // private int maxPoolPreparedStatementPerConnectionSize = -1;

    /**
     * 配置监控统计拦截的filters，监控统计："stat" 防SQL注入："wall" 组合使用： "stat,wall"
     */
    private String filters;
    private List<Filter> filterList;

    /**
     * 数据源KEY-VALUE键值对
     */
    private Map<Object, Object> targetDataSources;

    /**
     * 创建数据源，这里创建的数据源是带有连接池属性的
     */
    @Override
    public DruidDataSource createDataSource(String driverClassName, String url, String username,
                                            String password) {
        DruidDataSource parent = (DruidDataSource) AbstractDynamicDataSource.getApplicationContext().getBean(
                AbstractDynamicDataSource.DEFAULT_DATASOURCE_KEY);
        DruidDataSource ds = new DruidDataSource();
        ds.setUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        ds.setInitialSize(parent.getInitialSize());
        ds.setMinIdle(parent.getMinIdle());
        ds.setMaxActive(parent.getMaxActive());
        ds.setMaxWait(parent.getMaxWait());
        ds.setTimeBetweenConnectErrorMillis(parent.getTimeBetweenConnectErrorMillis());
        ds.setTimeBetweenEvictionRunsMillis(parent.getTimeBetweenEvictionRunsMillis());
        ds.setMinEvictableIdleTimeMillis(parent.getMinEvictableIdleTimeMillis());

        ds.setValidationQuery(parent.getValidationQuery());
        ds.setTestWhileIdle(testWhileIdle);
        ds.setTestOnBorrow(testOnBorrow);
        ds.setTestOnReturn(testOnReturn);

        ds.setRemoveAbandoned(removeAbandoned);
        ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
        ds.setLogAbandoned(logAbandoned);

        // 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
        ds.setMaxPoolPreparedStatementPerConnectionSize(parent
                .getMaxPoolPreparedStatementPerConnectionSize());

        if (StringUtils.isNotBlank(filters)) {
            try {
                ds.setFilters(filters);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        addFilterList(ds);
        return ds;
    }

    public void addFilterList(DruidDataSource ds) {
        if (filterList != null) {
            List<Filter> targetList = ds.getProxyFilters();
            for (Filter add : filterList) {
                boolean found = false;
                for (Filter target : targetList) {
                    if (add.getClass().equals(target.getClass())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    targetList.add(add);
                }
            }
        }
    }

    /**
     * 设置数据源
     * @param targetDataSources the targetDataSources to set
     * @return
     */
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        this.targetDataSources = targetDataSources;
        super.setTargetDataSources(targetDataSources);
        // afterPropertiesSet()方法调用时用来将targetDataSources的属性写入resolvedDataSources中的
        super.afterPropertiesSet();
    }

    /**
     * 设置系统当前使用的数据源
     * <p>数据源为空或者为0时，自动切换至默认数据源，即在配置文件中定义的默认数据源
     * @see org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource#determineCurrentLookupKey()
     */
    @Override
    public Object determineCurrentLookupKey() {
        logger.info("【设置系统当前使用的数据源】");
        Map<String, Object> configMap = DBContextHolder.getDBType();
        logger.info("【当前数据源配置为：{}】", configMap);
        System.out.println("【当前数据源配置为：{}】" + configMap);
        if (MapUtils.isEmpty(configMap)) {
            // 使用默认数据源
            return DEFAULT_DATASOURCE_KEY;
        }
        // 判断数据源是否需要初始化
        this.verifyAndInitDataSource();
        logger.info("【切换至数据源：{}】", configMap);
        return configMap.get(DBContextHolder.DATASOURCE_KEY);
    }

    /**
     * 判断数据源是否需要初始化
     * @Author : ll. create at 2017年3月27日 下午3:57:43
     */
    public void verifyAndInitDataSource() {
        if (targetDataSources == null) {
            targetDataSources = new HashMap<Object, Object>();
        }

        Map<String, Object> configMap = DBContextHolder.getDBType();
        Object obj = this.targetDataSources.get(configMap.get(DBContextHolder.DATASOURCE_KEY));
        if (obj != null) {
            return;
        }

        logger.info("【初始化数据源】");
        DruidDataSource datasource = this.createDataSource(configMap.get(DBContextHolder.DATASOURCE_DRIVER)
                        .toString(), configMap.get(DBContextHolder.DATASOURCE_URL).toString(),
                configMap.get(DBContextHolder.DATASOURCE_USERNAME).toString(),
                configMap.get(DBContextHolder.DATASOURCE_PASSWORD).toString());
        this.addTargetDataSource(configMap.get(DBContextHolder.DATASOURCE_KEY).toString(),
                datasource);

        // 测试数据源连接是否有效
        Boolean flag = testDatasource(configMap.get(DBContextHolder.DATASOURCE_DRIVER)
                        .toString(), configMap.get(DBContextHolder.DATASOURCE_URL).toString(),
                configMap.get(DBContextHolder.DATASOURCE_USERNAME).toString(),
                configMap.get(DBContextHolder.DATASOURCE_PASSWORD).toString());

        // 如果连不上才去创建数据源
        if (!flag) {
            // 自动创建数据库 数据表
            try {
                createDatabase();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 检查数据库连接
            System.out.println(testDatasource(configMap.get(DBContextHolder.DATASOURCE_DRIVER)
                            .toString(), configMap.get(DBContextHolder.DATASOURCE_URL).toString(),
                    configMap.get(DBContextHolder.DATASOURCE_USERNAME).toString(),
                    configMap.get(DBContextHolder.DATASOURCE_PASSWORD).toString()));
        }
    }

    /**
     * 往数据源key-value键值对集合添加新的数据源
     * @param key        新的数据源键
     * @param dataSource 新的数据源
     * @Author : ll. create at 2017年3月27日 下午2:56:49
     */
    public void addTargetDataSource(String key, DruidDataSource dataSource) {
        this.targetDataSources.put(key, dataSource);
        super.setTargetDataSources(this.targetDataSources);
        // afterPropertiesSet()方法调用时用来将targetDataSources的属性写入resolvedDataSources中的
        super.afterPropertiesSet();
    }

    /**
     * 测试数据源连接是否有效
     */
    public boolean testDatasource(String driveClass, String url, String username, String password) {
        try {
            Class.forName(driveClass);
            DriverManager.getConnection(url, username, password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建数据库
     */
    public void createDatabase() throws Exception {
        // 主数据库连接信息
        String mainDB = PropertiesUtil.getProperties("jdbc", "jdbc.url");
        String user = PropertiesUtil.getProperties("jdbc", "jdbc.user");
        String password = PropertiesUtil.getProperties("jdbc", "jdbc.password");

        // 一开始必须填一个已存在的数据库 连接主数据库
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(mainDB, user, password);
        Statement stat = conn.createStatement();
        // 创建新数据库
        Map<String, Object> configMap = DBContextHolder.getDBType();
        String databaseUrl = configMap.get(DBContextHolder.DATASOURCE_URL).toString();
        Integer startIndex = databaseUrl.lastIndexOf("/");
        Integer endIndex = databaseUrl.indexOf("?");
        // 数据库名
        String databaseName = databaseUrl.substring(startIndex + 1, endIndex);
        stat.executeUpdate("create database " + databaseName);
        // 关闭主数据库连接
        stat.close();
        conn.close();

        // 连接新建的数据库
        conn = DriverManager.getConnection(configMap.get(DBContextHolder.DATASOURCE_URL).toString(), user, password);
        stat = conn.createStatement();
        // 创建表
        // 创建ScriptRunner，用于执行SQL脚本
        ScriptRunner runner = new ScriptRunner(conn);
        runner.setErrorLogWriter(null);
        runner.setLogWriter(null);
        // 执行SQL脚本
        runner.runScript(Resources.getResourceAsReader("sql/school.sql"));
        // 关闭数据库
        stat.close();
        conn.close();
    }

    /**
         使用步骤：
         1.启动时初始化所有学校的数据库加入targetDataSources集合 切换数据库时如果没有创建数据库和表则创建。

             初始化数据源代码:
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

             切换数据源代码：
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

                 User user = userMapper.selectByPrimaryKey(1);
                 System.out.println(user.getLogin());

            切换数据源测试代码：
                 // 主数据源
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

                 // 新建数据源
                 user = userMapper.selectByPrimaryKey(1);
                 System.out.println(user.getLogin());
         2.新加入学校需自动生成map的信息并调用determineCurrentLookupKey初始化数据库。
     */
}
