package cn.lyjuan.base.exception.impl;

import cn.lyjuan.base.exception.IAppModuleEnum;

public enum  BaseModule implements IAppModuleEnum
{
    /**
     * 通用组件
     */
    BASE_COMMON("01")
    /**
     * 通用领域
     */
    , BASE_MODEL("02")
    /**
     * 通用服务模块
     */
    , BASE_SERVICE("03")
    /**
     * http接口
     */
    , HTTP_API("04")
    /**
     * PC WEB
     */
    , HTTP_WEB("05")
    /**
     * 手机H5
     */
    , HTTP_H5("06")
    /**
     * 后台管理系统
     */
    , HTTP_BMS("07")
    /**
     * 普通服务
     */
    , SERVICE_COMMON("08")
    /**
     * 任务服务
     */
    , SERVICE_TASK("09");

    /**
     * 模块名称
     */
    private String module;

    @Override
    public String module()
    {
        return this.module;
    }

    BaseModule(String module)
    {
        this.module = module;
    }
}
