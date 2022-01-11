package cn.lyjuan.base.util.field;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 配置结构用来说明一个配置的所有属性及属性的类型和条件
 * <p>
 * 用于接口的请求或响应参数
 * </p>
 *
 * @author chad
 * @date 2021/12/22 12:00:00
 * @since 1 by chad at 2021/12/22 新增
 */
@Data
@ApiModel
public class FieldApiVo {
    @ApiModelProperty(required = true, value = "属性名")
    private String name;
    @ApiModelProperty(required = true, value = "属性标题")
    private String title;
    @ApiModelProperty(required = true, value = "类型")
    private ItemTypeEnum type;
    @ApiModelProperty(required = false, value = "备注信息，为空表示没有")
    private String remark = "";
    @ApiModelProperty(required = false, value = "数字为最大值（包含），字符为最大长度（包含），-1表示不限制")
    private int max = -1;
    @ApiModelProperty(required = false, value = "数字为最小值（包含），字符为最小长度（包含），-1表示不限制")
    private int min = -1;
    @ApiModelProperty(required = true, value = "true允许为空")
    private boolean notNull;
    @ApiModelProperty(required = true, value = "升序排序")
    private int sort;
    @ApiModelProperty(required = false, value = "List、Map的值类型")
    private FieldApiVo subField;
    @ApiModelProperty(required = false, value = "Object的属性列表")
    private List<FieldApiVo> objectFields;
}

