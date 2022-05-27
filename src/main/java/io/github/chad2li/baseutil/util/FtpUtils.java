package io.github.chad2li.baseutil.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by chad on 2016/5/18.
 */
public class FtpUtils
{
    /**
     * 下载FTP文件
     * @param ftpFile       FTP文件名
     * @param localFile     保存本地文件名
     * @return              true 表示下载文件成功， false 表示下载文件失败
     */
    public static boolean downFile(String host, String user, String pwd, String ftpFile, String localFile)
    {
        FTPClient ftp = new FTPClient();
        File outFile = null;
        FileOutputStream out = null;
        try
        {
            outFile = new File(localFile);
            if (!outFile.getParentFile().isDirectory())
                outFile.getParentFile().mkdirs();

            ftp.connect(host);
            boolean isLogin = ftp.login(user, pwd);
            if (!isLogin)
                    throw new RuntimeException("ftp connection error, user or password error");
            ftp.setDefaultTimeout(10000);
            ftp.setConnectTimeout(10000);// 十秒连接超时
            ftp.setDataTimeout(5 * 60 * 1000);

            // Get file info.
            FTPFile[] fileInfoArray = ftp.listFiles(ftpFile);
            if (null == fileInfoArray || fileInfoArray.length < 1)
                throw new FTPFileNotFoundException("File " + ftpFile + " was not found on FTP server.");

            ftp.setBufferSize(1024);
            out = new FileOutputStream(outFile);
            //设置文件类型（二进制）
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.retrieveFile(ftpFile, out);
            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
            return false;
        } finally
        {
            if (null != out)
            {
                try {  out.flush(); out.close(); } catch (IOException e)
                { e.printStackTrace(); return false;}

                try { ftp.disconnect(); } catch (IOException e)
                { e.printStackTrace(); return false;}
            }
        }
    }

    public static class FTPFileNotFoundException extends RuntimeException
    {
        public FTPFileNotFoundException()
        {
        }

        public FTPFileNotFoundException(String message)
        {
            super(message);
        }
    }
}
