package cn.lyjuan.base.redis;

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 只需要把SqlSessionFactoryBean的源码复制一份，重命名成ScanEnumSqlSessionFactoryBean，然后稍加修改：
 */
@Data
@Slf4j
@Configuration
@ConfigurationProperties(
        prefix = "mybatis"
)
public class SqlSessionConfig {
//    @Value("${mapper-locations}")
    private String[] mapperLocations;

//    @Value("${common-mybatis.mapper-locations}")
//    private String commonMapperLocation;

    @Value("${spring.datasource.druid.username}")
    private String username;

    @Value("${spring.datasource.druid.password}")
    private String password;

    @Value("${spring.datasource.druid.url}")
    private String dbUrl;

    @Value("${spring.datasource.druid.initial-size}")
    private int initialSize;

    @Value("${spring.datasource.druid.min-idle}")
    private int minIdle;

    @Value("${spring.datasource.druid.max-active}")
    private int maxActive;

    @Value("${spring.datasource.druid.max-wait}")
    private long maxWait;

    @Value("${spring.datasource.druid.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.druid.min-evictable-idle-time-millis}")
    private long minEvictableIdleTimeMillis;

    @Value("${spring.datasource.druid.time-between-eviction-runs-millis}")
    private long timeBetweenEvictionRunsMillis;

    @Value("${spring.datasource.druid.validation-query}")
    private String validationQuery;

    @Value("${spring.datasource.druid.test-while-idle}")
    private boolean testWhileIdle;

    @Value("${spring.datasource.druid.test-on-borrow}")
    private boolean testOnBorrow;

    @Value("${spring.datasource.druid.test-on-return}")
    private boolean testOnReturn;

//    @Value("${spring.datasource.druid.filter.stat.log-slow-sql}")
    private boolean logSlowSql;

//    @Value("${spring.datasource.druid.filter.stat.slow-sql-millis}")
    private long slowSqlMillis;

    @Bean
    public DruidDataSource dataSource() {
        log.info("Load DataSource to {}", dbUrl);
        DruidDataSource druidDataSource = new DruidDataSource();
        try {
            druidDataSource.setUsername(username);
            druidDataSource.setPassword(password);
            druidDataSource.setUrl(dbUrl);
            druidDataSource.setFilters("stat,wall");
            druidDataSource.setInitialSize(initialSize);
            druidDataSource.setMinIdle(minIdle);
            druidDataSource.setMaxActive(maxActive);
            druidDataSource.setMaxWait(maxWait);
            druidDataSource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
            druidDataSource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
            druidDataSource.setUseGlobalDataSourceStat(true);
            druidDataSource.setDriverClassName(driverClassName);
            druidDataSource.setValidationQuery(validationQuery);
            druidDataSource.setTestWhileIdle(testWhileIdle);
            druidDataSource.setTestOnBorrow(testOnBorrow);
            druidDataSource.setTestOnReturn(testOnReturn);
            // 设置需要的过滤
            List<Filter> statFilters =new ArrayList<>();
            StatFilter statFilter = new StatFilter();
            statFilter.setLogSlowSql(logSlowSql);
            statFilter.setSlowSqlMillis(slowSqlMillis);
            statFilters.add(statFilter);
            // 设置慢SQL
            druidDataSource.setProxyFilters(statFilters);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return druidDataSource;
    }

    private static final ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();

    @Bean
    public SqlSessionFactoryBean mysqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new CustomSqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

//        return (Resource[]) Stream.of((Object[]) Optional.ofNullable(this.mapperLocations).orElse(new String[0])).flatMap((location) -> {
//            return Stream.of(this.getResources(location));
//        }).toArray((x$0) -> {
//            return new Resource[x$0];
//        });
        List<Resource> list = new ArrayList<>();
        for (int i = 0; i < mapperLocations.length; i++) {
            Resource[] sub = resourceResolver.getResources(mapperLocations[i]);
            list.addAll(Arrays.asList(sub));
        }

        sqlSessionFactoryBean.setMapperLocations(list.toArray(new Resource[list.size()]));

//        sqlSessionFactoryBean.setMapperLocations(rs);
//        sqlSessionFactoryBean.setPlugins(new Interceptor[]{Interceptornew CatMybatisInterceptor(dbUrl)});
        return sqlSessionFactoryBean;
    }

}