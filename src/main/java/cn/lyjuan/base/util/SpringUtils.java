package cn.lyjuan.base.util;

import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by ly on 2015/3/17.
 */
public class SpringUtils {
    /**
     * 根据时间获取文件名
     *
     * @param subPath 路径前缀
     * @param suf     文件后缀
     * @return
     */
    public static String getDateRealPath(String subPath, String suf) {
        LocalDate now = LocalDate.now();

        String path = DateUtils.format(now, "yyyy/MM");

        path = subPath + "/" + path;

        String realPath = getProjectRealPath();

        // 获取文件名
        String fileName = path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + suf;
        File f = new File(realPath + "/" + fileName);

        while (f.isFile()) {
            fileName = path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + suf;
            f = new File(realPath + "/" + fileName);
        }

        return fileName.replaceAll("//", "/");
    }

    /**
     * 获取项目的绝对路径
     *
     * @return
     */
    public static String getProjectRealPath() {
        String path = SpringUtils.class.getClassLoader().getResource("").getFile();

        path = path.replace("WEB-INF/classes/", "");

        return path;
    }

    /**
     * 保存文件
     *
     * @param file
     * @param disPath 区分路径，完整路径为 项目路径/区分路径/yyyy/MM/文件名.后缀
     * @return 返回保存文件的相对路径
     * @throws java.io.IOException
     */
    public static String saveUploadFile(CommonsMultipartFile file, String disPath) {
        if (null == file || file.isEmpty()) return null;

        String realPath = getProjectRealPath();

        String oriFileName = file.getOriginalFilename();
        String suf = "";
        suf = oriFileName.indexOf(".") > -1 ?
                oriFileName.substring(oriFileName.lastIndexOf(".")) : suf;// 文件后缀名

        // 生成文件名称
        String name = disPath + "/" + DateUtils.format(LocalDateTime.now(), "yyyy/MM/yyyyMMddHHmmssSSS") + suf;
        File saveFile = new File(realPath, name);
        while (saveFile.isFile())
            saveFile = new File(realPath, name = disPath + "/" + DateUtils.format(LocalDateTime.now(), "yyyy/MM/yyyyMMddHHmmssSSS") + suf);

        if (!saveFile.getParentFile().isDirectory())
            saveFile.getParentFile().mkdirs();

        // 保存文件
        try {
            file.getFileItem().write(saveFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 返回保存的文件名
        return name;
    }

    /**
     * 获取 HttpServletRequest
     *
     * @return
     */
    public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    }

    /**
     * 获取 HttpServletResponse
     *
     * @return
     */
    public static HttpServletResponse getResponse() {
        HttpServletResponse resp = ((org.springframework.web.context.request.ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getResponse();
        return resp;
    }

    /**
     * 获取 ServletServerHttpRequest
     *
     * @return
     */
    public static ServletServerHttpRequest getServletRequest() {
        HttpServletRequest req = getRequest();

        return new ServletServerHttpRequest(req);
    }

//    /**
//     * 获取 Spring Security 认证用户信息
//     *
//     * @return
//     */
//    public static UserDetails getUserPrincipal()
//    {
//        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getPrincipal();
//
//        return userDetails;
//    }

    /**
     * 获取Request属性
     *
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T reqAttr(String name) {
        Object val = SpringUtils.getRequest().getAttribute(name);
        if (null == val) return null;
        return (T) val;
    }

    /**
     * 设置请求属性值
     *
     * @param name
     * @param val
     * @param <T>
     */
    public static <T> void reqAttr(String name, T val) {
        SpringUtils.getRequest().setAttribute(name, val);
    }

    /**
     * 获取参数，使用 {@code getParameter()} 方法
     *
     * @param req
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static Map<String, String> getParam(HttpServletRequest req) {
        Map<String, String> map = new HashMap<String, String>();

        Enumeration<String> es = req.getParameterNames();

        while (es.hasMoreElements()) {
            String name = es.nextElement();
            String value = req.getParameter(name);

            map.put(name, value);
        }

        return map;
    }

    /**
     * 读取request body内容
     *
     * @param req
     * @return
     */
    public static String reqBody(HttpServletRequest req) {
        InputStream in = null;
        String str = null;
        try {
            in = req.getInputStream();
            if (!in.markSupported())
                throw new RuntimeException("request unsupported mark");
            in.reset();

            // in.mark(req.getContentLength());

            str = HttpUtils.postStr(in);

            in.reset();
        } catch (IOException e) {
            try {
                if (null != in) in.reset();
            } catch (IOException e2) {
                throw new RuntimeException(e2);
            }

            throw new RuntimeException(e);
        }

        return str;
    }

    /**
     * 将非 HTTP 开头的地址拼接上前缀URL
     *
     * @param subUrl 子URL
     * @param preUrl 前缀URL
     * @return
     */
    public static String appendUrl(String subUrl, String preUrl) {
        if (StringUtils.isNull(subUrl)) return "";

        if (StringUtils.isNull(preUrl)) return subUrl;

        if (subUrl.startsWith("http://") || subUrl.startsWith("HTTP://"))
            return subUrl;

        return preUrl + subUrl;
    }

    /**
     * 解析图片后缀
     *
     * @param content
     * @return
     */
    public static String parseImageType(byte[] content) {
        ByteArrayInputStream bais = new ByteArrayInputStream(content);
        ImageInputStream iis = null;

        String type = "";
        try {
            iis = ImageIO.createImageInputStream(bais);

            Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);

            ImageReader reader = iter.next();

            type = reader.getFormatName();

            // close stream
            iis.close();
        } catch (IOException e) {
            throw new RuntimeException("parseImageType faile", e);
        }

        return type;
    }

    /**
     * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
     * <p>
     * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
     * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
     * <p>
     * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130,
     * 192.168.1.100
     * <p>
     * 用户真实IP为： 192.168.1.110
     */
    public static String getRemoteIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        String unknown = "unknown";
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
