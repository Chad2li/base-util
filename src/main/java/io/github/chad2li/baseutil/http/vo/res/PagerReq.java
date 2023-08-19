package io.github.chad2li.baseutil.http.vo.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分页请求基类
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/19 14:51
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PagerReq implements Serializable {
    /**
     * page number
     */
    private Integer pn;
    /**
     * page size
     */
    private Integer ps;
    /**
     * 排序字段，会自动将驼峰格式转为数据库格式
     */
    private String sortKey;
    /**
     * 排序方式，ASC, DESC
     */
    private String sortDirection;
    private static final long serialVersionUID = 1L;
}
