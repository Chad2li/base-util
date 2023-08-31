package io.github.chad2li.baseutil.util;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import io.github.chad2li.baseutil.consts.DefaultConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

/**
 * file
 *
 * @author chad
 * @since 1 by chad at 2023/8/22
 */
@Slf4j
public class FileUtils {

    /**
     * 文件名唯一锁
     */
    private static final Object LOCK_CREATE_FILE = new Object();

    /**
     * 日期路径
     *
     * @param fatherPath 上级路径
     * @return fatherPath/yyyy/MM/dd
     * @author chad
     * @since 1 by chad at 2023/8/22
     */
    public static String datePath(String fatherPath) {
        Assert.notNull(fatherPath);
        String datePath = DateUtils.format(LocalDateTime.now(), "yyyy/MM/dd");
        if (fatherPath.endsWith(DefaultConstant.Norm.FILE_SPLIT)) {
            return fatherPath + datePath;
        }

        return fatherPath + DefaultConstant.Norm.FILE_SPLIT + datePath;
    }

    /**
     * 生成唯一文件名，并创建文件来占用文件名
     *
     * @param path   路径
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀
     * @return path /
     * @author chad
     * @since 1 by chad at 2023/8/22
     */
    public static File timeFileNameUnique(String path, @Nullable String prefix,
                                            @Nullable String suffix) {
        Assert.notNull(path);
        path = path.trim();
        // 1. 创建目录
        File file = new File(path, timeFileName(prefix, suffix));
        if (!file.getParentFile().isDirectory()) {
            // 创建目录
            file.getParentFile().mkdirs();
        }

        // 2. 生成唯一文件名
        synchronized (LOCK_CREATE_FILE) {
            try {
                while (!file.createNewFile()) {
                    // 文件已经存在，重新生成文件名
                    file = new File(path, timeFileName(prefix, suffix));
                }
            } catch (Exception e) {
                log.error("create file error, file:{}", file.getAbsoluteFile(), e);
                throw new IllegalStateException(e);
            }
        }
        return file;
    }

    /**
     * 时间文件名
     *
     * @param prefix 文件名前缀
     * @param suffix 文件名后缀，如：txt效果同.txt
     * @return prefix + yyyyMMddHHmmss + {3位随机数} + [.]suffix
     * @author chad
     * @since 1 by chad at 2023/8/22
     */
    public static String timeFileName(@Nullable String prefix, @Nullable String suffix) {
        // 1. 拼接前缀和-
        String fileName = DefaultConstant.Norm.EMPTY;
        if (CharSequenceUtil.isNotEmpty(prefix)) {
            fileName = prefix.trim();
            if (!fileName.endsWith(DefaultConstant.Norm.DIVISION)) {
                fileName += DefaultConstant.Norm.DIVISION;
            }
        }
        // 2. 拼接时间yyyyMMddHHmmss + 3位随机数
        fileName += DateUtils.format(LocalDateTime.now(), DatePattern.PURE_DATETIME_PATTERN);
        fileName += RandomUtil.randomNumbers(3);
        // 3. 拼接文件后缀
        if (CharSequenceUtil.isEmpty(suffix)) {
            // 3.1 没有后缀
            return fileName;
        }
        suffix = suffix.trim();
        if (!suffix.startsWith(DefaultConstant.Norm.DOT)) {
            return fileName + DefaultConstant.Norm.DOT + suffix;
        }
        return fileName + suffix;
    }

    /**
     * 获取唯一的文件名
     *
     * @param path
     * @return
     */
    public static String getUniqueFileName(String path, String fix) {
        String fn = System.currentTimeMillis() + "" + RandomUtils.randomInt(6) + fix;

        File f = null;
        for (f = new File(path + "/" + fn); f.isFile();
             f = new File(path + "/" + System.currentTimeMillis() + "" + RandomUtils.randomInt(6) + fix))
            ;

        return f.getName();
    }

    public static void saveFile(File file, String content, String charset) {
        try {
            file.createNewFile();
            OutputStream out = new FileOutputStream(file);

            out.write(content.getBytes(charset));

            out.flush();

            out.close();
        } catch (IOException e) {
            throw new RuntimeException("save file error", e);
        }
    }
}
