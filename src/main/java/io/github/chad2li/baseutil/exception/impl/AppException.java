package io.github.chad2li.baseutil.exception.impl;

import cn.hutool.core.util.StrUtil;
import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.IAppException;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务逻辑异常，无法处理，返回状态码及信息
 *
 * @author chad
 * @since 1 by chad at 2018/2/27
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AppException extends RuntimeException implements IAppException {
    /**
     * 状态码
     */
    private IAppCode code;
    /**
     * 自定义消息
     */
    private String msg;

    public AppException(IAppCode code, String msg, Throwable throwable) {
        super(StringUtils.joinIgnoreEmpty("-", code.fullCode(), msg), throwable);
        this.setCode(code);
        this.setMsg(msg);
    }
}
