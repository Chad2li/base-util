package cn.lyjuan.base.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * 资源加载工具
 * 		<p>此工具加载 properties 文件中的资源到 Java 类中的相应变量中。</p>
 * 		<p>属性名要与 Java 变量名相对应，且以 "$_" 开头，变量修饰符固定为 public static。</p>
 * 		<p>
 * 		例如 constant.properties:<br>
 * 			$_ISDEBUG = false<br>
 * 			$_DBNAME = dbname<br>
 * 			$_DBPASSWORD = dbpwd<br>
 * 			$_DBURL = dburl<br>
 * 		</p>
 * 		<p>
 * 		Constant.java:<br>
 * 			public static boolean $_ISDEBUG;<br>
 *			public static String $_DBNAME;<br>
 *			public static String $_DBPASSWORD;<br>
 *			public static String $_DBURL;<br>
 *		</p>
 *		<p>
 *		资源加载方式为 Test.java：<br>
 *			CstUtils.load(Constant.class, "constant.properties");</p>
 *		</p>
 *		<p>
 *		当资源 $_ISDEUBG 为 true 时（测试环境），变量会被测试变量覆盖：<br>
 *			$_DBNAME = dbname<br>
 *			$_DENAME_DEUBG = dbnametest<br>
 *		$_DBNAME_DEBUG 会替代 $_DBNAME 加载到变量中。<br>
 *		默认情况下，非 DEBUG 模式，可配置文件 rscst.properties 中的 $_ISDEBUG 为 true 开启 DEBUG 模式<br/>
 *		rscst.properties 应该放于 classpath 下。<br/>
 *		</p>
 * @author 		Chad	
 * @version 	2014-09-16	2.0.0	修改 1.0.0 版本缺点
 *
 */
public class CstUtils
{
	private static Logger log = Logger.getLogger(CstUtils.class.getName());

	private static boolean isDebug = false;// 默认非debug模式

    public static void load(Class constant, String config)
    {
        Object obj = constant;

        Properties prop = loadProperties(config);
        Field[] fields = constant.getDeclaredFields();
        try
        {
            load(obj, config, prop, fields, true);
        } catch (Throwable a)
        {
            throw new RuntimeException(a);
        }
    }

    private static Properties loadProperties(String config)
	{
		Properties pro = new Properties();

		try
		{
			InputStream in = CstUtils.class.getClassLoader().getResourceAsStream(config);

			pro.load(in);
		} catch (IOException e)
		{
			throw new RuntimeException("load properties error: " + CstUtils.class.getClassLoader().getResource(config), e);
		}

		return pro;
	}

	private static void load(Object obj, String config, Properties prop, Field[] fields, boolean isDebug) throws IllegalArgumentException, IllegalAccessException
	{
		for (Field f : fields)
		{
			String name = f.getName();// 名称
			
			if (!name.startsWith("$_"))
			{
				log.fine(((Class<?>) obj).getSimpleName() + " >> 跳过非资源属性: " + name);
				continue;
			}
			
			int modifier = f.getModifiers();
			
			if (!Modifier.isStatic(modifier))// 用 static 修饰
			{
				log.fine(((Class<?>) obj).getSimpleName() + " >> 跳过非静态属性: " + name);
				continue;
			}
			
			if (Modifier.isFinal(modifier))// final 属性
			{
				log.fine(((Class<?>) obj).getSimpleName() + " >> 跳过final属性: " + name);
				continue;
			}
			
			if (!Modifier.isPublic(modifier))// 非 public
			{
				log.fine(((Class<?>)obj).getSimpleName() + " >> 跳过非public属性: " + name);
				continue;
			}
			
			Class<?> type = f.getType();// 类型
			
			String value = prop.getProperty(name);
			
			if (StringUtils.isNull(value))
				throw new NullPointerException(config + " 中没有属性: " + name+ " 或属性值为空");
			
			value = value.trim();
			
			//f.setAccessible(true);// 设置属性关闭安全访问机制
			if ("string".equalsIgnoreCase(type.getSimpleName()))// 字符串
				f.set(obj, value);
			
			else if ("integer".equalsIgnoreCase(type.getSimpleName())
					|| "int".equalsIgnoreCase(type.getSimpleName()))// 整数
				f.set(obj, Integer.parseInt(value));
			
			else if ("double".equalsIgnoreCase(type.getSimpleName()))// double
				f.set(obj, Double.parseDouble(value));
			
			else if ("boolean".equalsIgnoreCase(type.getSimpleName()))// boolean
				f.set(obj, Boolean.parseBoolean(value));
			
			else  if ("long".equalsIgnoreCase(type.getSimpleName())) // long
				f.set(obj, Long.parseLong(value));
			
			else
				throw new RuntimeException(((Class<?>)obj).getSimpleName() + "类中存在不支持的属性[" +
						name + "]类型[" + type.getName() + "]");
			
			log.info(((Class<?>)obj).getSimpleName() + " >> 加载资源 [" + name + "] == [" + value + "]");
		}
	}
}





















