package io.github.chad2li.baseutil.mybatis.typehander;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.chad2li.baseutil.mybatis.enums.ICodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数字与枚举值转换器
 *
 * @param <E>
 * @author chad
 */
public class CodeTypeHandler<E extends Enum<?> & ICodeEnum> extends BaseTypeHandler<ICodeEnum> {
    private Class<E> type;

    public CodeTypeHandler(Class<E> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ICodeEnum parameter, JdbcType jdbcType)
            throws SQLException {
        ps.setString(i, parameter.code());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String i = rs.getString(columnName);
        return toEnum(i);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String i = rs.getString(columnIndex);
        return toEnum(i);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String i = cs.getString(columnIndex);
        return toEnum(i);
    }

    private E toEnum(String code) {
        E[] enumConstants = type.getEnumConstants();
        if (null == enumConstants || enumConstants.length < 1) return null;
        for (E e : enumConstants) {
            if (CharSequenceUtil.equalsIgnoreCase(e.code(), code)) {
                return e;
            }
        }

        return null;
    }
}
