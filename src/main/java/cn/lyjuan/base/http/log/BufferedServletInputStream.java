package cn.lyjuan.base.http.log;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class BufferedServletInputStream extends ServletInputStream
{
    private ByteArrayInputStream inputStream;

    public BufferedServletInputStream(byte[] buffer)
    {
        this.inputStream = new ByteArrayInputStream(buffer);
    }

    @Override
    public boolean isFinished()
    {
        return true;
    }

    @Override
    public boolean isReady()
    {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener)
    {

    }

    @Override
    public synchronized void mark(int readlimit)
    {
        inputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException
    {
        this.inputStream.reset();
    }

    @Override
    public boolean markSupported()
    {
        return this.inputStream.markSupported();
    }

    @Override
    public int available() throws IOException
    {
        return inputStream.available();
    }

    @Override
    public int read() throws IOException
    {
        return inputStream.read();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException
    {
        return inputStream.read(b, off, len);
    }
}
