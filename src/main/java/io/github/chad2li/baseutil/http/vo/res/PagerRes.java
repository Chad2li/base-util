package io.github.chad2li.baseutil.http.vo.res;

import io.github.chad2li.baseutil.exception.impl.BaseCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 带分页的统一响应
 *
 * @author chad
 * @since 1 by chad at 2018/2/27
 */
@ApiModel
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class PagerRes<T> extends BaseRes<T> {
    /**
     * total count
     */
    @ApiModelProperty(value = "条目总数", notes = "用于计算分页")
    private Long tc;

    PagerRes(String code, String msg, T list) {
        super(code, msg, list);
        this.tc = 0L;
    }

    /**
     * 构建 page res
     *
     * @param list response list
     * @return page res
     * @author chad
     * @since 1 by chad at 2023/8/24
     */

    public static <T> PagerRes<T> succ(T list) {
        return new PagerRes<>(BaseCode.SUCC.fullCode(), BaseCode.SUCC.msg(), list);
    }

    private static final long serialVersionUID = 1L;
}
