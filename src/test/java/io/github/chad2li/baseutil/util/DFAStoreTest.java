package io.github.chad2li.baseutil.util;

import lombok.AllArgsConstructor;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.*;

public class DFAStoreTest {
    @Test
    public void find() throws Exception {
        loadTrie();
        DFAStore.DFAResult result = DFAStore.findOne("多多益善", true);
        System.out.println(result);

    }

    @Test
    public void test() throws Exception {
        Set<String> set = loadDFA();
        List<String> contents = loadTxt();
        long loadDFAStart = System.currentTimeMillis();
        DFAStore.initTrie(set);
        long loadDFAend = System.currentTimeMillis();
        System.out.println("load dfa duration: " + (loadDFAend - loadDFAStart));

        List<FindOpr> oprs = new ArrayList<>();
        long findStart = System.currentTimeMillis();
        FindOpr opr = null;
        for (String s : contents) {
            List<DFAStore.DFAResult> results = DFAStore.findAll(s, true, true);
            long findEnd = System.currentTimeMillis();
            opr = new FindOpr(findEnd - findStart, results);
            oprs.add(opr);
            findStart = findEnd;
        }

        long findAll = System.currentTimeMillis();
        System.out.println("find dfa duration: " + (findAll - loadDFAend));

        double min = Integer.MAX_VALUE, max = 0, all = 0;
        for (FindOpr f : oprs) {
            all += f.duration;
            min = Math.min(min, f.duration);
            max = Math.max(max, f.duration);
        }
        System.out.println("max: " + max + ", min: " + min + ", avg: " + (all / oprs.size()));
        // 过滤结果数量的时间统计
        Map<Integer, List<Long>> countTime = new HashMap<>();
        for (FindOpr f : oprs) {
            List<Long> list = countTime.get(f.results.size());
            if (null == list) {
                list = new ArrayList<>();
                countTime.put(f.results.size(), list);
            }
            list.add(f.duration);
        }
        for (Map.Entry<Integer, List<Long>> e : countTime.entrySet()) {
            max = 0;
            min = Integer.MAX_VALUE;
            all = 0;
            for (Long l : e.getValue()) {
                all += l;
                max = Math.max(max, l);
                min = Math.min(min, l);
            }
            System.out.println("count: " + e.getKey() + ", " + ", max: " + max + ", min: " + min + ", avg: " + (all / e.getValue().size()));
        }
    }

    @AllArgsConstructor
    public static class FindOpr {
        private long duration;
        private List<DFAStore.DFAResult> results;
    }

    private List<String> loadTxt() throws Exception {
        List<String> contents = new ArrayList<>();
        File file = new File("./doc/content.txt");
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        while (null != (line = in.readLine()))
            contents.add(line);
        return contents;
    }


    private Set<String> loadDFA() throws Exception {
        String dfaFile = "./dfa/keys.txt";
        File file = new File(dfaFile);
        Set<String> set = new HashSet<>();

        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line = null;
        while (null != (line = reader.readLine()))
            set.add(line);
        return set;
    }

    @Test
    public void findAll() {
        initTrie();

        List<DFAStore.DFAResult> results = DFAStore.findAll("多多益善", true, false);
        System.out.println(StringUtils.toStr(results));

        DFAStore.DFAResult result = DFAStore.findOne("dbc", true);
        System.out.println(StringUtils.toStr(result));

        result = DFAStore.findOne("a.<b", true);
        System.out.println(StringUtils.toStr(result));
    }

    @Test
    public void loadTrie() throws Exception {
        Set<String> sensitives = loadDFA();
        DFAStore.initTrie(sensitives);

//        for (Map.Entry<Character, DFAStore.DFAItem> e : DFAStore.ROOT.subs.entrySet()) {
//            StringBuilder treePre = new StringBuilder();
//            say(treePre, e.getValue(), 0);
//        }
    }

    @Test
    public void initTrie() {
        Set<String> sensitives = new HashSet<>();
        sensitives.add("ab");
        sensitives.add("abc");
        sensitives.add("abcd");
        sensitives.add("bc");
        sensitives.add("cd");

        DFAStore.initTrie(sensitives);

        for (Map.Entry<Character, DFAStore.DFAItem> e : DFAStore.ROOT.subs.entrySet()) {
            StringBuilder treePre = new StringBuilder();
            say(treePre, e.getValue(), 0);
        }
    }

    private void say(StringBuilder treePre, DFAStore.DFAItem item, int deep) {
//        if (deep > 0)
//            treePre.append("");

        System.out.println(treePre.toString() + item.word + "(" + (item.isEnd ? 1 : 0) + ")");

        if (!item.hasSub())
            return;

        StringBuilder tmpPre = new StringBuilder();
        for (int i = 0; i < deep; i++) {
            tmpPre.append("....");
            if (deep > 0)
                tmpPre.append(".");
        }
        tmpPre.append("|____");
        for (Map.Entry<Character, DFAStore.DFAItem> e : item.subs.entrySet()) {
            say(tmpPre, e.getValue(), ++deep);
        }
    }

    /**
     * 将文件内容为Base64转为文字
     *
     * @throws Exception
     */
    @Test
    public void covertBase64() throws Exception {
        File file = new File("./doc/pub_sms_banned_words.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./doc/key2.txt"), StandardCharsets.UTF_8));
        String line = null;
        while (null != (line = reader.readLine())) {
            byte[] b = Base64.getDecoder().decode(line);
            writer.write(new String(b, "UTF-8"));
        }
        writer.close();
        reader.close();
    }

    @Test
    public void covertGB2312() throws Exception {
        File file = new File("./doc/贪腐词库.txt");
        InputStream in = new FileInputStream(file);
        File outFile = new File("./doc/content.txt");
        ByteArrayOutputStream out1 = new ByteArrayOutputStream();
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)));

        byte[] b = new byte[1024];
        int len = -1;

        while (-1 != (len = in.read(b)))
            out1.write(b, 0, len);
        in.close();

        out.write(new String(out1.toByteArray(), "GB2312"));
        out1.close();
        out.close();
    }

    /**
     * 将两个文件内的敏感词去重合并
     *
     * @throws Exception
     */
    @Test
    public void unionDFA() throws Exception {
        Set<String> sensitives = new HashSet<>();
        int index = 9;
        readIn("./doc/keys" + index + ".txt", sensitives);
        readIn("./doc/content.txt", sensitives);
//        readIn("./doc/english_dictionary.txt", sensitives);

        writeOut("./doc/keys" + (index + 1) + ".txt", sensitives);
    }

    private void writeOut(String path, Set<String> sensitives) throws Exception {
        BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
        for (String s : sensitives) {
            if (StringUtils.isNull(s)) continue;
            w.write(s);
            w.newLine();
        }
        w.close();
    }

    private void readIn(String path, Set<String> sensitives) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
        String line = null;
        while (null != (line = reader.readLine()))
            sensitives.add(line);
        reader.close();
    }

    @Test
    public void analysisDFA() throws Exception {
        Set<String> dfa = loadDFA();
        Map<String, List<Integer>> res = new HashMap<>();
        for (String s : dfa) {
            String c = s.substring(0, s.length() >= 3 ? 3 : s.length());
            List<Integer> list = res.get(c);
            if (null == list) {
                list = new ArrayList<>(1);
                res.put(c, list);
            }
            list.add(s.length());
        }
        for (Map.Entry<String, List<Integer>> e : res.entrySet()) {
            System.out.println(e.getKey() + ": " + e.getValue());
        }
    }

    /**
     * 按行读取 敏感词文本
     *
     * @param filePath filePath
     * @return Set<String>
     */
    public static Set<String> load(String filePath) {
        File f = new File(filePath);
        System.out.println(f.getAbsolutePath());
        if (!f.exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!f.canRead()) {
            throw new RuntimeException("无法读取文件");
        }
        if (f.isDirectory()) {
            throw new RuntimeException("这是文件夹");
        }
        Set<String> set = new HashSet<>(1000000);
        try (
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr)
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                set.add(line.trim());
            }
        } catch (IOException e) {
            //
        }
        return set;
    }

    /**
     * 按 | 间隔读取敏感词
     *
     * @param filePath filePath
     * @return Set<String>
     */
    public static Set<String> load2(String filePath) {
        File f = new File(filePath);
        System.out.println(f.getAbsolutePath());
        if (!f.exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!f.canRead()) {
            throw new RuntimeException("无法读取文件");
        }
        if (f.isDirectory()) {
            throw new RuntimeException("这是文件夹");
        }
        Set<String> set = new HashSet<>(1000000);
        try (
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr)
        ) {
            int c = 0;
            while ((c = br.read()) != -1) {
                char ct = (char) c;
                StringBuilder sb = new StringBuilder();
                sb.append(ct);
                while (ct != '|' && (c = br.read()) != -1) {
                    ct = (char) c;
                    if (ct != '|') {
                        sb.append(ct);
                    }
                }
                set.add(sb.toString());
            }
        } catch (IOException e) {
            //
        }
        return set;
    }

    /**
     * 读取全部
     *
     * @param filePath filePath
     * @return String
     */
    public static String load3(String filePath) {
        File f = new File(filePath);
        System.out.println(f.getAbsolutePath());
        if (!f.exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!f.canRead()) {
            throw new RuntimeException("无法读取文件");
        }
        if (f.isDirectory()) {
            throw new RuntimeException("这是文件夹");
        }
        StringBuilder sb = new StringBuilder();
        try (
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr)
        ) {
            int c = 0;
            while ((c = br.read()) != -1) {
                char ct = (char) c;
                sb.append(ct);
            }
        } catch (IOException e) {
            //
        }
        return sb.toString();
    }
}