package io.github.chad2li.baseutil.mybatis.typehander;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.github.chad2li.baseutil.consts.DefaultConstant;
import io.github.chad2li.baseutil.mybatis.type.CommaJoinList;
import io.github.chad2li.baseutil.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * List&lt;String&gt;与,分隔的字符串 之间相互转换
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/31 08:42
 */
@Slf4j
@MappedTypes(CommaJoinList.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class CommaJoinListTypeHandler extends AbstractStringNullTypeHandler<List<String>> {
    @Override
    protected String format(List<String> parameter) {
        return StringUtils.joinIgnoreEmpty(DefaultConstant.Norm.COMMA,
                ArrayUtil.toArray(parameter, String.class));
    }

    @Override
    protected List<String> parse(String value) {
        if (CharSequenceUtil.isBlank(value)) {
            // 如果值为空，返回空集合
            return new ArrayList<>(8);
        }
        return StrUtil.split(value, DefaultConstant.Norm.COMMA);
    }
}
