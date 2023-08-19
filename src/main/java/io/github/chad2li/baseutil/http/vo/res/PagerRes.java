package io.github.chad2li.baseutil.http.vo.res;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 带分页的统一响应
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

    public PagerRes(String code, String msg, T list, Long totalCount) {
        super(code, msg, list);
        this.tc = totalCount;
    }

    private static final long serialVersionUID = 1L;
}
