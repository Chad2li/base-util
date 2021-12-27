package cn.lyjuan.base.util.field;

import cn.lyjuan.base.util.JsonUtils;
import cn.lyjuan.base.util.ReflectUtils;
import cn.lyjuan.base.util.StringUtils;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 配置结构转换工具
 *
 * @author chad
 * @date 2021/12/22 14:59
 * @since 1 by chad at 2021/12/22 新增
 */
public class FieldUtil {
    /**
     * 解析结构配置
     *
     * @param analysis 配置结构，Json格式
     * @return
     */
    public static List<FieldApiVo> parse(String analysis) {
        return JsonUtils.from(new TypeToken<List<FieldApiVo>>() {
        }.getType(), analysis);
    }

    /**
     * 直接从类中解析为Json格式的结构字符串，
     *
     * @param cls
     * @return
     */
    public static String toJson(Class cls) {
        List<FieldApiVo> vals = toFields(cls);

        return format(vals);
    }

    /**
     * 将结构转换为Json格式用于存储
     *
     * @param analysis 配置结构
     * @return
     */
    public static String format(List<FieldApiVo> analysis) {
        return JsonUtils.to(analysis);
    }

    public static List<FieldApiVo> toFields(Class cls) {
        List<FieldApiVo> list = new ArrayList<>();
        Map<String, Field> fmap = ReflectUtils.fields(cls);
        for (Map.Entry<String, Field> m : fmap.entrySet()) {
            FieldApiVo f = new FieldApiVo();
            Field field = m.getValue();
            FieldProperties fp = field.getAnnotation(FieldProperties.class);
            if (null == fp) {
                throw new IllegalStateException(cls.getSimpleName() + " need annotation: " + FieldProperties.class.getSimpleName() + ", but null");
            }

            if (!StringUtils.isNull(fp.name())) {
                f.setName(fp.name());
            } else {
                f.setName(m.getKey());
            }

            f.setTitle(fp.title());
            f.setRemark(fp.remark());
            f.setMax(fp.max());
            f.setMin(fp.min());
            f.setNotNull(fp.notNull());


            Field val = m.getValue();
            Class fCls = val.getType();
            if (fCls == Integer.class
                    || fCls == int.class
                    || fCls == Byte.class
                    || fCls == byte.class
                    || fCls == Short.class
                    || fCls == short.class
                    || fCls == Long.class
                    || fCls == long.class
                    || fCls == Double.class
                    || fCls == double.class
                    || fCls == Float.class
                    || fCls == float.class) {
                f.setType(ItemTypeEnum.NUMBER);
            } else if (fCls == String.class) {
                f.setType(ItemTypeEnum.STRING);
            } else if (fCls.isArray()) {
                f.setType(ItemTypeEnum.ARRAY);
                Class<?> actualCls = fCls.getComponentType();
                f.setSubFields(toFields(actualCls));
            } else if (List.class.isAssignableFrom(fCls)) {
                f.setType(ItemTypeEnum.ARRAY);
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    Type actual = ((ParameterizedType) type).getActualTypeArguments()[0];
                    f.setSubFields(toFields((Class) actual));
                } else {
                    throw new IllegalStateException(f.getName() + "[" + fCls.getName() + "] must have generic class");
                }
            } else {
                f.setType(ItemTypeEnum.OBJECT);
                f.setSubFields(toFields(fCls));
            }

            list.add(f);
        }

        return list;
    }
}
