package io.github.chad2li.baseutil.mybatis.type;

import io.github.chad2li.baseutil.mybatis.typehander.CommaJoinListTypeHandler;
import org.apache.ibatis.type.Alias;

/**
 * List值用,拼接
 * @see CommaJoinListTypeHandler
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/31 08:44
 */
@Alias("CommaJoinList")
public class CommaJoinList {
    public CommaJoinList() {
        // do nothing
    }
}
