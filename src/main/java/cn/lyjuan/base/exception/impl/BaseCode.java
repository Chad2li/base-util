package cn.lyjuan.base.exception.impl;

import cn.lyjuan.base.exception.IAppCode;
import cn.lyjuan.base.exception.IAppModuleEnum;

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
    , ERROR("0005", "error");

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
