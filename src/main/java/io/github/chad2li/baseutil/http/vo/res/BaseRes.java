package io.github.chad2li.baseutil.http.vo.res;

import io.github.chad2li.baseutil.exception.IAppCode;
import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.github.chad2li.baseutil.util.StringUtils;
import com.github.pagehelper.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class BaseRes<T> {
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
     * 成功的响应重载方法
     *
     * @param <T>
     * @return
     */
    public static <T> BaseRes<T> succ() {
        return resp();
    }

    /**
     * 成功响应数据
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> BaseRes<T> succ(T data) {
        return resp(BaseCode.SUCC, "succ", data);
    }

    /**
     * 返回不带数据的成功消息
     *
     * @return
     */
    public static <T> BaseRes<T> resp() {
        return resp(BaseCode.SUCC, "succ", null);
    }

    /**
     * 返回指定状态码的消息
     *
     * @param code
     * @return
     */
    public static <T> BaseRes<T> resp(IAppCode code) {
        return resp(code, "", null);
    }

    /**
     * 返回定制的不带数据的消息
     *
     * @param code
     * @param msg
     * @return
     */
    public static <T> BaseRes<T> resp(IAppCode code, String msg) {
        return resp(code, msg, null);
    }

    /**
     * 返回带数据的成功消息
     *
     * @param data 数据
     * @param <T>
     * @return
     */
    public static <T> BaseRes<T> resp(T data) {
        return resp(BaseCode.SUCC, "succ", data);
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
        BaseRes<T> base = null;
        if (null != data && data instanceof Page) {
            Page p = (Page) data;
            base = PagerRes.page(p.getPageNum(), p.getPageSize(), p.getTotal());
        }

        if (null == base)
            base = new BaseRes();

        base.setCode(IAppCode.fullCode(code)).setMsg(msg).setData(data);

        return base;
    }

    public BaseRes() {
    }

    /**
     * 是否为成功响应
     *
     * @return
     */
    public boolean isSucc() {
        return IAppCode.fullCode(BaseCode.SUCC).equals(this.code);
    }

    public T getData() {
        return data;
    }

    public BaseRes<T> setData(T data) {
        this.data = data;
        return this;
    }

    public String getCode() {
        return code;
    }

    public BaseRes<T> setCode(String code) {
        this.code = code;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public BaseRes<T> setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public static BaseRes res(IAppCode code, String msg) {
        return res(IAppCode.fullCode(code), msg);
    }

    public static BaseRes res(IAppCode code) {
        return res(IAppCode.fullCode(code), code.msg());
    }

    public static BaseRes res(String code, String msg) {
        BaseRes res = new BaseRes();
        res.setCode(StringUtils.isNull(code) ? IAppCode.fullCode(BaseCode.ERROR) : code);
        res.setMsg(StringUtils.isNull(msg) ? BaseCode.ERROR.msg() : msg);
        return res;
    }

    @Override
    public String toString() {
        return "BaseRes{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
