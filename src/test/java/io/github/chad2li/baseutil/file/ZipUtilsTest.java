package io.github.chad2li.baseutil.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ZipUtilsTest
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/9/5 13:33
 */
@Slf4j
public class ZipUtilsTest {

    private static final String PATH =
            Objects.requireNonNull(ZipUtilsTest.class.getClassLoader().getResource("")).getPath();

    @Test
    public void zip() throws Exception {
        File file = null;
        // 1. file
        file = new File(PATH + DateUtil.format(LocalDateTime.now(),
                DatePattern.PURE_DATETIME_MS_PATTERN) + "-file.zip");
        try (FileOutputStream out = new FileOutputStream(file);) {
            ZipUtils.zip(out, "123456", null, PATH + "/zip/");
            log.info("zip save, file:{}", file.getAbsoluteFile());
        } catch (Exception e) {
            log.error("zip error", e);
        }

        // 2. inputStream
        file = new File(PATH + DateUtil.format(LocalDateTime.now(),
                DatePattern.PURE_DATETIME_MS_PATTERN) + "-inputStream.zip");
        InputStream in = Files.newInputStream(Paths.get(PATH + "/zip/目录/中文.txt"));
        try (FileOutputStream out = new FileOutputStream(file);) {
            ZipUtils.zip(out, "123456", it -> "目录/中文.txt", in);
            log.info("zip save, file:{}", file.getAbsoluteFile());
        } catch (Exception e) {
            log.error("zip error", e);
        }

        // 3. String, File, InputStream
        String path = PATH + "/zip/1.txt";
        file = new File(PATH + DateUtil.format(LocalDateTime.now(),
                DatePattern.PURE_DATETIME_MS_PATTERN) + "-all.zip");
        File inFile = new File(PATH + "/zip/2.txt");
        in = Files.newInputStream(Paths.get(PATH + "/zip/目录/中文.txt"));
        try (FileOutputStream out = new FileOutputStream(file);) {
            ZipUtils.zip(out, "123456", it -> "目录/中文.txt", path, inFile, in);
            log.info("zip save, file:{}", file.getAbsoluteFile());
        } catch (Exception e) {
            log.error("zip error", e);
        }
    }
}