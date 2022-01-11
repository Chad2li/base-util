package cn.lyjuan.base.util.field;

/**
 * {@link FieldProperties}类型标识
 *
 * @author chad
 * @date 2021/12/22 12:00:00
 * @since 1 by chad at 2021/12/22 新增
 */
public enum ItemTypeEnum {
    NUMBER
    //
    , STRING
    //
    , OBJECT
    //
    , LIST
    //
    /**
     * hash结构，Key固定为 String，Value的标签为 {@link ItemTypeEnum#NAME_HASH_VALUE}
     */
    , HASH
    //
    ;
    /**
     * Hash结构值对象的标识
     */
    public static final String NAME_HASH_VALUE = "HASH.VALUE";
    public static final String NAME_LIST_VALUE = "LIST.VALUE";
}
