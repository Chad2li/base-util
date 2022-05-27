package io.github.chad2li.baseutil.util.field;

import io.github.chad2li.baseutil.util.JsonUtils;
import io.github.chad2li.baseutil.util.ReflectUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import com.google.gson.reflect.TypeToken;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Field;
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
 * @since 2 by chad at 2022/1/8 增加 Hash 结构，优化结构JSON
 */
public class FieldUtil {
    /**
     * 解析结构配置
     *
     * @param analysis 配置结构，Json格式
     * @return
     */

    /**
     * 将JSON描述的结构还原为{@link FieldApiVo}
     *
     * @param analysis JSON格式的结构描述
     * @return cn.lyjuan.base.util.field.FieldApiVo 结构
     * @date 2022/1/8 19:53
     * @author chad
     * @since 2 by chad at 2022/1/8
     */
    public static FieldApiVo parse(String analysis) {
        return JsonUtils.from(new TypeToken<FieldApiVo>() {
        }.getType(), analysis);
    }

    /**
     * 将配置类格式化为JSON结构描述语言
     *
     * @param type 配置类
     * @return java.lang.String JSON结构描述语言
     * @date 2022/1/8 19:56
     * @author chad
     * @since 1 by chad create
     */
    public static String format(Type type) {
        FieldApiVo analysis = toFields(type);
        return format(analysis);
    }

    /**
     * 将结构转换为JSON结构描述语言
     *
     * @param analysis 结构
     * @return java.lang.String JSON结构描述语言
     * @date 2022/1/8 19:57
     * @author chad
     * @since 1 by chad create
     */
    public static String format(FieldApiVo analysis) {
        return JsonUtils.to(analysis);
    }

    /**
     * 生成配置类的配置结构
     *
     * @param type 配置类
     * @return cn.lyjuan.base.util.field.FieldApiVo
     * @date 2022/1/8 19:57
     * @author chad
     * @since 2 by chad at 2022/1/8
     */
    public static FieldApiVo toFields(Type type) {
        return toFields(type, null, null);
    }

    /**
     * 为属性生成配置类的配置结构
     *
     * @param type            类
     * @param field           类属性
     * @param fieldProperties 类的注解或手动指定的注解
     * @return cn.lyjuan.base.util.field.FieldApiVo 配置结构
     * @date 2022/1/8 20:04
     * @author chad
     * @since 2 by chad at 2022/1/8
     */
    public static FieldApiVo toFields(Type type, Field field, FieldProperties fieldProperties) {
        FieldApiVo fieldApiVo = new FieldApiVo();
        fieldApiVo.setType(parseType(type));

        if (null != field) {
            fieldProperties = field.getAnnotation(FieldProperties.class);
            // 属性一定要有 fp
            if (null == fieldProperties) {
                throw new IllegalStateException(type.getTypeName() + " need annotation: " + FieldProperties.class.getSimpleName() + ", but null");
            }
            if (!StringUtils.isNull(fieldProperties.name())) {
                fieldApiVo.setName(fieldProperties.name());
            } else {
                fieldApiVo.setName(field.getName());
            }
        }
        if (null == fieldProperties && type instanceof Class) {
            fieldProperties = (FieldProperties) ((Class) type).getAnnotation(FieldProperties.class);
        }

        // 根据 FieldProperties 设置结构属性
        if (null != fieldProperties) {
            fieldApiVo.setTitle(fieldProperties.title());
            if (StringUtils.isNull(fieldApiVo.getTitle())) {
                fieldApiVo.setTitle(fieldApiVo.getName());
            }
            fieldApiVo.setRemark(fieldProperties.remark());
            fieldApiVo.setMax(fieldProperties.max());
            fieldApiVo.setMin(fieldProperties.min());
            fieldApiVo.setNotNull(fieldProperties.notNull());
        }

        if (ItemTypeEnum.NUMBER == fieldApiVo.getType()
                || ItemTypeEnum.STRING == fieldApiVo.getType()) {
            return fieldApiVo;
        } else if (ItemTypeEnum.LIST == fieldApiVo.getType()) {
            if (type instanceof ParameterizedTypeImpl) {
                Type actualType = ((ParameterizedTypeImpl) type).getActualTypeArguments()[0];
                fieldApiVo.setSubField(toFields(actualType, null, null));
            } else {
                // Class
                Class<?> actualCls = ((Class) type).getComponentType();
                fieldApiVo.setSubField(toFields(actualCls, null, null));
            }
        } else if (ItemTypeEnum.HASH == fieldApiVo.getType()) {
            Type actualType = ((ParameterizedTypeImpl) type).getActualTypeArguments()[1];
            fieldApiVo.setSubField(toFields(actualType, null, null));
        } else {
            // OBJECT
            List<FieldApiVo> list = new ArrayList<>();
            Map<String, Field> fmap = ReflectUtils.fields((Class) type);
            for (Map.Entry<String, Field> m : fmap.entrySet()) {
                Field fieldMember = m.getValue();
                FieldApiVo f = toFields(m.getValue().getGenericType(), fieldMember, null);
                list.add(f);
            }
            fieldApiVo.setObjectFields(list);
        }

        return fieldApiVo;
    }

    /**
     * 将数据类型转为 {@link ItemTypeEnum}
     *
     * @param type 数据类型
     * @return cn.lyjuan.base.util.field.ItemTypeEnum
     * @date 2022/1/8 19:52
     * @author chad
     * @since 2 by chad create
     */
    private static ItemTypeEnum parseType(Type type) {
        if (type instanceof ParameterizedTypeImpl) {
            ParameterizedTypeImpl typeImpl = (ParameterizedTypeImpl) type;
            Type rawType = typeImpl.getRawType();
            if (!(rawType instanceof Class)) {
                // 结构太复杂
                throw new IllegalStateException("The structure is too complex for " + typeImpl.getTypeName());
            }
            Class cls = typeImpl.getRawType();
            if (cls.isArray()) {
                return ItemTypeEnum.LIST;
            } else if (List.class.isAssignableFrom(cls)) {
                return ItemTypeEnum.LIST;
            } else if (Map.class.isAssignableFrom(cls)) {
                return ItemTypeEnum.HASH;
            }
        } else if (type instanceof Class) {
            Class fCls = (Class) type;
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
                return ItemTypeEnum.NUMBER;
            } else if (fCls == String.class) {
                return ItemTypeEnum.STRING;
            } else {
                return ItemTypeEnum.OBJECT;
            }
        }
        throw new IllegalStateException("Unknown type: " + type.getTypeName());
    }
}
