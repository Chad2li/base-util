package cn.lyjuan.base.http.aop.service;


import cn.lyjuan.base.util.StringUtils;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.servlet.http.HttpServletRequest;

/**
 * 请求头部信息缓存服务
 */
public interface IHeaderService<T extends IHeaderService.AHeaderParam> {
    /**
     * 缓存
     *
     * @param headerParam
     */
    void cache(T headerParam);

    /**
     * 取出
     */
    T cache();

    @Data
    @NoArgsConstructor
    abstract class AHeaderParam {
        /**
         * 分配的appId，对应有md5签名key
         */
        protected Integer appId;
        /**
         * 接口请求时的unix时间戳，13位毫秒级
         */
        protected Long timestamp;
        /**
         * 接口请求唯一标识，防重
         */
        protected String requestId;
        /**
         * md5签名，用于校验来源
         */
        protected String sign;

        public static <T extends AHeaderParam> T parse(HttpServletRequest req, Class<T> headerCls) {
            T header = null;
            try {
                header = headerCls.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String appIdStr = req.getHeader("appId");
            if (StringUtils.isNumber(appIdStr))
                header.setAppId(Integer.valueOf(appIdStr));
            header.setRequestId(req.getHeader("requestId"));
            String timestampStr = req.getHeader("timestamp");
            if (StringUtils.isNumber(timestampStr, 13))
                header.setTimestamp(Long.valueOf(timestampStr));
            header.setSign(req.getHeader("sign"));

            header.parse(req);

            return header;
        }

        /**
         * 解析自己平台需要的参数
         *
         * @param req
         */
        public abstract void parse(HttpServletRequest req);

        /**
         * 参数检查
         *
         * @return 返回参数错误信息，null或空字符串表示没有参数错误
         */
        public String check() {
            StringBuilder sb = new StringBuilder();
            if (null == appId || 0 >= appId)
                sb.append("appId").append(",");
            if (StringUtils.isNull(requestId))
                sb.append("requestId").append(",");
            if (null == timestamp)
                sb.append("timestamp").append(",");
            if (StringUtils.isNull(sign))
                sb.append("sign").append(",");

            // 自已平台的参数校验
            check(sb);

            return sb.toString();
        }

        /**
         * 参数检查，如果有参数错误，使用errMsg拼接，多个使用“,”分隔<br/>
         * 在{@code HeaderFilter.isDebug}为true时，接口会返回 errMsg 信息
         *
         * @param errMsg
         * @return
         */
        public abstract String check(StringBuilder errMsg);
    }
}
