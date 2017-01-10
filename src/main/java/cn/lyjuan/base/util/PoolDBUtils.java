package cn.lyjuan.base.util;


import org.logicalcobwebs.proxool.ProxoolException;
import org.logicalcobwebs.proxool.configuration.JAXPConfigurator;

import java.io.InputStreamReader;
import java.sql.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * @author Chad
 *         2014-01-09 修改没有启用事务的获取数据库连接时也加入 ThreadLocal bug.
 * @version 2.2
 * @descript 本工具类只适用于 struts 框架项目,不适用 servlet 项目
 * @deprecated 本工具有重大逻辑问题，不再使用，请使用{@link ProxoolDBUtils}
 */
public final class PoolDBUtils
{
    /**
     * 是否开启事务
     */
    private static boolean transaction = false;

    /**
     * 连接池名称前缀
     */
    public static final String base_proxool_pre = "proxool.";

    /**
     * 默认的连接池
     */
    public static final String DEFAULT_PROXOOL = "DBPool";

    /**
     * 存放 Servlet 本地线程连接池的Map，KEY为连接池名称，VALUE 为保存的连接 {@code java.sql.Connection}
     */
    private static Map<String, ThreadLocal<Connection>> threadLocalMap = new HashMap<String, ThreadLocal<Connection>>();

    public static boolean isAuto()
    {
        return transaction;
    }


    public static void setAuto(boolean auto)
    {
        PoolDBUtils.transaction = auto;
    }

    public static boolean isTransaction(){return transaction; }

    /**
     * 启动事务
     */
    public static void startTransaction(){PoolDBUtils.transaction = true; }

    /**
     * 关闭事务
     * 先提交所有事务
     * 再关闭
     */
    public static void stopTransaction(){PoolDBUtils.commit();PoolDBUtils.close();PoolDBUtils.transaction = false;}


    static
    {
        try
        {
            Class.forName("org.logicalcobwebs.proxool.ProxoolDriver");
        } catch (ClassNotFoundException e)
        {
            new RuntimeException("load database driver class error:org.logicalcobwebs.proxool.ProxoolDriver", e);
        }
    }


    /**
     * 根据连接池名称获取本地线程对象
     *
     * @param poolName 连接池名称
     * @return 如果没有相应的本地线程对象，则创建之
     */
    public static ThreadLocal<Connection> getThreadLoca(String poolName)
    {
        ThreadLocal<Connection> th = threadLocalMap.get(poolName);

        if (th == null)
        {
            th = new ThreadLocal<Connection>();
            threadLocalMap.put(poolName, th);
        }

        return th;
    }


    /**
     * 获取默认的数据库连接，连接池名称为{@code DEFAULT_PROXOOL}
     *
     * @return 返回数据库连接对象，如果本地连接池中没有，则新建之
     * @throws java.sql.SQLException
     */
    public static Connection getConnection()
    {
        return getConnection(DEFAULT_PROXOOL);
    }


    /**
     * 获取自定义的连接池本地线程中保持的数据库连接对象
     *
     * @param pool 自定义的连接池名称
     * @return 返回数据库连接对象，如果本地连接池中没有，则新建之
     * @throws java.sql.SQLException
     */
    public static Connection getConnection(String pool)
    {
        Connection conn = null;
        try
        {
            if (!transaction)// 没有开启事务,直接获取
            {
                return DriverManager.getConnection(base_proxool_pre + pool);
            }

            ThreadLocal<Connection> th = getThreadLoca(pool);

            conn = th.get();

            if (conn == null || conn.isClosed())
            {
                conn = DriverManager.getConnection(base_proxool_pre + pool);
                th.set(conn);
            }

            conn.setAutoCommit(false);// 开启事务

            // 防止本线程中其他线程获取同一连接
            // th.remove();

        } catch (SQLException e)
        {
            throw new RuntimeException("get database connection error", e);
        }
        return conn;
    }


    /**
     * 提交所有的数据库连接
     */
    public static void commit()
    {

        String pool = null;
        for (Iterator<String> it = threadLocalMap.keySet().iterator(); it.hasNext(); )
        {
            pool = it.next();
            commit(pool);
        }
    }


    /**
     * 提交连接池保持的数据库连接对象
     *
     * @param pool 连接池名称
     */
    public static void commit(String pool)
    {
        Connection conn = getThreadLoca(pool).get();
        try
        {
            if (conn == null || conn.isClosed())
            {
                return;
            }
            conn.commit();
        } catch (SQLException e)
        {
            throw new RuntimeException("commit database connection error", e);
        }
    }


    /**
     * 关闭连接池保持的数据库连接对象
     *
     * @param pool 连接池名称
     */
    public static void close(String pool)
    {

        ThreadLocal<Connection> th = getThreadLoca(pool);
        Connection conn = th.get();

        try
        {
            if (conn == null || conn.isClosed())
            {
                return;
            }

            conn.setAutoCommit(true);

        } catch (SQLException e)
        {
            throw new RuntimeException(e);
        }

        th.remove();

        try
        {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close database connection error", e);
        }

        conn = null;
    }


    /**
     * 关闭所有的数据库连接
     */
    public static void close()
    {
        String pool = null;
        for (Iterator<String> it = threadLocalMap.keySet().iterator(); it.hasNext(); )
        {
            pool = it.next();
            close(pool);
        }
    }


    /**
     * 回滚所有连接
     */
    public static void rollback()
    {
        for (Iterator<String> it = threadLocalMap.keySet().iterator(); it.hasNext(); )
            rollback(it.next());
    }


    public static void rollback(String pool)
    {
        Connection conn = getThreadLoca(pool).get();
        try
        {
            if (conn == null || conn.isClosed())
            {
                return;
            }

            conn.rollback();
        } catch (SQLException e)
        {
            throw new RuntimeException("rollback database connection error", e);
        }
    }


    public static void close(ResultSet rs)
    {
        try
        {
            if (rs != null)
            {
                rs.close();
            }
        } catch (SQLException e)
        {
            throw new RuntimeException("close database connection error", e);
        }
    }


    public static void close(Statement ps)
    {
        try
        {
            if (ps != null && !ps.isClosed())
                ps.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close database statement error", e);
        }
    }


    /**
     * 空的数据库连接关闭方法，本项目不需手动关闭数据库连接，次方法用于代码重用到其他项目时防止忘记关闭连接
     *
     * @param conn
     */
    public static void close(Connection conn)
    {
        // 开启事务，不关闭
        if (transaction) return;

        try
        {
            if (conn != null && !conn.isClosed())
                conn.close();
        } catch (SQLException e)
        {
            throw new RuntimeException("close database connection error", e);
        }
    }

    public static void close(ResultSet rs, Statement ps, Connection conn)
    {
        close(rs);close(ps); close(conn);
    }

    /**
     * 用于测试的时候手动开启连接池
     */
    public static void startProxool()
    {
        startProxool("proxool.xml");
    }


    /**
     * 用于测试的时候手动开启连接池
     */
    public static void startProxool(String file)
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
}