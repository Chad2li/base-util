package io.github.chad2li.baseutil.http.filter.log;

import io.github.chad2li.baseutil.util.SpringUtils;
import io.github.chad2li.baseutil.util.StringUtils;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

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
        if (null != this.inputStream) {
            return;
        }
        try {
            InputStream is = super.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte buff[] = new byte[10 * 1024];
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
        if (StringUtils.isNullArray(bytes)) {
            wrapperInputStream();
            try {
                bytes = SpringUtils.reqBodyByInputWithMark(this.getInputStream(), StandardCharsets.UTF_8.name()).getBytes(StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return bytes;
        // spring在获取 getParameters 后无法再次获取输入流数据

//        String body = SpringUtils.reqBody(this);
//        return StringUtils.isNull(body) ? new byte[0] : body.getBytes();
    }

    /**
     * 获取Body内容
     *
     * @param charset 字符编码
     * @return
     */
    public String getContent(String charset) {
        byte[] bytes = getContentAsByteArray();

        try {
            return new String(bytes, charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getContent() {
        return getContent(StandardCharsets.UTF_8.name());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null != this.inputStream ? this.inputStream : super.getInputStream();
//        return this.inputStream;
    }
}