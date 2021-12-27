package cn.lyjuan.base.util.field;

import cn.lyjuan.base.util.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Data;
import lombok.Generated;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chad
 * @date 2021/12/22 15:05
 * @since
 */
public class FieldUtilTest {

    @Test
    public void cls() throws Exception {
        Class cls = FieldDemo.class;

        Field f = cls.getDeclaredField("addrArray");
        Class<?> objCls = f.getType();
        System.out.println("Array ==> " + objCls.getComponentType().getName());

        f = cls.getDeclaredField("addrList");
        Type type = f.getGenericType();
        if (type instanceof ParameterizedType){
            Type raw = ((ParameterizedType)type).getRawType();
            Type actual = ((ParameterizedType) type).getActualTypeArguments()[0];
            System.out.println("actual ==> " + actual.getClass());
        }
    }

    @Test
    public void toJson() {
        // format
        String json = FieldUtil.toJson(FieldDemo.class);
        System.out.println("format ==> " + json);

        List<FieldApiVo> result = FieldUtil.parse(json);
        json = FieldUtil.format(result);
        System.out.println("format2 ==> " + json);
    }

    @Data
    public static class FieldDemo {
        @FieldProperties(title = "姓名")
        private String name;

        @FieldProperties(title = "年龄", min = 18, max = 65)
        private int age;

        @FieldProperties(title = "联系地址List", remark = "可以填多个")
        private ArrayList<Address> addrList;

//        @FieldProperties(title = "列表", remark = "测试没有泛型会报错")
//        private List list;

        @FieldProperties(title = "联系地址Array", remark = "可以填多个")
        private Address[] addrArray;

        @FieldProperties(title = "邮箱")
        private Email email;

        @Data
        public static class Email {
            @FieldProperties(title = "账号")
            private String account;

            @FieldProperties(title = "邮箱域名")
            private String domain;
        }

        @Data
        public static class Address {
            @FieldProperties(title = "省")
            private String prov;
            @FieldProperties(title = "市")
            private String city;
            @FieldProperties(title = "区/县")
            private String area;
        }
    }
}