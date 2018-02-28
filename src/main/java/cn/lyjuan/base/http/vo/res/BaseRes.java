package cn.lyjuan.base.http.vo.res;

import cn.lyjuan.base.exception.impl.BaseCode;
import cn.lyjuan.base.exception.IAppCode;
import com.github.pagehelper.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class BaseRes<T>
{
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
     * 返回不带数据的成功消息
     * @return
     */
    public static <T> PagerRes<T> resp()
    {
        return resp(BaseCode.SUCC, "succ", null);
    }

    /**
     * 返回指定状态码的消息
     * @param code
     * @return
     */
    public static <T> PagerRes<T> resp(IAppCode code)
    {
        return resp(code, "", null);
    }

    /**
     * 返回定制的不带数据的消息
     * @param code
     * @param msg
     * @return
     */
    public static <T> PagerRes<T> resp(IAppCode code, String msg)
    {
        return resp(code, msg, null);
    }

    /**
     * 返回带数据的成功消息
     * @param t             数据
     * @param <T>
     * @return
     */
    public static <T> PagerRes<T> resp(T t)
    {
        return resp(BaseCode.SUCC, "succ", t);
    }

    /**
     * 定制返回消息信息
     * @param code      状态码
     * @param msg       返回消息说明
     * @param t         消息携带的数据
     * @param <T>
     * @return
     */
    public static <T> PagerRes<T> resp(IAppCode code, String msg, T t)
    {
        PagerRes<T> base = null;
        if (null != t && t instanceof Page)
        {
            Page p = (Page) t;
            base = PagerRes.page(p.getPageNum(), p.getPageSize(), p.getTotal());
        }

        if (null == base)
            base = new PagerRes<>();

        base.setCode(IAppCode.fullCode(code)).setMsg(msg).setData(t);

        return base;
    }

    public BaseRes()
    {
    }

    public T getData()
    {
        return data;
    }

    public BaseRes<T> setData(T data)
    {
        this.data = data;
        return this;
    }

    public String getCode()
    {
        return code;
    }

    public BaseRes<T> setCode(String code)
    {
        this.code = code;
        return this;
    }

    public String getMsg()
    {
        return msg;
    }

    public BaseRes<T> setMsg(String msg)
    {
        this.msg = msg;
        return this;
    }

    @Override
    public String toString()
    {
        return "BaseRes{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }
}
