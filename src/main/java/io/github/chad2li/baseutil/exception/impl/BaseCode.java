package io.github.chad2li.baseutil.exception.impl;

import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.IAppModuleEnum;

public enum BaseCode implements IAppCode
{
    /**
     * 业务执行成功
     */
    SUCC("0000", "succ")
    /**
     * 业务执行失败
     */
    , FAILED("0002", "failed")
    /**
     * 参数无效
     */
    , PARAM_INVALID("0003", "param_invalid")
    /**
     * 不支持的请求方法
     */
    , REQ_METHOD_UNSUPPORTED("0004", "req_method_unsupported")
    /**
     * 异常
     */
    , ERROR("0005", "error")
    /**
     * 路径不存在
     */
    , PATH_NOT_FOUND("0006", "path_not_found")
    /**
     * requestId重复
     */
    , REQUESTID_DUPLICATE("0007", "request_id_duplicate")
    /**
     * 请求时间戳超时
     */
    , TIMESTAMP_TIMEOUT("0008", "timestamp_timeout")
    /**
     * appId无效
     */
    , APP_ID_INVALID("0009", "app_id_invalid")
    /**
     * 签名无效
     */
    , SIGN_INVALID("0010", "sign_invalid")
    /**
     * 非法访问
     */
    , ACCESS_ILLEGAL("0011", "access_illegal")
    /**
     * 网络异常
     */
    , NETWORK_ERROR("0012", "network_error")
    //
    ;

    private String code;

    private String msg;

    @Override
    public IAppModuleEnum module()
    {
        return BaseModule.BASE_COMMON;
    }

    @Override
    public String code()
    {
        return this.code;
    }

    @Override
    public String msg()
    {
        return this.msg;
    }

    BaseCode(String code, String msg)
    {
        this.code = code;
        this.msg = msg;
    }

    @Override
    public String toString()
    {
        return this.code();
    }
}
