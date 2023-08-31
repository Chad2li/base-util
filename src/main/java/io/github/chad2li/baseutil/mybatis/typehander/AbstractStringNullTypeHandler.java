package io.github.chad2li.baseutil.mybatis.typehander;

import cn.hutool.core.util.ObjectUtil;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.lang.Nullable;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数据库类型为String且可为null，抽象转换为Java类型的type handler
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/31 08:36
 */
public abstract class AbstractStringNullTypeHandler<P> extends BaseTypeHandler<P> {
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, P parameter, JdbcType jdbcType) throws SQLException {
        if (ObjectUtil.isEmpty(parameter)) {
            // 写入null
            ps.setNull(i, JdbcType.VARBINARY.TYPE_CODE);
            return;
        }
        String value = format(parameter);
        ps.setString(i, value);
    }

    @Override
    public P getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parse(rs.getString(columnName));
    }

    @Override
    public P getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parse(rs.getString(columnIndex));
    }

    @Override
    public P getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parse(cs.getString(columnIndex));
    }

    /**
     * 将参数转为字符串，入参出参均不为空
     *
     * @param parameter Java类型值
     * @return 参数转为对应的数据库String类型值
     * @author chad
     * @since 1 by chad at 2023/8/31
     */
    protected abstract String format(P parameter);

    /**
     * 将数据库String值解析为Java类型值，入参出参可能为空
     *
     * @param value 数据库值
     * @return Java类型值
     * @author chad
     * @since 1 by chad at 2023/8/31
     */
    protected abstract P parse(@Nullable String value);
}
