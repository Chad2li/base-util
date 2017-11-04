package cn.lyjuan.base.util;


import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具
 * 参考：http://www.cnblogs.com/bluestorm/archive/2012/07/23/2605412.html
 * Created by chad on 04/11/2017.
 */
public class PinyinUtils
{
    private static final HanyuPinyinOutputFormat format;

    private static final String TAG = PinyinUtils.class.getSimpleName();

    static
    {
        format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 转换单个字符
     *
     * @return {@code content}为空时返回空字符串
     */
    private static String signlePinyin(char c)

    {
        if (isNull(String.valueOf(c))) return "";
        String pinyin[] = null;
        try
        {
            pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format);

        } catch (BadHanyuPinyinOutputFormatCombination e)
        {
            e.printStackTrace();
        }

        if (null == pinyin || pinyin.length < 1) return "";

        return pinyin[0];
    }

    /**
     * 解析字符串的拼音，多音字使用第一个
     *
     * @param content
     * @return 拼音部分小写，非拼音部分保持原留；{@code content}为空时返回空字符串
     */
    public static String pinyin(String content)
    {
        if (isNull(content)) return "";
        StringBuilder sb = new StringBuilder();

        String tempPinyin = null;

        for (int i = 0; i < content.length(); ++i)

        {
            tempPinyin = signlePinyin(content.charAt(i));
            // 如果str.charAt(i)非汉字，则保持原样
            sb.append(isNull(tempPinyin) ? content.charAt(i) : tempPinyin);
        }

        return sb.toString();
    }

    /**
     * 解析拼音首写字母，多音字使用第一个
     *
     * @param content
     * @return 全小写，非拼音部分原样保留；{@code content}为空时返回空字符串
     */
    public static String pinyinSimple(String content)
    {
        if (isNull(content)) return "";
        StringBuilder sb = new StringBuilder();

        String tempPinyin = null;

        for (int i = 0; i < content.length(); ++i)

        {
            tempPinyin = signlePinyin(content.charAt(i));
            // 如果str.charAt(i)非汉字，则忽略

            sb.append(isNull(tempPinyin) ? content.charAt(i) : tempPinyin.charAt(0));
        }

        return sb.toString();
    }

    /**
     * 同时返回拼音和拼音简写
     *
     * @param content
     * @return 索引0为拼音，索引1为拼音简写；{@code content}为空时返回空字符串数组
     */
    public static String[] pinyinAndSimple(String content)
    {
        if (isNull(content)) return new String[]{"", ""};
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();

        String tempPinyin = null;

        for (int i = 0; i < content.length(); ++i)

        {
            tempPinyin = signlePinyin(content.charAt(i));
            if (isNull(tempPinyin))
            {
                sb1.append(content.charAt(i));
                sb2.append(content.charAt(i));
            } else
            {
                sb1.append(tempPinyin);
                sb2.append(tempPinyin.charAt(0));
            }
        }

        return new String[]{sb1.toString(), sb2.toString()};
    }

    /**
     * 空字符串
     *
     * @param content
     * @return
     */
    private static boolean isNull(String content)
    {
        return null == content || content.trim().length() < 1;
    }
}
