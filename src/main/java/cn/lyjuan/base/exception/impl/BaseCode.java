package cn.lyjuan.base.exception.impl;

import cn.lyjuan.base.exception.IAppCode;
import cn.lyjuan.base.exception.IAppModuleEnum;

public enum BaseCode implements IAppCode
{
    /**
     * 业务执行成功
     */
    SUCC("0001", "SUCC")
    /**
     * 业务执行失败
     */
    , FAILED("0002", "FAILED")
    /**
     * 异常
     */
    , ERROR("0003", "ERROR");

    private static final IAppModuleEnum MODULE = BaseModule.BASE_COMMON;

    private String code;

    private String msg;

    @Override
    public IAppModuleEnum module()
    {
        return MODULE;
    }

    @Override
    public String code()
    {
        return this.module() + code;
    }

    @Override
    public String msg()
    {
        return msg;
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
