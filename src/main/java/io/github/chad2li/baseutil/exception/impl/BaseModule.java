package io.github.chad2li.baseutil.exception.impl;

import io.github.chad2li.baseutil.exception.IAppModuleEnum;

/**
 * 模块配置
 *
 * @author chad
 */
public enum BaseModule implements IAppModuleEnum {
    /**
     * 通用组件
     */
    BASE_COMMON
    /**
     * 通用领域
     */
    , BASE_MODEL
    /**
     * 通用服务模块
     */
    , BASE_SERVICE
    /**
     * http接口
     */
    , HTTP_API
    /**
     * PC WEB
     */
    , HTTP_WEB
    /**
     * 手机H5
     */
    , HTTP_H5
    /**
     * 后台管理系统
     */
    , HTTP_BMS
    /**
     * 普通服务
     */
    , SERVICE_COMMON
    /**
     * 任务服务
     */
    , SERVICE_TASK;


    @Override
    public String displayName() {
        return this.name();
    }
}
