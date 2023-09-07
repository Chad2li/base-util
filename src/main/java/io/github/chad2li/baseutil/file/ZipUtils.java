package io.github.chad2li.baseutil.file;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import io.github.chad2li.baseutil.exception.util.ErrUtils;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * zip util
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/5 08:52
 */
@Slf4j
public class ZipUtils {
    public static void zip(OutputStream out, String path, @Nullable String password) {
        Assert.notNull(out);
        Assert.notEmpty(path);
        Assert.notEmpty(password);
        try (ZipOutputStream zip = new ZipOutputStream(out,
                CharSequenceUtil.isEmpty(password) ? null : password.toCharArray(),
                StandardCharsets.UTF_8)) {
            ZipParameters param = zipParameters(password);
            // 递归压缩文件
            zip(zip, path, null, param);
        } catch (Exception e) {
            log.error("zip error, path:{}", path, e);
            throw ErrUtils.appThrow(e);
        }
        // 遍历文件
    }

    /**
     * 构建加密参数
     *
     * @param password password
     * @return zip parameter
     * @author chad
     * @since 1 by chad at 2023/9/5
     */
    public static ZipParameters zipParameters(@Nullable String password) {
        ZipParameters param = new ZipParameters();
        // 压缩方式，标准ZIP
        param.setCompressionMethod(CompressionMethod.DEFLATE);
        // 压缩等级，中等
        param.setCompressionLevel(CompressionLevel.MAXIMUM);
        if (CharSequenceUtil.isNotEmpty(password)) {
            // 加密方式，标准ZIP加密
            param.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            // 设置加密文件
            param.setEncryptFiles(true);
        }
        return param;
    }

    /**
     * 遍历压缩文件
     *
     * @param zip
     * @param path
     * @param param
     * @author chad
     * @since 1 by chad at 2023/9/5
     */
    public static void zip(ZipOutputStream zip, String path, @Nullable String relativeName,
                           ZipParameters param)
            throws IOException {
        String fileName = path;
        if (CharSequenceUtil.isNotEmpty(relativeName)) {
            fileName += relativeName;
        }
        File file = new File(fileName);
        if (file.isFile()) {
            ZipParameters tmpParam = BeanUtil.copyProperties(param, ZipParameters.class);
            tmpParam.setFileNameInZip(relativeName);
            zip.putNextEntry(tmpParam);
            FileUtil.writeToStream(file, zip);
            zip.closeEntry();
            log.info("zip in file:{}", file.getAbsoluteFile());
        } else if (file.isDirectory()) {
            // 遍历
            File[] files = file.listFiles();
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (File f : files) {
                relativeName = f.getAbsolutePath().substring(path.length() - 1);
                zip(zip, path, relativeName, param);
            }
        } else {
            log.warn("zip skip for file not found, file:{}", file.getAbsoluteFile());
        }
    }

    private ZipUtils() {
        // do nothing
    }
}
