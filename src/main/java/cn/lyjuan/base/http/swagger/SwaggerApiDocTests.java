package cn.lyjuan.base.http.swagger;

import cn.lyjuan.base.util.JsonUtils;
import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;
import org.yaml.snakeyaml.Yaml;
import springfox.documentation.swagger2.web.Swagger2Controller;

import java.io.File;
import java.io.FileWriter;
import java.util.*;

/**
 * Created by chad on 2017/1/6.
 */
public class SwaggerApiDocTests
{
    /**
     * 生成swagger扫描插件
     * <p>
     * 需要在Spring加载时生成该Bean
     * </p>
     *
     * @param springSwaggerConfig
     * @param pkgPattern
     * @return
     */
    public SwaggerSpringMvcPlugin customImplementation(SpringSwaggerConfig springSwaggerConfig, String pkgPattern)
    {
        return new SwaggerSpringMvcPlugin(springSwaggerConfig)
                .includePatterns(pkgPattern);
    }

    /**
     * 将swagger接口返回的JSON内容转换为yaml，并保存到classpath下的{@code saveFile}文件中
     * <p>
     *     swagger的文档可使用{@link Swagger2Controller#DEFAULT_URL}接口获取
     * </p>
     * @param json
     * @param saveFile 如果文件存在会直接覆盖；自动创建目录
     * @throws Exception
     */
    public void saveApiDoc(String json, String saveFile) throws Exception
    {
        Map map = JsonUtils.from(Map.class, json);

        // 对整个文档进行操作
        modifyDoc(map);

        // json 转 yaml
        Yaml yaml = new Yaml();

        File file = new File(saveFile);
        System.out.println(file.getAbsolutePath());
        if (!file.getParentFile().isDirectory())
            file.getParentFile().mkdirs();
        file.createNewFile();
        yaml.dump(map, new FileWriter(file));
    }

    /**
     * 操作整个文档
     *
     * @param doc
     */
    protected void modifyDoc(Map doc)
    {
        // 去掉Spring error信息
        removeSpringErrores(doc);

        Map paths = (Map) doc.get("paths");
        Iterator<Map.Entry> it = paths.entrySet().iterator();

        while (it.hasNext())
        {
            modifyPath((Map) it.next().getValue());
        }
    }

    /**
     * 操作api请求信息
     *
     * @param path
     */
    protected void modifyPath(Map path)
    {
        Iterator<Map.Entry> methodIt = path.entrySet().iterator();
        while (methodIt.hasNext())
        {
            modifyMethod((Map) methodIt.next().getValue());
        }
    }

    /**
     * 操作请求的方法
     *
     * @param method
     */
    protected void modifyMethod(Map method)
    {
        List<Map> parameters = (List) method.get("parameters");

        for (Map m : parameters)
        {
            modifyPath(m);
        }
    }

    /**
     * 操作方法的每个参数
     *
     * @param param
     */
    protected void modifyParam(Map param)
    {

    }

    /**
     * 拷贝方法参数
     *
     * @param map
     * @return
     */
    protected Map copyParam(Map map)
    {
        if (null == map) return null;

        Map nm = new HashMap(map.size());
        if (map.isEmpty()) return nm;
        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry e = it.next();
            nm.put(e.getKey(), e.getValue());
        }

        return nm;
    }

    /**
     * 去掉SpringError信息
     *
     * @param map
     */
    private void removeSpringErrores(Map map)
    {
        List<Map> tags = (List) map.get("tags");
        for (Iterator<Map> it = tags.iterator(); it.hasNext(); )
        {
            Map m = it.next();
            String name = (String) m.get("name");
            if ("basic-error-controller".equalsIgnoreCase(name))
                it.remove();
        }
        Map<String, Map> paths = (Map) map.get("paths");
        for (Iterator<Map.Entry<String, Map>> it = paths.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry<String, Map> entry = it.next();
            if ("/error".equalsIgnoreCase(entry.getKey()))
                it.remove();
        }
    }
}


































