package cn.lyjuan.base.exception.util.test;

import cn.lyjuan.base.exception.IAppModuleEnum;

public enum BaseModuleImpl implements IAppModuleEnum {
    HTTP_API("HTTP_API");

    private String module;

    BaseModuleImpl(String module) {
        this.module = module;
    }

    @Override
    public String module() {
        return this.module;
    }
}
