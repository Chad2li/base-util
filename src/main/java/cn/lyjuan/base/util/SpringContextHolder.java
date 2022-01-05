package cn.lyjuan.base.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Profiles;

import java.util.Map;

/**
 * 以静态变量保存Spring ApplicationContext, 可在任何代码任何地方任何时候中取出ApplicaitonContext.
 */
public class SpringContextHolder implements ApplicationContextAware {

    /**
     * 生产环境策略标识
     */
    public static final String PROFILE_PROD = "prod";

    private static ApplicationContext applicationContext;


    /**
     * 实现ApplicationContextAware接口的context注入函数, 将其存入静态变量.
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        setMyApplicationContext(ctx);
    }

    /**
     * 设置 ApplicationContext
     *
     * @param ctx Spring ApplicationContext
     * @return void
     * @date 2021/11/19 17:16
     * @author chad
     * @since 1 by chad at 2021/11/19 新增
     */
    public static void setMyApplicationContext(ApplicationContext ctx) {
        SpringContextHolder.applicationContext = ctx;
    }


    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        checkApplicationContext();
        return applicationContext;
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    /**
     * 从静态变量ApplicationContext中取得Bean, 自动转型为所赋值对象的类型.
     * 如果有多个Bean符合Class, 取出第一个.
     */
    public static <T> T getBean(Class<T> clazz) {
        checkApplicationContext();
        Map beanMaps = getApplicationContext().getBeansOfType(clazz);
        if (StringUtils.isNull(beanMaps)) {
            return null;
        }
        return (T) beanMaps.values().iterator().next();
    }

    /**
     * 获取spring.profiles.active
     */
    public static String[] getActiveProfile() {
        return getApplicationContext().getEnvironment().getActiveProfiles();
    }

    /**
     * 是否包含了该策略
     *
     * @param profile
     * @return true包含
     * @since 2 by chad at 2022/02/05 修正方法名
     */
    public static boolean hasActiveProfile(String profile) {
        Profiles profiles = Profiles.of(profile);
        return getApplicationContext().getEnvironment().acceptsProfiles(profiles);
    }

    /**
     * 是否为生产环境
     *
     * @return boolean  true生产环境；false其他环境
     * @date 2022/1/5 09:03
     * @author chad
     * @since 1 by chad create
     */
    public static boolean isProdProfile() {
        return hasActiveProfile(PROFILE_PROD);
    }

    /**
     * 检查是否注入了 ApplicationContent
     */
    private static void checkApplicationContext() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicationContext未注入,请在applicationContext.xml中定义SpringContextHolder");
        }
    }

    public SpringContextHolder(ApplicationContext applicationContext) {
        setApplicationContext(applicationContext);
    }

    public SpringContextHolder() {
    }
}