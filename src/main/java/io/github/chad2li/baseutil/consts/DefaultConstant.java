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

        public static final String AMPERSAND = "&";

        public static final String QUESTION_MARK = "?";
        public static final String EQUALS = "=";

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
        public static final String HTTPS_PROTOCOL = "https://";
        public static final String ROOT_PROTOCOL = "//";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATON_JSON = "application/json";
        public static final String GET = "GET";
        public static final String POST = "POST";
        public static final String PUT = "PUT";
        public static final String DELETE = "DELETE";
        public static final String OPTIONS = "OPTIONS";
        public static final String HEAD = "HEAD";
        public static final String PATCH = "PATCH";
        public static final String TRACE = "TRACE";

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

    /**
     * 时间相关常量
     *
     * @author chad
     * @since 1 by chad at 2023/10/11
     */
    public static final class Time {
        /**
         * 一个小时秒数
         */
        public static final int HOUR_SECONDS = 3600;
        /**
         * 一天秒数
         */
        public static final int DAY_SECONDS = 86400;
    }

    private DefaultConstant() {
        // do nothing
    }
}
