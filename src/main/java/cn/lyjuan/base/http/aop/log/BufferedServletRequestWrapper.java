package cn.lyjuan.base.http.aop.log;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 对req重复读取
 */
public class BufferedServletRequestWrapper extends HttpServletRequestWrapper
{
    private BufferedServletInputStream inputStream;

    public BufferedServletRequestWrapper(HttpServletRequest request) throws IOException
    {
        super(request);
        InputStream is = request.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte buff[] = new byte[1024];
        int read;
        while ((read = is.read(buff)) > 0)
        {
            baos.write(buff, 0, read);
        }
        this.inputStream = new BufferedServletInputStream(baos.toByteArray());
    }

    @Override
    public ServletInputStream getInputStream() throws IOException
    {
        return this.inputStream;
    }
}