package cn.lyjuan.base.http.filter.log;

import cn.lyjuan.base.util.ArrayUtils;
import cn.lyjuan.base.util.SpringUtils;
import cn.lyjuan.base.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.Arrays;

/**
 * 对req重复读取
 */
public class BufferedRequestWrapper extends ContentCachingRequestWrapper {

    private BufferedInputStream inputStream;

    public BufferedRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
//        InputStream is = super.getInputStream();
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        byte buff[] = new byte[1024];
//        int read;
//        while ((read = is.read(buff)) > 0) {
//            baos.write(buff, 0, read);
//        }
//        this.inputStream = new BufferedInputStream(baos.toByteArray());
    }

    private void wrapperInputStream() {
        if (null != this.inputStream)
            return;
        try {
            InputStream is = super.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buff[] = new byte[1024];
            int read;
            while ((read = is.read(buff)) > 0) {
                baos.write(buff, 0, read);
            }
            this.inputStream = new BufferedInputStream(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] getContentAsByteArray() {
        byte[] bytes = super.getContentAsByteArray();
        if (null != bytes && bytes.length > 0) {
            return bytes;
        }
        // spring在获取 getParameters 后无法再次获取输入流数据
        wrapperInputStream();
        bytes = SpringUtils.reqBody(this).getBytes();
        return bytes;
//        String body = SpringUtils.reqBody(this);
//        return StringUtils.isNull(body) ? new byte[0] : body.getBytes();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null != this.inputStream ? this.inputStream : super.getInputStream();
//        return this.inputStream;
    }
}