package cn.lyjuan.base.http.validator;

/**
 * @author chad
 * @date 2021/10/27 23:54
 * @since 1 by chad at 2021/10/27 新增
 */

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.validation.Validator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 自动处理参数校验的扩展信息
 * spring默认使用{@link AcceptHeaderLocaleResolver}从请求头中读取 {@code accept-language}区分语言，
 * 对于Locale的切换，Spring是通过拦截器来实现的，其提供了一个LocaleChangeInterceptor，在该拦截器中的preHandle()方法中，
 * Spring会读取浏览器参数中的locale参数，然后调用LocaleResolver.setLocale()方法来实现对Locale的切换。
 *
 * @author chad
 * @date 2021/10/27
 * @since by chad at 2021/10/27 新增
 */
public class ValidatorUtil {

    /**
     * 自定义spring validator校验器
     *
     * @return javax.validation.Validator
     * @date 2021/10/27 23:58
     * @author chad
     * @since by chad at 2021/10/27 新增
     */
    public static Validator validator(MessageSource messageSource) {
        LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();
        localValidatorFactoryBean.setValidationMessageSource(messageSource);
        localValidatorFactoryBean.setMessageInterpolator(new MessageInterpolator());

        return localValidatorFactoryBean;
    }

    public static MessageSource messageSource() {
        return messageSource("classpath:/ValidationMessages", "classpath:/ValidationMessages_en");
    }

    /**
     * 解决国际化资源文件
     * <p>
     * 资源存放在classpath下的ValidationMessages.properties和ValidationMessages_en.properties，目前仅
     * 支持中文和英文，默认设置为中文
     * </p>
     * <p>
     * 详见：https://blog.csdn.net/DislodgeCocoon/article/details/80520235
     * </p>
     *
     * @param resources 资源文件路径如："classpath:/ValidationMessages"
     * @return 资源中心
     */
    public static MessageSource messageSource(String... resources) {
        // 默认设为中文
        Locale.setDefault(Locale.CHINA);
        // 加载设定的国际化资源文件
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(resources);

        return messageSource;
    }

    /**
     * 自定义校验失败消息处理器
     * <p>
     * 根据校验不通过的注解（如{@link javax.validation.constraints.Size}）和 参数名称，获取国际化资源消息
     * </p>
     */
    private static class MessageInterpolator extends ResourceBundleMessageInterpolator {
        @Override
        public String interpolate(String message, javax.validation.MessageInterpolator.Context context, Locale locale) {
            // 获取注解类型
            String annotationTypeName = context.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();

            // 根据注解类型获取自定义的消息Code
            String annotationDefaultMessageCode = VALIDATION_ANNATATION_DEFAULT_MESSAGES.get(annotationTypeName);
            if (null != annotationDefaultMessageCode && !message.startsWith("javax.validation")
                    && !message.startsWith("org.hibernate.validator.constraints")) {
                // 如果注解上指定的message不是默认的javax.validation或者org.hibernate.validator等开头的情况，
                // 则需要将自定义的消息Code拼装到原message的后面；
                message += "{" + annotationDefaultMessageCode + "}";
            }

            return super.interpolate(message, context, locale);
        }
    }

    /**
     * 校验注解对应在国际化资源中的资源名
     */
    private static final Map<String, String> VALIDATION_ANNATATION_DEFAULT_MESSAGES =
            new HashMap<String, String>(20) {{
                put("Min", "validation.message.min");
                put("Max", "validation.message.max");
                put("NotNull", "validation.message.notNull");
                put("NotBlank", "validation.message.notBlank");
                put("NotEmpty", "validation.message.notEmpty");
                put("Size", "validation.message.size");
            }};
}
