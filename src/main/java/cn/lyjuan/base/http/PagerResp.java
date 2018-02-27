package cn.lyjuan.base.http;

import com.github.pagehelper.Page;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class PagerResp<T> extends BaseResp<T>
{
    /**
     * page number
     */
    @ApiModelProperty(value = "当前页", notes = "默认为1")
    private Integer pn;

    /**
     * page size
     */
    @ApiModelProperty("页大小")
    private Integer ps;

    /**
     * totcal count
     */
    @ApiModelProperty(value = "条目总数", notes = "用于计算分页")
    private Long tc;

    /**
     * 偏移量
     * @return
     */
    public Integer offset()
    {
        return (pn - 1) * ps;
    }

    /**
     * 数据条目
     * @return
     */
    public Integer limit()
    {
        return ps;
    }

    /**
     * 获取总页数，0条记录返回 1
     * @return
     */
    public Integer pageCount()
    {
        return (int) ((tc - 1) / ps + 1);
    }

    public Integer getPn()
    {
        return pn;
    }

    /**
     * 清除信息
     */
    public PagerResp<T> clear()
    {
        this.pn = null;
        this.ps = null;
        this.tc = null;

        return this;
    }

    public PagerResp()
    {
    }

    public PagerResp<T> page(Object page)
    {
        if (null != page && page instanceof Page)
        {
            Page p = (Page) page;

            this.setPn(p.getPageNum()).setPs(p.getPageSize()).setTc(p.getTotal());
        }

        return this;
    }

    public static <T> PagerResp<T> page(Integer pn, Integer ps, Long tc)
    {
        return new PagerResp<T>().setPn(pn).setPs(ps).setTc(tc);
    }

    public PagerResp<T> setPn(Integer pn)
    {
        this.pn = pn;
        return this;
    }

    public Integer getPs()
    {
        return ps;
    }

    public PagerResp<T> setPs(Integer ps)
    {
        this.ps = ps;
        return this;
    }

    public Long getTc()
    {
        return tc;
    }

    public PagerResp<T> setTc(Long tc)
    {
        this.tc = tc;
        return this;
    }

    @Override
    public String toString()
    {
        return "PagerResp{" +
                "pn=" + pn +
                ", ps=" + ps +
                ", tc=" + tc +
                '}';
    }
}
