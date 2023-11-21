package io.github.chad2li.baseutil.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ArrayUtil;
import io.github.chad2li.baseutil.exception.util.ErrUtils;
import io.github.chad2li.baseutil.util.URLUtils;
import lombok.extern.slf4j.Slf4j;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import org.springframework.lang.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.function.Function;

/**
 * zip util
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/5 08:52
 */
@Slf4j
public class ZipUtils {

    /**
     * 压缩文件
     *
     * @param out             压缩包输出流
     * @param password        密码
     * @param inputStreamName 输入流获取文件名方法，当ins有InputStream时必填，放参为InputStream，输出为文件名
     * @param ins             输入，支持 String（路径）、File、InputStream
     * @author chad
     * @since 1 by chad at 2023/9/10
     */
    public static void zip(OutputStream out, @Nullable String password,
                           @Nullable Function<InputStream, String> inputStreamName, Object... ins) {
        Assert.notNull(out);
        Assert.notEmpty(ins);
        Object in = null;
        try (ZipOutputStream zip = new ZipOutputStream(out,
                CharSequenceUtil.isEmpty(password) ? null : password.toCharArray(),
                StandardCharsets.UTF_8)) {
            // 递归压缩文件
            for (Object o : ins) {
                in = o;
                zip(zip, in, password, inputStreamName);
            }
        } catch (Exception e) {
            log.error("zip error, ins:{}, in:{}", Arrays.toString(ins), in, e);
            throw ErrUtils.appThrow(e);
        }
    }

    /**
     * 压缩文件
     *
     * @param zip             压缩包输出流
     * @param in              输入，支持 String（路径）、File、InputStream
     * @param password        密码，null则不使用密码压缩
     * @param inputStreamName 输入流获取文件名方法，当in为InputStream时必填，放参为InputStream，输出为文件名
     * @author chad
     * @since 1 by chad at 2023/9/10
     */
    public static void zip(ZipOutputStream zip, Object in, @Nullable String password,
                           @Nullable Function<InputStream, String> inputStreamName) throws IOException {
        if (in instanceof String) {
            // 文件路径
            zip(zip, (String) in, null, password);
        } else if (in instanceof File) {
            // 文件
            zip(zip, ((File) in).getAbsolutePath(), null, password);
        } else if (in instanceof InputStream) {
            // 输入流
            Assert.notNull(inputStreamName);
            String name = inputStreamName.apply((InputStream) in);
            zipSingle(zip, in, name, password);
        }
    }

    /**
     * 遍历压缩文件
     *
     * @param zip      zip输出流
     * @param path     输入路径
     * @param password 压缩参数
     * @author chad
     * @since 1 by chad at 2023/9/5
     */
    public static void zip(ZipOutputStream zip, String path, @Nullable String relativeName,
                           @Nullable String password)
            throws IOException {
        String fileName = path;
        if (CharSequenceUtil.isNotEmpty(relativeName)) {
            fileName += relativeName;
        }
        File file = new File(fileName);
        if (file.isFile()) {
            zipSingle(zip, file, relativeName, password);
        } else if (file.isDirectory()) {
            // 遍历
            String[] files = file.list();
            if (ArrayUtil.isEmpty(files)) {
                return;
            }
            for (String f : files) {
                zip(zip, path, URLUtils.appendUrlPath(relativeName, null, f), password);
            }
        } else {
            log.warn("zip skip for file not found, file:{}", file.getAbsoluteFile());
        }
    }

    /**
     * 压缩单个文件
     *
     * @param zip
     * @param in
     * @param name
     * @param password
     * @author chad
     * @since 1 by chad at 2023/9/10
     */
    private static void zipSingle(ZipOutputStream zip, Object in,
                                  @Nullable String name, @Nullable String password)
            throws IOException {
        Assert.notNull(in);
        ZipParameters tmpParam = zipParameters(password);

        if (in instanceof String || in instanceof File) {
            File file;
            file = in instanceof String ? new File((String) in) : (File) in;
            if (CharSequenceUtil.isEmpty(name)) {
                name = file.getName();
            }
            tmpParam.setFileNameInZip(name);
            zip.putNextEntry(tmpParam);
            FileUtil.writeToStream(file, zip);
            log.info("zip in string, name:{}, path:{}", name, in);
        } else if (in instanceof InputStream) {
            Assert.notEmpty(name);
            tmpParam.setFileNameInZip(name);
            zip.putNextEntry(tmpParam);
            IoUtil.copy((InputStream) in, zip);
            log.info("zip in inputStream, name:{}", name);
        } else {
            throw new IllegalArgumentException("not supported zip in(String, File, InputStream)," +
                    " in:" + (null != in ? in.getClass().getName() : null));
        }
        zip.closeEntry();
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

    private ZipUtils() {
        // do nothing
    }
}
