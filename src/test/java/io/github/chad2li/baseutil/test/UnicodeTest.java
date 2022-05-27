package io.github.chad2li.baseutil.test;

import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class UnicodeTest {
    @Test
    public void code() throws UnsupportedEncodingException {
        String str = "\\xac\\xed\\x00\\x05t\\x00#bms:oauth:refresh:token:15385431226";
        String result = URLDecoder.decode(str.replaceAll("\\\\x", "%"), StandardCharsets.UTF_8.name());
//        String[] arr = str.split("\\\\x");
//        StringJoiner sj = new StringJoiner("");
//        for (String s : arr) {
//            if (StringUtils.isNull(s)) continue;
//            byte[] bs = HexUtils.fromHex(s.substring(0, 2));
//            char c = (char) bs[0];
//            sj.add(String.valueOf(c));
//            if (s.length() > 2)
//                sj.add(s.substring(2));
//        }
//
        System.out.println("result ==> " + result);
    }
}
