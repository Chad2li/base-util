package cn.lyjuan.base.util;

import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import java.io.InputStreamReader;
import java.sql.*;

/**
 * 数据库连接操作，适应单线程，简单的应用环境
 * Created by ly on 2015/2/2.
 */
public class ProxoolDBUtils
{
    /**
     * 连接池名称前缀
     */
    public static final String base_proxool_pre = "proxool.";

    /**
     * 默认的连接池
     */
    public static final String DEFAULT_PROXOOL = "DBPool";

    /**
     * 连接失败重试次数
     */
    private static final int CONN_FAIL_TRY_TIMES = 3;

    static
    {
        // 加载驱动
        try
        {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
        } catch (ClassNotFoundException e)
        {
            new RuntimeException("load database driver class error:org.logicalcobwebs.proxool.ProxoolDriver", e);
        }
    }

    /**
     * 初始化连接数据库连接环境
     */
    public static void init()
    {
        init("proxool.xml");
    }

    /**
     * 用于测试的时候手动开启连接池
     * @param file              Proxool 配置文件
     */
    public static void init(String file)
    {
        try
        {
            JAXPConfigurator.configure(new InputStreamReader(Thread.currentThread()
                    .getContextClassLoader().getResourceAsStream(file)), false);
        } catch (ProxoolException e)
        {
            throw new RuntimeException("load proxool config xml error:" + file, e);
        }
    }

    /**
     * 获取数据库连接
     * 1. 连接会自动提交
     * 2. 获取失败时自动重试，默认重试次数为{@code CONN_FAIL_TRY_TIMES}
     * 3. 使用默认的连接池名称，默认为 {@code DEFAULT_PROXOOL}
     * @return
     */
    public static Connection getConn()
    {
        return getConn(true);
    }


    /**
     * 获取数据库连接
     * @param autocommit    是否自动提交
     * @return
     */
    public static Connection getConn(boolean autocommit)
    {
        return getConn(CONN_FAIL_TRY_TIMES, DEFAULT_PROXOOL, autocommit);
    }

    /**
     * 获取数据库连接
     * @param pool
     * @param autocommit
     * @return
     */
    public static Connection getConn(String pool, boolean autocommit)
    {
        return getConn(CONN_FAIL_TRY_TIMES, pool, autocommit);
    }

    /**
     * 获取数据库连接
     * @param tryCount      失败重试次数
     * @param pool          数据库连接池名称
     * @param autocommit    是否开启自动提交功能
     * @return
     */
    public static Connection getConn(int tryCount, String pool, boolean autocommit)
    {
        Connection conn = null;

        try
        {
            conn = DriverManager.getConnection(base_proxool_pre + pool);
            conn.setAutoCommit(autocommit);
        } catch (SQLException e)
        {
            try
            {
                ProxoolDBUtils.close(conn);
            } catch (Exception e2){e.printStackTrace();}// 压制异常

            if (--tryCount <= 0)
                throw new RuntimeException("database connection fail, check your url, username and password", e);
            else
                conn = getConn(tryCount, pool, autocommit);
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
            {
                conn.setAutoCommit(true);// 恢复自动提交
                conn.close();
            }
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
