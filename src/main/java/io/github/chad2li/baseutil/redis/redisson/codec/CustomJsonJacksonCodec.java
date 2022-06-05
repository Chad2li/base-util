package io.github.chad2li.baseutil.redis.redisson.codec;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import org.redisson.codec.JsonJacksonCodec;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 解决原生JsonJacksonCodec无法正确解析/反解析 LocalDateTime
 *
 * @author chad
 * @since 1 create by chad
 * @since 2 at 2022/06/05 by chad: 增加 DefaultBaseTypeLimitingValidator
 */
public class CustomJsonJacksonCodec extends JsonJacksonCodec {

    public CustomJsonJacksonCodec(ObjectMapper mapObjectMapper) {
        super(mapObjectMapper);
    }

    @Override
    protected void initTypeInclusion(ObjectMapper mapObjectMapper) {

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build();

        TypeResolverBuilder<?> mapType = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL, ptv) {
            @Override
            public boolean useForType(JavaType t) {
                Class cls = t.getRawClass();
                switch (_appliesFor) {
                    case NON_CONCRETE_AND_ARRAYS:
                        while (t.isArrayType()) {
                            t = t.getContentType();
                        }
                        // fall through
                    case OBJECT_AND_NON_CONCRETE:
                        return (t.getRawClass() == Object.class) || !t.isConcrete();
                    case NON_FINAL:
                        while (t.isArrayType()) {
                            t = t.getContentType();
                        }
                        // to fix problem with wrong long to int conversion
                        if (cls == Long.class) {
                            return true;
                        }
                        if (cls == LocalDateTime.class ||
                                cls == LocalDate.class ||
                                cls == LocalTime.class) {
                            return true;
                        }
                        if (cls == XMLGregorianCalendar.class) {
                            return false;
                        }
                        return !t.isFinal(); // includes Object.class
                    default:
                        // case JAVA_LANG_OBJECT:
                        return cls == Object.class;
                }
            }
        };

        mapType.init(JsonTypeInfo.Id.CLASS, null);
        mapType.inclusion(JsonTypeInfo.As.PROPERTY);
        mapObjectMapper.setDefaultTyping(mapType);
    }
}
