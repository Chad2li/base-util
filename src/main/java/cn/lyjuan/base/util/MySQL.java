package cn.lyjuan.base.util;


import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ly on 2015/9/18.
 */
public class MySQL
{
    private String table;// 表名

    private int pageNum;// 分页参数,第几页

    private int pageSize;// 分页参数,每页条数

    private String orderBy;// 排序

    private List<String> fields;// sql中需要操作的字段

    private String idColumn;// insert操作中序列对应的列

    private boolean idAuto = true;// 主键是否自动增长

    private List<ColumnAndValue> insertOrUpdateFields;// insert或update操作中不使用bean方式需要设置对应的列和值

    private List<ColumnAndValue> where;// where条件

    private List<ColumnAndValue> whereNull;// where条件中的是否为null条件

    private boolean showSql = false;// 是否打印sql语句

    private boolean annotation = true;// 是否开启annotation

    private String pool;// 连接池名称

    /** 获取数据库操作类实例 **/
    public static MySQL getInstance() {return new MySQL();}
    /** 设置链接池信息 **/
    public MySQL pool(final String pool){this.pool = pool;return this;}
    /** 设置表名 **/
    public MySQL table(String table){this.table = table;return this;}
    /** 设置分页第几页 **/
    public MySQL pageNum(int pageNum){this.pageNum = pageNum;return this;}
    /** 设置分页每页条数 **/
    public MySQL pageSize(int pageSize){this.pageSize = pageSize;return this;}
    /** 设置排序 **/
    public MySQL orderBy(String column, OrderBySort orderBySort)
    {
        this.orderBy = column;
        if (OrderBySort.DESC.compareTo(orderBySort) == 0)
        {
            this.orderBy = this.orderBy + " desc";
        }
        return this;
    }
    /** 设置排序 不需写order by 这几个字，例如：sort desc,createtime asc
     * @param orderBy       需要设置的多字段排序值 **/
    public MySQL orderBy(String orderBy) {this.orderBy = orderBy;return this;}
    /** 设置sql操作的字段 (区分大小写，必须与bean中属性名一致) **/
    public MySQL fields(String... field)
    {
        if (null == field)
            throw new IllegalArgumentException("fields cat not be null");

        if (null == this.fields)
            this.fields = new ArrayList<String>();

        for (String f : field)
        {
            this.fields.add(f);
        }
        return this;
    }
    /** 设置insert中自增的列和序列名 **/
    public MySQL idAuto(String idColumn, boolean idAuto){this.idColumn = idColumn;this.idAuto = idAuto;return this;}
    /** 设置是否打印sql语句 **/
    public MySQL showSql(boolean showSql) {this.showSql = showSql;return this;}
    /** 设置是否开启annotation **/
    public MySQL annotation(boolean annotation) {this.annotation = annotation;return this;}

    /**
     * insert操作中不使用bean方式，而是设置对应的列和值
     * @return
     */
    public MySQL insertField(String column, Object value)
    {
        if (this.insertOrUpdateFields == null)
        {
            this.insertOrUpdateFields = new ArrayList<ColumnAndValue>();
        }
        ColumnAndValue cav = new ColumnAndValue();
        cav.setColumn(column);
        cav.setValue(value);
        this.insertOrUpdateFields.add(cav);
        return this;
    }

    /**
     * update操作中不使用bean方式，而是设置对应的列和值
     * @return
     */
    public MySQL updateField(String column, Object value)
    {
        return this.insertField(column, value);
    }

    /**
     * 增加where条件
     * @param column        列名
     * @param operator      操作符 例如> < = != like >= <= 等
     * @param value
     * @return
     */
    public MySQL where(String column, String operator, Object value)
    {
        if (value == null || "".equals(value)) { throw new RuntimeException("where条件的值不能为空！"); }
        if (this.where == null)
        {
            this.where = new ArrayList<ColumnAndValue>();
        }
        ColumnAndValue cav = new ColumnAndValue();
        cav.setColumn(column);
        cav.setOperator(operator);
        cav.setValue(value);
        this.where.add(cav);
        return this;
    }

    /**
     * 增加where列名是否为null的条件
     * @param column        列名
     * @param cn            {@code ColumnNull}标识是否为null
     * @return
     */
    public MySQL whereNull(String column, ColumnNull cn)
    {
        if (this.whereNull == null)
        {
            this.whereNull = new ArrayList<ColumnAndValue>();
        }
        ColumnAndValue cav = new ColumnAndValue();
        cav.setColumn(column);
        cav.setValue(cn);
        this.whereNull.add(cav);
        return this;
    }

    /**
     * insert操作,将参数对象的值保存到数据库
     *
     * @param <T>
     * @param t
     * @return
     */
    public <T> int insert(T t)
    {
        this.setBeanToInsertOrUpdateFields(t);
        return this.insert();
    }

    /**
     * insert操作,将参数对象的值保存到数据库并且返回插入的id，必须设置idAuto
     * @param <T>
     * @param t
     * @return 插入的id
     */
    public <T> int insertReturnID(T t)
    {
        this.setBeanToInsertOrUpdateFields(t);
        return this.insertReturnID();
    }


    /**
     * insert操作,取insertOrUpdateFields中的值插入数据库
     *
     * @return
     */
    public int insert()
    {
        if (this.insertOrUpdateFields == null) { throw new RuntimeException("必须设置需要插入的字段！"); }
        String sql = this.getInsertSql();
        Connection conn = null;
        PreparedStatement ps = null;
        int num = 0;
        try
        {
            if (this.showSql)
                System.out.println(sql);
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = 1;
            for (ColumnAndValue cav : this.insertOrUpdateFields)
            {
                if (idAuto && this.idColumn != null && this.idColumn.equals(cav.getColumn()))
                    continue;
                this.setPreparedStatement(ps, i, cav.getValue());
                i++;
            }
            num = ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("insert->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(null, ps, conn);
        }
        return num;
    }


    /**
     * insert操作,取insertOrUpdateFields中的值插入数据库，并且返回插入的id
     *
     * @return
     */
    public int insertReturnID()
    {
        if (this.insertOrUpdateFields == null) { throw new RuntimeException("必须设置需要插入的字段！"); }
        String sql = this.getInsertSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int id = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            int i = 1;
            for (ColumnAndValue cav : this.insertOrUpdateFields)
            {
                if (idAuto && this.idColumn != null && this.idColumn.equals(cav.getColumn()))
                    continue;
                this.setPreparedStatement(ps, i, cav.getValue());
                i++;
            }
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();

            if (rs.next())
                id = rs.getInt(1);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("insertReturnID->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return id;
    }


    /**
     * update操作,将参数对象的值update到数据库
     *
     * @param <T>
     * @param t
     * @return
     */
    public <T> int update(T t)
    {
        this.setBeanToInsertOrUpdateFields(t);
        return this.update();
    }


    /**
     * update操作,取insertOrUpdateFields中的值update到数据库
     *
     * @return
     */
    public int update()
    {
        if (this.insertOrUpdateFields == null) { throw new RuntimeException("必须设置需要插入的字段！"); }
        String sql = this.getUpdateSql();
        Connection conn = null;
        PreparedStatement ps = null;
        int num = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = 1;
            for (ColumnAndValue cav : this.insertOrUpdateFields)
            {
                if (this.idAuto && null != this.idColumn && cav.getColumn().equalsIgnoreCase(this.idColumn))
                    continue;
                this.setPreparedStatement(ps, i, cav.getValue());
                i++;
            }
            this.setWhereValues(ps, i);
            num = ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("update->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(null, ps, conn);
        }
        return num;
    }


    /**
     * update操作,带乐观锁
     *
     * @param <T>
     * @param t
     * @return
     */
    public <T> int updateWithVersion(T t)
    {
        this.setBeanToInsertOrUpdateFields(t);
        return this.updateWithVersion(t, "version");
    }


    /**
     * update操作,带乐观锁
     *
     * @param <T>
     * @param t
     * @param versionColumn
     * @return
     */
    public <T> int updateWithVersion(T t, String versionColumn)
    {
        if (this.insertOrUpdateFields == null)
        {
            this.setBeanToInsertOrUpdateFields(t);
        }
        Field f = null;
        try
        {
            f = t.getClass().getDeclaredField(versionColumn);
            f.setAccessible(true);
            int i = this.updateWithVersion(versionColumn, f.getInt(t));

            f.set(t, (Integer) f.get(t) + 1);

            return i;
        }
        catch (SecurityException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * update操作,带乐观锁
     *
     * @param versionColumn
     * @param versionValue
     * @return
     */
    public int updateWithVersion(String versionColumn, int versionValue)
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        String where = this.getWhereSql();
        if (where == null || where.trim().length() == 0) { throw new RuntimeException(
                "必须设置where条件！"); }
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(this.table).append(" set ");
        for (ColumnAndValue cav : this.insertOrUpdateFields)
        {
            if (cav.getColumn().equals(versionColumn))
            {
                sql.append(versionColumn).append("=").append(versionColumn).append("+1,");
                continue;
            }
            sql.append(cav.getColumn()).append("=?,");
        }
        sql.setCharAt(sql.length() - 1, ' ');
        sql.append(where).append(" and ").append(versionColumn).append("=?");
        Connection conn = null;
        PreparedStatement ps = null;
        int num = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql.toString());
            int i = 1;
            for (ColumnAndValue cav : this.insertOrUpdateFields)
            {
                if (cav.getColumn().equals(versionColumn))
                {
                    continue;
                }
                this.setPreparedStatement(ps, i, cav.getValue());
                i++;
            }
            i = this.setWhereValues(ps, i);
            this.setPreparedStatement(ps, i, versionValue);
            num = ps.executeUpdate();
            if (num != 1)
                throw new StaleObjectStateException("对象已过期！");

            // 更新成功, 增加乐观锁属性值
        }
        catch (SQLException e)
        {
            throw new RuntimeException("updateWithVersion->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(null, ps, conn);
        }
        return num;
    }


    /**
     * delete操作
     *
     * @return
     */
    public <T> int delete(Class<T> c)
    {
        this.setTableName(c);
        return this.delete();
    }


    /**
     * delete操作
     *
     * @return
     */
    public int delete()
    {
        String sql = this.getDeleteSql();
        Connection conn = null;
        PreparedStatement ps = null;
        int num = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            this.setWhereValues(ps);
            num = ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("delete->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(null, ps, conn);
        }
        return num;
    }


    /**
     * 执行sql插入、删除或更新数据
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public int executeSql(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        int num = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object value : param)
                {
                    this.setPreparedStatement(ps, i, value);
                    i++;
                }
            }
            num = ps.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException("executeSql->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(null, ps, conn);
        }
        return num;
    }


    /**
     * 查询数量
     *
     * @return
     */
    public int selectCount()
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        String where = this.getWhereSql();
        String sql = new StringBuffer("select count(*) from ").append(this.table).append(where)
                .toString();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try
        {
            if (this.showSql)
                System.out.println(sql);
            conn = getConn();
            ps = conn.prepareStatement(sql);
            this.setWhereValues(ps);
            rs = ps.executeQuery();
            if (rs.next())
            {
                count = rs.getInt(1);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectCount->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return count;
    }


    /**
     * 查询数量
     *
     * @return
     */
    public <T> int selectCount(Class<T> c)
    {
        this.setTableName(c);
        return this.selectCount();
    }


    /**
     * 查询并返回一组bean
     *
     * @param <T>
     * @param c
     * @return
     */
    public <T> List<T> select(Class<T> c)
    {
        this.setTableName(c);
        this.setFields(c);
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<T>();
        try
        {
            if (this.showSql)
                System.out.println(sql);
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);
            if (this.pageNum > 0 && this.pageSize > 0)
            {
                this.setPreparedStatement(ps, i, this.pageNum * this.pageSize);
                i++;
                this.setPreparedStatement(ps, i, (pageNum - 1) * pageSize);
                i++;
            }
            rs = ps.executeQuery();
            while (rs.next())
            {
                T t = this.createBean(c, rs);
                list.add(t);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("select->数据库异常:" + e.getMessage(), e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("select->InstantiationException:" + e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("select->IllegalAccessException:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 查询返回一组对象数据
     *
     * @return
     */
    public List<Object[]> selectArray()
    {
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> list = new ArrayList<Object[]>();
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);
            if (this.pageNum > 0 && this.pageSize > 0)
            {
                this.setPreparedStatement(ps, i, (this.pageNum - 1) * this.pageSize);
                i++;
                this.setPreparedStatement(ps, i, this.pageNum * this.pageSize);
                i++;
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next())
            {
                Object[] o = new Object[columnCount];
                for (int j = 0; j < columnCount; j++)
                {
                    o[j] = rs.getObject(j + 1);
                }
                list.add(o);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectArray->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }


    /**
     * 查询返回一组map
     *
     * @return
     */
    public List<Map<String, Object>> selectMap()
    {
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);
            if (this.pageNum > 0 && this.pageSize > 0)
            {
                this.setPreparedStatement(ps, i, (this.pageNum - 1) * this.pageSize);
                i++;
                this.setPreparedStatement(ps, i, this.pageNum * this.pageSize);
                i++;
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next())
            {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < columnCount; j++)
                {
                    map.put(md.getColumnLabel(j + 1), rs.getObject(j + 1));
                }
                list.add(map);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectMap->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }


    /**
     * 查询一条记录返回bean
     *
     * @param <T>
     * @param c
     * @return
     */
    public <T> T selectSingle(Class<T> c)
    {
        this.setTableName(c);
        this.setFields(c);
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T t = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);

            rs = ps.executeQuery();
            if (rs.next())
            {
                t = this.createBean(c, rs);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSingle->数据库异常:" + e.getMessage(), e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("selectSingleRow->InstantiationException:" + e.getMessage(),
                    e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("selectSingleRow->IllegalAccessException:" + e.getMessage(),
                    e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return t;
    }


    /**
     * 查询一条记录返回对象数组
     *
     * @return
     */
    public Object[] selectSingleArray()
    {
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Object[] o = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);
            if (this.pageNum > 0 && this.pageSize > 0)
            {
                this.setPreparedStatement(ps, i, (this.pageNum - 1) * this.pageSize);
                i++;
                this.setPreparedStatement(ps, i, this.pageNum * this.pageSize);
                i++;
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (rs.next())
            {
                o = new Object[columnCount];
                for (int j = 0; j < columnCount; j++)
                {
                    o[j] = rs.getObject(j + 1);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSingleArray->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return o;
    }


    /**
     * 查询一条记录返回map
     *
     * @return
     */
    public Map<String, Object> selectSingleMap()
    {
        String sql = this.getSelectSql();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            int i = this.setWhereValues(ps);
            if (this.pageNum > 0 && this.pageSize > 0)
            {
                this.setPreparedStatement(ps, i, (this.pageNum - 1) * this.pageSize);
                i++;
                this.setPreparedStatement(ps, i, this.pageNum * this.pageSize);
                i++;
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (rs.next())
            {
                map = new HashMap<String, Object>();
                for (int j = 0; j < columnCount; j++)
                {
                    map.put(md.getColumnLabel(j + 1), rs.getObject(j + 1));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSingleMap->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return map;
    }


    /**
     * 用sql语句查询数量
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public int selectSqlCount(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        int count = 0;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            if (rs.next())
            {
                count = rs.getInt(1);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlCount->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return count;
    }

    /**
     * 用sql语句查询返回一组对象
     *
     * @param <T>
     * @param c
     * @param sql
     * @param param
     *            sql中问号对应的值 最后两个为 pageNum（当前页） 和 pageSize（页大小）
     * @return
     */
    public <T> List<T> selectSql(Class<T> c, String sql, String orderby, Object... param)
    {
        this.setFields(c);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<T> list = new ArrayList<T>();
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (null != param && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            while (rs.next())
            {
                T t = this.createBean(c, rs);
                list.add(t);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSql->数据库异常:" + e.getMessage(), e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("selectSql->InstantiationException:" + e.getMessage(), e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("selectSql->IllegalAccessException:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 用sql语句查询返回一组对象数组
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public List<Object[]> selectSqlArray(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Object[]> list = new ArrayList<Object[]>();

        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next())
            {
                Object[] o = new Object[columnCount];
                for (int j = 0; j < columnCount; j++)
                {
                    o[j] = rs.getObject(j + 1);
                }
                list.add(o);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlArray->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }


    /**
     * 用sql查询返回一组键值对
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public List<Map<String, Object>> selectSqlMap(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();

        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            while (rs.next())
            {
                Map<String, Object> map = new HashMap<String, Object>();
                for (int j = 0; j < columnCount; j++)
                {
                    map.put(md.getColumnLabel(j + 1), rs.getObject(j + 1));
                }
                list.add(map);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlMap->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return list;
    }

    /**
     * 用sql语句查询一条记录返回bean
     *
     * @param <T>
     * @param c
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public <T> T selectSqlSingle(Class<T> c, String sql, Object... param)
    {
        this.setFields(c);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        T t = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            if (rs.next())
            {
                t = this.createBean(c, rs);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlSingle->数据库异常:" + e.getMessage(), e);
        }
        catch (InstantiationException e)
        {
            throw new RuntimeException("selectSqlSingle->InstantiationException:" + e.getMessage(),
                    e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException("selectSqlSingle->IllegalAccessException:" + e.getMessage(),
                    e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return t;
    }


    /**
     * 用sql语句查询一条记录返回对象数组
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public Object[] selectSqlSingleArray(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Object[] oa = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (rs.next())
            {
                oa = new Object[columnCount];
                for (int j = 0; j < columnCount; j++)
                {
                    oa[j] = rs.getObject(j + 1);
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlSingleArray->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return oa;
    }


    /**
     * 用sql语句查询一条记录返回键值对
     *
     * @param sql
     * @param param
     *            sql中问号对应的值
     * @return
     */
    public Map<String, Object> selectSqlSingleMap(String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Map<String, Object> map = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            ResultSetMetaData md = rs.getMetaData();
            int columnCount = md.getColumnCount();
            if (rs.next())
            {
                map = new HashMap<String, Object>();
                for (int j = 0; j < columnCount; j++)
                {
                    map.put(md.getColumnLabel(j + 1), rs.getObject(j + 1));
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectSqlSingleMap->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return map;
    }


    /**
     * 根据sql语句查询，并自定义实现对ResultSet的操作
     *
     * @param callBack
     * @param sql
     * @param param
     * @return
     */
    public Object selectDoInResultSet(CallBack callBack, String sql, Object... param)
    {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        Object object = null;
        try
        {
            if (this.showSql)
            {
                System.out.println(sql);
            }
            conn = getConn();
            ps = conn.prepareStatement(sql);
            if (param != null && param.length > 0)
            {
                int i = 1;
                for (Object o : param)
                {
                    this.setPreparedStatement(ps, i, o);
                    i++;
                }
            }
            rs = ps.executeQuery();
            object = callBack.doInResultSet(rs);
        }
        catch (SQLException e)
        {
            throw new RuntimeException("selectDoInResultSet->数据库异常:" + e.getMessage(), e);
        }
        finally
        {
            ProxoolDBUtils.close(rs, ps, conn);
        }
        return object;
    }


    private <T> void setBeanToInsertOrUpdateFields(T t)
    {
        Class<?> c = t.getClass();
        this.setTableName(c);
        for (Field f : c.getDeclaredFields())
        {
            if (this.fields != null && !this.fields.contains(f.getName()))
            {// 设置了fields并且不在其中的跳过
                continue;
            }

            if (Modifier.isTransient(f.getModifiers()))// 修饰符中是否包含 transient
                continue;

            f.setAccessible(true);
            try
            {
                this.updateField(f.getName(), f.get(t));
            }
            catch (IllegalArgumentException e) { throw new RuntimeException(e); }
            catch (IllegalAccessException e) { throw new RuntimeException(e); }
        }
    }


    private String getInsertSql()
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        StringBuffer sql = new StringBuffer();
        sql.append("insert into ").append(this.table).append("(");
        for (ColumnAndValue cav : this.insertOrUpdateFields)
        {
            if (idAuto && this.idColumn != null && this.idColumn.equals(cav.getColumn()))
                continue;
            sql.append(cav.getColumn()).append(",");
        }
        sql.setCharAt(sql.length() - 1, ')');
        sql.append(" values(");
        for (ColumnAndValue cav : this.insertOrUpdateFields)
        {
            if (idAuto && this.idColumn != null && this.idColumn.equals(cav.getColumn()))
                continue;
            sql.append("?").append(",");
        }
        sql.setCharAt(sql.length() - 1, ')');
        return sql.toString();
    }

    private String getUpdateSql()
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        String where = this.getWhereSql();
        if (where == null || where.trim().length() == 0) { throw new RuntimeException(
                "必须设置where条件！"); }
        StringBuffer sql = new StringBuffer();
        sql.append("update ").append(this.table).append(" set ");
        for (ColumnAndValue cav : this.insertOrUpdateFields)
        {
            if (this.idAuto && null != this.idColumn && cav.getColumn().equalsIgnoreCase(this.idColumn))
                continue;
            sql.append(cav.getColumn()).append("=?,");
        }
        sql.setCharAt(sql.length() - 1, ' ');
        sql.append(where);
        return sql.toString();
    }


    private String getDeleteSql()
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        String where = this.getWhereSql();
        if (where == null || where.trim().length() == 0) { throw new RuntimeException(
                "必须设置where条件！"); }
        String sql = new StringBuffer("delete from ").append(this.table).append(where).toString();
        return sql;
    }


    private String getSelectSql()
    {
        if (this.table == null || this.table.trim().length() == 0) { throw new RuntimeException(
                "必须设置表名！"); }
        StringBuffer sql = new StringBuffer();
        sql.append("select ");
        if (this.fields == null)
        {
            sql.append("* ");
        }
        else
        {
            for (String field : this.fields)
            {
                sql.append(field).append(",");
            }
        }
        sql.setCharAt(sql.length() - 1, ' ');
        sql.append(" from ").append(this.table).append(this.getWhereSql());
        if (this.orderBy != null && this.orderBy.trim().length() != 0)
        {
            sql.append(" order by ").append(this.orderBy);
        }
        if (this.pageNum > 0 && this.pageSize > 0)
        {
            sql.append(" limit ?, ?");
        }
        return sql.toString();
    }

    private String getWhereSql()
    {
        StringBuffer where = new StringBuffer(" where ");
        if (this.where != null)
        {
            for (ColumnAndValue cav : this.where)
            {
                where.append(cav.getColumn()).append(" ").append(cav.getOperator()).append(
                        " ? and ");
            }
        }
        if (this.whereNull != null)
        {
            for (ColumnAndValue cav : this.whereNull)
            {
                where.append(cav.getColumn());
                switch ((ColumnNull) cav.getValue())
                {
                    case IsNull:
                        where.append(" is null and ");
                        break;
                    case IsNotNull:
                        where.append(" is not null and ");
                        break;
                }
            }
        }
        if (where.length() <= 7) { return ""; }
        return where.substring(0, where.length() - 4).toString();
    }


    private int setWhereValues(PreparedStatement ps) throws SQLException
    {
        return this.setWhereValues(ps, 1);
    }


    private int setWhereValues(PreparedStatement ps, int i) throws SQLException
    {
        if (this.where != null)
        {
            for (ColumnAndValue cav : this.where)
            {
                this.setPreparedStatement(ps, i, cav.getValue());
                i++;
            }
        }
        return i;
    }


    private void setPreparedStatement(PreparedStatement ps, int index, Object value)
            throws SQLException
    {
        if (value == null)
        {
            ps.setObject(index, null);
            return;
        }
        if (value instanceof Integer)
        {
            ps.setInt(index, (Integer) value);
            return;
        }
        if (value instanceof String)
        {
            ps.setString(index, (String) value);
            return;
        }
        if (value instanceof java.util.Date)
        {
            ps.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
            return;
        }
        if (value instanceof Double)
        {
            ps.setDouble(index, (Double) value);
            return;
        }
        if (value instanceof Long)
        {
            ps.setLong(index, (Long) value);
            return;
        }
        if (value instanceof Byte)
        {
            ps.setByte(index, (Byte) value);
            return;
        }
        if (value instanceof Float)
        {
            ps.setFloat(index, (Float) value);
            return;
        }
        if (value instanceof Short)
        {
            ps.setShort(index, (Short) value);
            return;
        }
        if (value instanceof StringReader)
        {
            ps.setCharacterStream(index, (StringReader) value);
            return;
        }

        ps.setObject(index, value);
    }


    private <T> T createBean(Class<T> c, ResultSet rs) throws InstantiationException,
            IllegalAccessException, IllegalArgumentException, SQLException
    {
        T t = c.newInstance();
        for (Field f : c.getDeclaredFields())
        {
            if (this.fields != null && !this.fields.contains(f.getName()))
            {
                continue;
            }

            if (Modifier.isTransient(f.getModifiers()))// 修饰符中包含 transient
                continue;

            f.setAccessible(true);
            String type = f.getType().getName();
            if ("int".equals(type))
            {
                f.set(t, rs.getInt(f.getName()));
            }
            else if ("java.lang.String".equals(type))
            {
                f.set(t, rs.getString(f.getName()));
            }
            else if ("java.util.Date".equals(type))
            {
                f.set(t, rs.getTimestamp(f.getName()));
            }
            else if ("double".equals(type))
            {
                f.set(t, rs.getDouble(f.getName()));
            }
            else if ("java.lang.Integer".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getInt(f.getName()));
            }
            else if ("java.lang.Double".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getDouble(f.getName()));
            }
            else if ("long".equals(type))
            {
                f.set(t, rs.getLong(f.getName()));
            }
            else if ("java.lang.Long".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getLong(f.getName()));
            }
            else if ("byte".equals(type))
            {
                f.set(t, rs.getByte(f.getName()));
            }
            else if ("java.lang.Byte".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getByte(f.getName()));
            }
            else if ("float".equals(type))
            {
                f.set(t, rs.getFloat(f.getName()));
            }
            else if ("java.lang.Float".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getFloat(f.getName()));
            }
            else if ("short".equals(type))
            {
                f.set(t, rs.getShort(f.getName()));
            }
            else if ("java.lang.Short".equals(type))
            {
                f.set(t, rs.getObject(f.getName()) == null ? null : rs.getShort(f.getName()));
            }
            else
            {
                f.set(t, rs.getObject(f.getName()));
            }
        }
        return t;
    }


    private <T> void setTableName(Class<T> c)
    {
        if (this.table != null && this.table.trim().length() != 0) { return; }
        this.table = c.getSimpleName();

        String name = null;

        try
        {
            Field tableNameFile = c.getDeclaredField("TABLE_NAME");
            name = (String) tableNameFile.get(c);
        } catch (Exception e)
        {

        }

        if (name == null || name.trim().length() == 0) { return; }
        this.table = name;
    }


    private <T> void setFields(Class<T> c)
    {
        if (this.fields != null) { return; }
        this.fields = new ArrayList<String>();
        for (Field f : c.getDeclaredFields())
        {
            if (!this.annotation)
            {
                this.fields.add(f.getName());
                continue;
            }

            if (Modifier.isTransient(f.getModifiers()))
                continue;

            if (Modifier.isTransient(f.getModifiers()))
                continue;

            this.fields.add(f.getName());
        }
    }


    /**
     * 获得连接
     * @throws java.sql.SQLException
     *             added by chenhang 2013-01-14
     */
    private Connection getConn() throws SQLException
    {
        if (this.pool != null && this.pool.length() > 0) { return ProxoolDBUtils.getConn(this.pool, true); }
        return ProxoolDBUtils.getConn();
    }


    /**
     * 用selectDoInResultSet方法查询时回调接口
     *
     * @author wb
     *
     */
    public static interface CallBack
    {
        public Object doInResultSet(ResultSet rs) throws SQLException;
    }

    /**
     * 枚举类型，定义排序的升序还是降序
     */
    public enum OrderBySort
    {
        ASC, DESC
    }

    /**
     * 枚举类型，定义列值是否为null
     */
    public enum ColumnNull
    {
        IsNull, IsNotNull
    }

    /**
     * 数据版本与数据库中版本不一致
     *
     * @author wb
     *
     */
    public class StaleObjectStateException extends RuntimeException
    {

        private static final long serialVersionUID = -4716718879501953166L;


        public StaleObjectStateException(String msg)
        {
            super(msg);
        }
    }


    /**
     * 内部类，存放列和对应的值
     */
    private class ColumnAndValue
    {
        private String column;

        private String operator;

        private Object value;


        public String getColumn()
        {
            return column;
        }


        public void setColumn(String column)
        {
            this.column = column;
        }


        public String getOperator()
        {
            return operator;
        }


        public void setOperator(String operator)
        {
            this.operator = operator;
        }


        public Object getValue()
        {
            return value;
        }


        public void setValue(Object value)
        {
            this.value = value;
        }
    }
}
