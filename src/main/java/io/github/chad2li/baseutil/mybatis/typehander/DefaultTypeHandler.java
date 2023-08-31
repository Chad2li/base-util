package io.github.chad2li.baseutil.mybatis.typehander;

import io.github.chad2li.baseutil.mybatis.enums.ICodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.EnumTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * mybatis默认typehandler
 * <p>
 * 处理原理：<br />
 * 1. 在实例化时判断是否为{@link ICodeEnum}实现枚举值
 * 2. 如果是，则使用{@link CodeTypeHandler}代理处理
 * </p>
 * <p>
 * 使用方法：<br />
 * 1. 配置该处理器为mybatis默认处理器
 * 2. 数据值枚举类实现{@link ICodeEnum}
 * </p>
 *
 * @param <E>
 * @author chad
 */
public class DefaultTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {

    private BaseTypeHandler<E> typeHandler = null;

    public DefaultTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        if (ICodeEnum.class.isAssignableFrom(type)) {
            // 如果实现了 BaseCodeEnum 则使用我们自定义的转换器
            typeHandler = new CodeTypeHandler(type);
        } else {
            // 默认转换器 也可换成 EnumOrdinalTypeHandler
            typeHandler = new EnumTypeHandler<>(type);
        }
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
        typeHandler.setNonNullParameter(ps, i, parameter, jdbcType);
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return (E) typeHandler.getNullableResult(rs, columnName);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return (E) typeHandler.getNullableResult(rs, columnIndex);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return (E) typeHandler.getNullableResult(cs, columnIndex);
    }
}
