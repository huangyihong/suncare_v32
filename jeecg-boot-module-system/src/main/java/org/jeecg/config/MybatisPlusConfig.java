package org.jeecg.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;

/**
 * 单数据源配置（jeecg.datasource.open = false时生效）
 * @Author zhoujf
 *
 */
@Configuration
@MapperScan({"org.jeecg.modules.**.mapper*","com.ai.modules.**.mapper*"})
public class MybatisPlusConfig {
	
	/**
         *  分页插件
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        // 设置sql的limit为无限制，默认是500
        return new PaginationInterceptor().setLimit(-1);
    }

    /**
     * 打印 sql
     */
    /*@Bean
    public PerformanceInterceptor performanceInterceptor() {
        PerformanceInterceptor performanceInterceptor = new PerformanceInterceptor();
        //格式化sql语句
        Properties properties = new Properties();
        properties.setProperty("format", "false");
        performanceInterceptor.setProperties(properties);
        return performanceInterceptor;
    }*/

//    /**
//     * mybatis-plus SQL执行效率插件【生产环境可以关闭】
//     */
//    @Bean
//    public PerformanceInterceptor performanceInterceptor() {
//        return new PerformanceInterceptor();
//    }

/*    @Bean
    public SqlSessionFactoryBean configSqlSessionFactoryBean(DataSource dataSource) throws IOException {
        System.out.println("初始化SqlSessionFactoryBean");
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        // configuration.setLogImpl(StdOutImpl.class);//标准输出日志
        configuration.setLogImpl(NoLoggingImpl.class);// 不输出日志（）
        configuration.setMapUnderscoreToCamelCase(true);// 开启驼峰命名
        configuration.setCallSettersOnNulls(true);// 开启在属性为null也调用setter方法

        sqlSessionFactoryBean.setConfiguration(configuration);
        sqlSessionFactoryBean.setDataSource(dataSource);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setMapperLocations(resolver.getResources(mapperLocations));// 设置mapper文件扫描路径
        return sqlSessionFactoryBean;
    }*/    
}
