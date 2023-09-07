package io.github.chad2li.baseutil.file;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
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

    private static final String SAVE = PATH + "/" + DateUtil.format(LocalDateTime.now(),
            DatePattern.PURE_DATETIME_MS_PATTERN) + ".zip";

    @Test
    public void zip() throws Exception {
        File file = new File(SAVE);
        try {
            file.createNewFile();
        } catch (Exception e) {
            log.error("create zip file error", e);
        }
        try (FileOutputStream out = new FileOutputStream(file);) {
            ZipUtils.zip(out, PATH + "/zip/", "123456");
            log.info("zip save, file:{}", SAVE);
        } catch (Exception e) {
            log.error("zip error", e);
        }
    }
}