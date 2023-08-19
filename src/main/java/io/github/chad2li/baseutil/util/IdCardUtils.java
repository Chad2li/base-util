package io.github.chad2li.baseutil.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 验证身份证号码 身份证号码, 可以解析身份证号码的各个字段，以及验证身份证号码是否有效;<br />
 * 身份证号码构成：6位地址编码+8位生日+3位顺序码+1位校验码<br />
 * 来源：http://blog.csdn.net/fssf0079/article/details/19121125
 *
 * @author chad
 */
@Slf4j
public class IdCardUtils {
//    private String cardNumber; // 完整的身份证号码
//    private Boolean cacheValidateResult = null; // 缓存身份证是否有效，因为验证有效性使用频繁且计算复杂
    /**
     * 身份证号码中的出生日期的格式工具
     */
    private final static DateTimeFormatter BIRTH_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 身份证最小出生日期
     */
    private final static LocalDate MINIMAL_BIRTH_DATE = LocalDate.of(1900, 1, 1); // 身份证的最小出生日期,1900年1月1日
    /**
     * 新身份证长度
     */
    private final static int NEW_CARD_NUMBER_LENGTH = 18;
    /**
     * 旧身份证长度
     */
    private final static int OLD_CARD_NUMBER_LENGTH = 15;
    /**
     * 18位身份证中最后一位校验码
     */
    private final static char[] VERIFY_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    /**
     * 18位身份证中，各个数字的生成校验码时的权值
     */
    private final static int[] VERIFY_CODE_WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 校验号码是否有效
     *
     * @param idCard
     * @return
     */
    public static boolean validate(String idCard) {
        if (CharSequenceUtil.isEmpty(idCard)) {
            // 不能为空
            return false;
        }
        String tmpIdCard = idCard.trim();
        if (tmpIdCard.length() == OLD_CARD_NUMBER_LENGTH) {
            // 15位转18位
            tmpIdCard = old2new(tmpIdCard);
        }
        if (NEW_CARD_NUMBER_LENGTH != tmpIdCard.length()) {
            // 长度不足
            return false;
        }
        // 身份证号的前17位必须是阿拉伯数字
        if (!CharSequenceUtil.isNumeric(tmpIdCard.substring(0, NEW_CARD_NUMBER_LENGTH - 1))) {
            return false;
        }
        boolean result;
        // 身份证号的第18位校验正确
        char code = calculateVerifyCode(tmpIdCard);
        result = code == tmpIdCard.charAt(NEW_CARD_NUMBER_LENGTH - 1);
        if (!result) {
            // 校验不通过
            return false;
        }
        // 出生日期不能晚于当前时间，并且不能早于1900年
        try {
            LocalDate birth = parseBirthDate(tmpIdCard);
            // 出生年月在当前日期前
            result = birth.isBefore(LocalDate.now());
            if (!result) {
                return false;
            }
            // 出生年月在身份证最小日期后
            result = birth.isAfter(MINIMAL_BIRTH_DATE);
            if (!result) {
                return false;
            }

            /*
             * 出生日期中的年、月、日必须正确,比如月份范围是[1,12],日期范围是[1,31]，还需要校验闰年、大月、小月的情况时，
             * 月份和日期相符合
             */
            String birthdayPart = parseBirthPart(tmpIdCard);// 出生年月部分
            String realBirthdayPart = formatBirthDate(birthdayPart).format(BIRTH_DATE_FORMAT);// 解析日期，看是否相同
            if (!realBirthdayPart.equalsIgnoreCase(birthdayPart)) return false;
        } catch (Exception e) {
            log.error("valid idCard error, idCard:{}", idCard, e);
            return false;
        }
        // todo 完整身份证号码的省市县区检验规则

        return true;
    }

    /**
     * 获取身份证地区段
     *
     * @param idcard
     * @return
     */
    public static String getAddressCode(String idcard) {
        idcard = old2new(idcard);

        return idcard.substring(0, 6);
    }

    /**
     * 获取身份证上的出生年月
     *
     * @param idcard
     * @return
     */
    public static LocalDate parseBirthDate(String idcard) {
        idcard = old2new(idcard);

        try {
            LocalDate birth = formatBirthDate(parseBirthPart(idcard));

            return birth;
        } catch (Exception e) {
            throw new RuntimeException("birthday of idcard:" + idcard + " format error");
        }
    }

    /**
     * 是否为男性
     *
     * @return
     */
    public static boolean isMale(String idcard) {
        idcard = old2new(idcard);

        return 1 == parseGenderCode(idcard);
    }

    /**
     * 是否为女性
     *
     * @param idcard
     * @return
     */
    public static boolean isFemale(String idcard) {
        idcard = old2new(idcard);

        return 0 == parseGenderCode(idcard);
    }

    /**
     * 获取身份证的第17位，奇数为男性，偶数为女性
     *
     * @return
     */
    private static int parseGenderCode(String idcard) {
        idcard = old2new(idcard);
        checkIfValid(idcard);
        char genderCode = idcard.charAt(NEW_CARD_NUMBER_LENGTH - 2);
        return ((genderCode - '0') & 0x1);
    }

    private static String parseBirthPart(String idcard) {
        idcard = old2new(idcard);

        return idcard.substring(6, 14);
    }

    /**
     * 解析出生年月
     *
     * @param birthPart 身份证上的出生年月
     * @return
     */
    private static LocalDate formatBirthDate(String birthPart) {
        return LocalDate.parse(birthPart, BIRTH_DATE_FORMAT);
    }

    private static void checkIfValid(String idcard) {
        idcard = old2new(idcard);

        if (false == validate(idcard))
            throw new RuntimeException("身份证号码不正确！");
    }

    /**
     * 校验码（第十八位数）：
     * <p>
     * 十七位数字本体码加权求和公式 S = Sum(Ai * Wi), i = 0...16 ，先对前17位数字的权求和；
     * Ai:表示第i位置上的身份证号码数字值 Wi:表示第i位置上的加权因子 Wi: 7 9 10 5 8 4 2 1 6 3 7 9 10 5 8 4
     * 2; 计算模 Y = mod(S, 11)< 通过模得到对应的校验码 Y: 0 1 2 3 4 5 6 7 8 9 10 校验码: 1 0 X 9
     * 8 7 6 5 4 3 2
     *
     * @param idcard
     * @return
     */
    private static char calculateVerifyCode(String idcard) {
        int sum = 0;
        for (int i = 0; i < NEW_CARD_NUMBER_LENGTH - 1; i++) {
            char ch = idcard.charAt(i);
            sum += ((int) (ch - '0')) * VERIFY_CODE_WEIGHT[i];
        }
        return VERIFY_CODE[sum % 11];
    }

    /**
     * 把15位身份证号码转换到18位身份证号码<br>
     * 15位身份证号码与18位身份证号码的区别为：<br>
     * 1、15位身份证号码中，"出生年份"字段是2位，转换时需要补入"19"，表示20世纪，只有20世纪才会有15号身份证<br>
     * 2、15位身份证无最后一位校验码。18位身份证中，校验码根据根据前17位生成
     *
     * @param old
     * @return
     */
    private static String old2new(String old) {
        Assert.notEmpty(old, "idCard cannot be null");

        if (old.length() != OLD_CARD_NUMBER_LENGTH) return old;

        StringBuilder buf = new StringBuilder(NEW_CARD_NUMBER_LENGTH);
        buf.append(old.substring(0, 6));
        buf.append("19");
        buf.append(old.substring(6));
        buf.append(calculateVerifyCode(buf.toString()));
        return buf.toString();
    }

}
