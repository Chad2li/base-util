package io.github.chad2li.baseutil.consts;

/**
 * 常量
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/22 08:15
 */
public class DefaultConstant {

    /**
     * 常量字符
     *
     * @author chad
     * @since 1 by chad at 2023/8/22
     */
    public static final class Norm {
        /**
         * /
         */
        public static final String FILE_SPLIT = "/";

        public static final String COMMA = ",";

        public static final String SEMICOLON = ";";

        public static final String COLON = ":";

        public static final String POUND = "#";

        public static final String DOT = ".";

        public static final String DIVISION = "-";

        public static final String UNDERLINE = "_";

        public static final String EMPTY = "";

        private Norm() {
            // do nothing
        }
    }

    /**
     * http相关
     *
     * @author chad
     * @since 1 by chad at 2023/8/22
     */
    public static final class Http {
        public static final String HTTP = "http";
        public static final String HTTP_PROTOCOL = "http://";
        public static final String HTTPS = "https";
        public static final String HTTPS_PROTOCOL = "http://";

        private Http() {
            // do nothing
        }
    }

    /**
     * database相关
     *
     * @author chad
     * @since 1 by chad at 2023/8/24
     */
    public static final class Db {
        public static final String ID = "id";
    }

    private DefaultConstant() {
        // do nothing
    }
}
