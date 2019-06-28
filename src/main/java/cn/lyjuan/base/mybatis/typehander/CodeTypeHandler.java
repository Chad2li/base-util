package cn.lyjuan.base.mybatis.typehander;

import cn.lyjuan.base.mybatis.enums.ICodeEnum;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 数字与枚举值转换器
 * @param <E>
 */
public class CodeTypeHandler<E extends Enum<?> & ICodeEnum> extends BaseTypeHandler<ICodeEnum>
{
    private Class<E> type;

    public CodeTypeHandler(Class<E> type)
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type argument cannot be null");
        }
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, ICodeEnum parameter, JdbcType jdbcType)
            throws SQLException
    {
        ps.setInt(i, parameter.code());
    }

    @Override
    public E getNullableResult(ResultSet rs, String columnName) throws SQLException
    {
        int i = rs.getInt(columnName);
        return toEnum(i);
    }

    @Override
    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException
    {
        int i = rs.getInt(columnIndex);
        return toEnum(i);
    }

    @Override
    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException
    {
        int i = cs.getInt(columnIndex);
        return toEnum(i);
    }

    private E toEnum(int code)
    {
        E[] enumConstants = type.getEnumConstants();
        if (null == enumConstants || enumConstants.length < 1) return null;
        for (E e : enumConstants)
        {
            if (e.code() == code)
                return e;
        }

        return null;
    }
}
