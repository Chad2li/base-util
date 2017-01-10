package cn.lyjuan.base.util;

import java.sql.*;

/**
 * 数据库连接操作，适应单线程，简单的应用环境
 * Created by ly on 2015/2/2.
 */
public class NativeDBUtils
{
    /**
     * 用于判断是否已经加载过驱动
     */
    private static Class driverClass;

    /**
     * 数据库连接地址
     */
    private static String url;

    /**
     * 数据库连接用户名
     */
    private static String username;

    /**
     * 数据库连接密码
     */
    private static String password;

    /**
     * 连接失败重试次数
     */
    private static int connFailTryCount = 3;

    /**
     * 初始化连接数据库连接环境
     *
     * @param param_driver   驱动全类名
     * @param param_url      连接URL地址
     * @param param_username 连接用户名
     * @param param_password 连接密码
     */
    public static void init(String param_driver, String param_url, String param_username, String param_password)
    {
        loadDriver(param_driver);// 加载驱动

        url = param_url;
        username = param_username;
        password = param_password;
    }

    /**
     * 初始化连接数据库连接环境
     *
     * @param param_driver      驱动全类名
     * @param param_url         连接URL地址
     * @param param_username    连接用户名
     * @param param_password    连接密码
     * @param connFailTryCount  获取数据库连接失败重试次数，默认为 3
     */
    public static void init(String param_driver, String param_url, String param_username, String param_password, int connFailTryCount)
    {
        NativeDBUtils.connFailTryCount = connFailTryCount;
        init(param_driver, param_url, param_username, param_password);
    }

    /**
     * 加载数据库驱动，只需执行一次
     *
     * @param className 驱动全类名
     */
    private static void loadDriver(String className)
    {
        try
        {
            driverClass = Class.forName(className);

        } catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * 使用自动提交方式获取连接信息
     * @return
     */
    public static Connection getConn()
    {
        return getConn(true);
    }


    /**
     * 获取连接信息
     * @param autocommit    是否自动提交
     * @return
     */
    public static Connection getConn(boolean autocommit)
    {
        return getConn(connFailTryCount, autocommit);
    }

    private static Connection getConn(int tryCount, boolean autocommit)
    {
        if (null == driverClass)
            throw new RuntimeException("not found driver class");

        Connection conn = null;

        try
        {
            conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(autocommit);
        } catch (SQLException e)
        {
            try
            {
                NativeDBUtils.close(conn);
            } catch (Exception e2){e.printStackTrace();}// 压制异常

            if (--tryCount <= 0)
                throw new RuntimeException("database connection fail, check your url, username and password", e);
            else
                conn = getConn(tryCount, autocommit);
        }

        return conn;
    }

    /**
     * 回滚并关闭环境
     * @param rs
     * @param st
     * @param conn
     */
    public static void rollbackWithClose(ResultSet rs, Statement st, Connection conn)
    {
        rollback(conn);
        close(rs, st, conn);
    }

    /**
     * 提交并关闭环境
     * @param rs
     * @param st
     * @param conn
     */
    public static void commitWithClose(ResultSet rs, Statement st, Connection conn)
    {
        commit(conn);
        close(rs, st, conn);
    }

    /**
     * 回滚并关闭
     * @param conn
     */
    public static void rollbackWithClose(Connection conn)
    {
        rollback(conn);
        close(conn);
    }

    /**
     * 提交并关闭
     * @param conn
     */
    public static void commitWithClose(Connection conn)
    {
        commit(conn);
        close(conn);
    }

    /**
     * 回滚
     * @param conn
     */
    public static void rollback(Connection conn)
    {
        try
        {
            if (null != conn && !conn.isClosed())
                conn.rollback();
        } catch (SQLException e)
        {
            throw new RuntimeException("rollback database connection error", e);
        }
    }

    /**
     * 提交
     *
     * @param conn
     */
    public static void commit(Connection conn)
    {
        try
        {
            if (null != conn && !conn.isClosed())
                conn.commit();
        } catch (SQLException e)
        {
            throw new RuntimeException("commit database connection error", e);
        }
    }

    /**
     * 按顺序关闭数据库连接环境
     *
     * @param rs   结果集
     * @param st   预处理命令
     * @param conn 连接
     */
    public static void close(ResultSet rs, Statement st, Connection conn)
    {
        close(rs);
        close(st);
        close(conn);
    }

    /**
     * 关闭连接
     *
     * @param conn
     */
    public static void close(Connection conn)
    {
        try
        {
            if (null != conn && !conn.isClosed())
                conn.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close the database connection error", e);
        }
    }

    /**
     * 关闭结果集
     *
     * @param st
     */
    public static void close(Statement st)
    {
        try
        {
            if (null != st && !st.isClosed())
                st.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close the database statement error", e);
        }
    }

    /**
     * 关闭结果集
     *
     * @param rs
     */
    public static void close(ResultSet rs)
    {
        try
        {
            if (null != rs && !rs.isClosed())
                rs.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close the database resultset error", e);
        }
    }
}
