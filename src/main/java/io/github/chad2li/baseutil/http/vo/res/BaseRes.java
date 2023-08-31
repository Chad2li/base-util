package io.github.chad2li.baseutil.http.vo.res;

import com.github.pagehelper.Page;
import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * rest统一响应
 *
 * @author chad
 * @since 1 by chad at 2023/8/19
 */
@ApiModel
@Data
public class BaseRes<T> implements Serializable {
    /**
     * 状态码
     */
    @ApiModelProperty("返回状态码")
    protected String code;

    /**
     * 返回消息
     */
    @ApiModelProperty("返回描述信息")
    protected String msg;

    /**
     * 数据
     */
    @ApiModelProperty(value = "数据", notes = "如果该接口没有任何数据则该字段为空")
    private T data;

    /**
     * 成功且无数据
     *
     * @return base res
     * @author chad
     * @see BaseRes#resp(IAppCode, String, Object)
     * @since 1 by chad at 2018/2/27
     */
    public static <T> BaseRes<T> succ() {
        return resp(BaseCode.SUCC, BaseCode.SUCC.msg(), null);
    }

    /**
     * 成功且有数据
     *
     * @return base res
     * @author chad
     * @see BaseRes#resp(IAppCode, String, Object)
     * @since 1 by chad at 2018/2/27
     */
    public static <T> BaseRes<T> succ(T data) {
        return resp(BaseCode.SUCC, BaseCode.SUCC.msg(), data);
    }

    /**
     * 定制返回消息信息
     *
     * @param code 状态码
     * @param msg  返回消息说明
     * @param data 消息携带的数据
     * @param <T>
     * @return
     */
    public static <T> BaseRes<T> resp(IAppCode code, String msg, T data) {
        return new BaseRes<>(code.fullCode(), msg, data);
    }

    public BaseRes(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public BaseRes() {
        // do nothing
    }

    /**
     * 业务是否执行成功
     *
     * @return true业务执行成功；否则失败
     * @author chad
     * @since 1 by chad at 2018/2/27
     */
    public boolean isSucc() {
        return BaseCode.SUCC.fullCode().equals(this.code);
    }

    private static final long serialVersionUID = 1L;
}
