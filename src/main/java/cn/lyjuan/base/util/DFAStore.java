package cn.lyjuan.base.util;


import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 过滤，常用于敏感词过滤
 */
@Slf4j
public class DFAStore {
    /**
     * 保存敏感词树
     */
    public static final DFAItem ROOT = new DFAItem('.');
    /**
     * 保存需要跳过的单字
     */
    private static final Set<Character> SKIP_WORD = new HashSet<>();

    static {
        char[] arr = "~`!@#$%^&*()_+-=\\|'\";:/?.>,<！……：；「」“”〈〉《》？[]{}".toCharArray();
        for (Character c : arr) {
            SKIP_WORD.add(c);
        }
    }

    /**
     * 是否包含
     */
    public static DFAResult findOne(String source, boolean isMax) {
        List<DFAResult> results = findAll(source, isMax, true);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 对源字符串逐字查找出所有敏感词
     *
     * @param source
     */
    public static List<DFAResult> findAll(String source, boolean isMax, boolean isOne) {
        if (StringUtils.isNull(source))
            return Collections.emptyList();

        List<DFAResult> results = new ArrayList<>();
        char[] arr = source.toCharArray();
        int len = arr.length;
        DFAItem startItem = null;
        DFAItem tmpItem = null;
        for (int i = 0; i < len; i++) {
            startItem = ROOT.getSub(arr[i]);
            if (null == startItem) continue;
            tmpItem = startItem;
            int findLen = 1, tmpFindLen = 1;
            boolean findEnd = false;
            if (tmpItem.findOne(startItem.word)) {
                if (log.isDebugEnabled())
                    log.debug("find1: {}", arr[i]);
                findEnd = true;
                if (!isMax) continue;
            }
            if (tmpItem.hasSub()) {
                Character tmpWord = null;
                for (int j = i + 1; j < len; j++) {
                    tmpWord = arr[j];
                    if (isSkip(tmpWord)) {// 跳过该字
                        tmpFindLen++;
                        continue;
                    }
                    tmpItem = tmpItem.subs.get(tmpWord);
                    if (null == tmpItem) break;
                    tmpFindLen++;
                    if (tmpItem.findOne(startItem.word)) {
                        if (log.isDebugEnabled())
                            log.debug("find2: {}", Arrays.toString(Arrays.copyOfRange(arr, i, j + 1)));
                        findEnd = true;
                        findLen = tmpFindLen;
                    }
                    if (!isMax)
                        break;
                    if (!tmpItem.hasSub())
                        break;
                }
            }

            if (findEnd) {// 有找到isEnd标识元素
                DFAResult result = new DFAResult(i, findLen);
                results.add(result);
            }

            if (isOne && !results.isEmpty())
                return results;
        }

        return results;
    }

    /**
     * 是否跳过单字
     *
     * @param word 是否跳过该字
     * @return
     */
    private static boolean isSkip(Character word) {
        return SKIP_WORD.contains(word);
    }

    /**
     * 初始化词库
     *
     * @param sensitives 所有敏感词
     */
    public static void initTrie(Set<String> sensitives) {
        if (StringUtils.isNull(sensitives))
            throw new NullPointerException("init trie cannot be null");

        for (String s : sensitives) {
            if (StringUtils.isNull(s)) continue;
            // 按单字分割
            char[] arr = s.toCharArray();
            DFAItem sRoot = ROOT;
            Character tmpChar = null;
            DFAItem tmpItem = null;
            int maxIndex = arr.length - 1;
            for (int i = 0; i < arr.length; i++) {// 跳过首字
                tmpChar = arr[i];
                tmpItem = new DFAItem(tmpChar, i == maxIndex);
                sRoot.addSub(tmpItem);
                sRoot = tmpItem;
            }
        }
    }

    /**
     * 过滤结果
     */
    public static class DFAResult {
        int start;
        int len;

        DFAResult(int start, int len) {
            this.start = start;
            this.len = len;
        }
    }

    /**
     * DFA元素
     */
    static class DFAItem {
        /**
         * 当前元素
         */
        char word;
        /**
         * 当前元素作有isEnd标识时，所有的头部节点
         */
        Set<Character> heads;
        /**
         * 子节点
         */
        Map<Character, DFAItem> subs;
        /**
         * 标识一个词组的结束，用于最短/最长匹配
         */
        boolean isEnd = false;

        DFAItem(char word, boolean isEnd) {
            this.word = word;
            if (isEnd)
                this.setEnd(word);
        }

        /**
         * 是否找到敏感词
         *
         * @param head
         * @return
         */
        boolean findOne(Character head) {
//            return isEnd && heads.contains(head);
            return isEnd;
        }

        /**
         * 设置 isEnd 标识
         *
         * @param head
         */
        void setEnd(Character head) {
            isEnd = true;
//            if (null == heads)
//                heads = new HashSet<>(1);
//            heads.add(head);
        }

        DFAItem(char word) {
            this(word, false);
        }

        /**
         * 增加子项
         *
         * @param word
         */
        void addSub(char word) {
            addSub(word, false);
        }

        /**
         * 增加子项
         *
         * @param word
         * @param isEnd
         */
        void addSub(char word, boolean isEnd) {
            addSub(new DFAItem(word, isEnd));
        }

        void addSub(DFAItem item) {
            if (null == subs)
                subs = new HashMap<>(1);
            DFAItem old = subs.get(item);
            if (null != old) {
                if (item.isEnd)
                    old.isEnd = true;
            } else
                subs.put(item.word, item);
        }

        /**
         * 是否包含子项
         *
         * @return
         */
        boolean hasSub() {
            return null != subs && !subs.isEmpty();
        }

        /**
         * 获取指定叶子节点
         *
         * @param word
         * @return
         */
        DFAItem getSub(Character word) {
            return hasSub() ? subs.get(word) : null;
        }

        @Override
        public boolean equals(Object obj) {
            if (null == obj) return false;
            if (DFAItem.class.isInstance(obj))
                return word == ((DFAItem) obj).word;
            if (Character.class.isInstance(obj))
                return word == (Character) obj;

            return false;
        }

        @Override
        public int hashCode() {
            return Character.hashCode(word);
        }
    }

    public static void main(String[] args) {
        String word = "中bcd";
        System.out.println(Arrays.toString(word.split("")));
        char a = word.charAt(0);
        char b = '中';
        System.out.println("char compare: " + (a == b));
        Character c = new Character('中');
        System.out.println("Character compare: " + (a == c));
        System.out.println("Character hashCode: " + c.hashCode());
        System.out.println("char hashCode: " + Character.hashCode(a));
    }
}
