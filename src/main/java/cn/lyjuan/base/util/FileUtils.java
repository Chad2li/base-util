package cn.lyjuan.base.util;

import java.io.*;
import java.util.Date;

public class FileUtils
{


    /**
     * 获取时间路径
     *
     * @param fatherPath
     */
    public static String getTimePath(String fatherPath)
    {
        fatherPath = StringUtils.isNull(fatherPath) ? "" : fatherPath;

        String subPath = DateUtils.format(new Date(), "/yyyy/MM/");

        return fatherPath + subPath;
    }

    /**
     * 获取服务的本地路径
     *
     * @param subPath
     * @return
     */
    public static String getRealPath(String subPath)
    {
        String path = FileUtils.class.getClassLoader().getResource("/").getPath();

        path = path.substring(0, path.length() - 1);

        path = path.substring(0, path.lastIndexOf("/"));

        path = path.substring(0, path.lastIndexOf("/") + 1);

        return path + subPath;
    }


    /**
     * 保存文件
     *
     * @param content
     * @param to
     */
    public static void saveFile(byte[] content, String to)
    {
        saveFile(content, new File(to));
    }

    /**
     * 保存文件
     *
     * @param content
     * @param to
     */
    public static void saveFile(byte[] content, File to)
    {
        try
        {
            if (!to.getParentFile().isDirectory())
                to.getParentFile().mkdirs();

            OutputStream out = new FileOutputStream(to);

            out.write(content);

            out.close();
        } catch (Exception e)
        {
            throw new RuntimeException("saveFile for byte fail", e);
        }
    }

    /**
     * 保存文件
     *
     * @param from
     * @param to
     */
    public static void saveFile(File from, File to)
    {
        try
        {
            InputStream  in  = new FileInputStream(from);
            OutputStream out = new FileOutputStream(to);
            byte[]       b   = new byte[1024];
            int          len = -1;
            while (-1 != (len = in.read(b)))
                out.write(b, 0, len);

            out.flush();
            out.close();
            in.close();
        } catch (IOException e)
        {
            throw new RuntimeException("saveFile for file fail", e);
        }
    }

    /**
     * 拷贝文件
     *
     * @param oriFile
     * @param toFile
     */
    public static void copyFile(File oriFile, File toFile)
    {
        if (null == oriFile || null == toFile)
            return;

        if (!toFile.getParentFile().isDirectory())
            toFile.getParentFile().mkdirs();

        InputStream  in  = null;
        OutputStream out = null;
        try
        {
            in = new FileInputStream(oriFile);
            out = new FileOutputStream(toFile);
            byte[] b   = new byte[1024];
            int    len = -1;
            while (-1 != (len = in.read(b)))
                out.write(b, 0, len);
        } catch (Exception e)
        {
            throw new RuntimeException("上传文件失败", e);
        } finally
        {
            try
            {
                if (null != out)
                {
                    out.flush();
                    out.close();
                }
                if (null != in)
                {
                    in.close();
                }
            } catch (IOException e1)
            {
                throw new RuntimeException("上传文件失败", e1);
            }
        }
    }

    /**
     * 获取唯一的文件名
     *
     * @param path
     * @return
     */
    public static String getUniqueFileName(String path, String fix)
    {
        String fn = System.currentTimeMillis() + "" + RandomUtils.randomInt(6) + fix;

        File f = null;
        for (f = new File(path + "/" + fn); f.isFile();
             f = new File(path + "/" + System.currentTimeMillis() + "" + RandomUtils.randomInt(6) + fix))
            ;

        return f.getName();
    }

    public static void saveFile(File file, String content, String charset)
    {
        try
        {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);

            out.write(content.getBytes(charset));

            out.flush();

            out.close();
        } catch (IOException e)
        {
            throw new RuntimeException("save file error", e);
        }
    }
}
