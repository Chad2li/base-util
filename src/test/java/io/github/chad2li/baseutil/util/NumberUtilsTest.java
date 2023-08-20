package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NumberUtilsTest
 *
 * @author chad
 * @copyright 2023 chad
 * @since created at 2023/8/20 16:21
 */
class NumberUtilsTest {

    @Test
    void isPositive() {
        // 1.1 int
        Integer intVal = null;
        Assert.assertTrue(NumberUtils.isPositive(1));
        Assert.assertFalse(NumberUtils.isPositive(0));
        Assert.assertFalse(NumberUtils.isPositive(-1));
        Assert.assertFalse(NumberUtils.isPositive(intVal));
        // long
        Long longVal = null;
        Assert.assertTrue(NumberUtils.isPositive(1L));
        Assert.assertFalse(NumberUtils.isPositive(0L));
        Assert.assertFalse(NumberUtils.isPositive(-1L));
        Assert.assertFalse(NumberUtils.isPositive(longVal));
        // big decimal
        BigDecimal bigDecimal = null;
        Assert.assertTrue(NumberUtils.isPositive(new BigDecimal("1.01")));
        Assert.assertFalse(NumberUtils.isPositive(new BigDecimal("0")));
        Assert.assertFalse(NumberUtils.isPositive(new BigDecimal("-1")));
        Assert.assertFalse(NumberUtils.isPositive(bigDecimal));
        // string
        String str = null;
        Assert.assertTrue(NumberUtils.isPositive("1"));
        Assert.assertFalse(NumberUtils.isPositive("0"));
        Assert.assertFalse(NumberUtils.isPositive("-1"));
        Assert.assertFalse(NumberUtils.isPositive(str));
    }

    @Test
    void isGt() {
        // 1.1 int
        Integer int1 = null;
        Integer int2 = null;
        Assert.assertTrue(NumberUtils.isGt(2, 1));
        Assert.assertTrue(NumberUtils.isGt(0, -1));
        Assert.assertTrue(NumberUtils.isGt(-1, -2));
        // int false
        Assert.assertFalse(NumberUtils.isGt(1, 2));
        Assert.assertFalse(NumberUtils.isGt(1, 1));
        Assert.assertFalse(NumberUtils.isGt(0, 0));
        Assert.assertFalse(NumberUtils.isGt(-2, -1));
        Assert.assertFalse(NumberUtils.isGt(int1, 2));
        Assert.assertFalse(NumberUtils.isGt(1, int2));
        Assert.assertFalse(NumberUtils.isGt(int1, int2));
        // long
        Long long1 = null;
        Long long2 = null;
        Assert.assertTrue(NumberUtils.isGt(2L, 1L));
        Assert.assertTrue(NumberUtils.isGt(0L, -1L));
        Assert.assertTrue(NumberUtils.isGt(-1L, -2L));
        // int false
        Assert.assertFalse(NumberUtils.isGt(1L, 2L));
        Assert.assertFalse(NumberUtils.isGt(1L, 1L));
        Assert.assertFalse(NumberUtils.isGt(0L, 0L));
        Assert.assertFalse(NumberUtils.isGt(-2L, -1L));
        Assert.assertFalse(NumberUtils.isGt(long1, 2L));
        Assert.assertFalse(NumberUtils.isGt(1L, long2));
        Assert.assertFalse(NumberUtils.isGt(long1, long2));
        // big decimal
        BigDecimal big1 = null;
        BigDecimal big2 = null;
        Assert.assertTrue(NumberUtils.isGt(new BigDecimal("2"), new BigDecimal("1")));
        Assert.assertTrue(NumberUtils.isGt(new BigDecimal("0"), new BigDecimal("-1")));
        Assert.assertTrue(NumberUtils.isGt(new BigDecimal("-1"), new BigDecimal("-2")));
        // int false
        Assert.assertFalse(NumberUtils.isGt(new BigDecimal("1"), new BigDecimal("2")));
        Assert.assertFalse(NumberUtils.isGt(new BigDecimal("1"), new BigDecimal("1")));
        Assert.assertFalse(NumberUtils.isGt(new BigDecimal("0"), new BigDecimal("0")));
        Assert.assertFalse(NumberUtils.isGt(new BigDecimal("-2"), new BigDecimal("-1")));
        Assert.assertFalse(NumberUtils.isGt(big1, new BigDecimal("2")));
        Assert.assertFalse(NumberUtils.isGt(new BigDecimal("1"), big2));
        Assert.assertFalse(NumberUtils.isGt(big1, big2));
    }

    @Test
    void isGte() {
        // 1.1 int
        Integer int1 = null;
        Integer int2 = null;
        Assert.assertTrue(NumberUtils.isGte(2, 1));
        Assert.assertTrue(NumberUtils.isGte(0, 0));
        Assert.assertTrue(NumberUtils.isGte(-1, -2));
        // int false
        Assert.assertFalse(NumberUtils.isGte(1, 2));
        Assert.assertFalse(NumberUtils.isGte(-2, -1));
        Assert.assertFalse(NumberUtils.isGte(int1, 2));
        Assert.assertFalse(NumberUtils.isGte(1, int2));
        Assert.assertFalse(NumberUtils.isGte(int1, int2));
        // long
        Long long1 = null;
        Long long2 = null;
        Assert.assertTrue(NumberUtils.isGte(2L, 1L));
        Assert.assertTrue(NumberUtils.isGte(0L, 0L));
        Assert.assertTrue(NumberUtils.isGte(-1L, -2L));
        // int false
        Assert.assertFalse(NumberUtils.isGte(1L, 2L));
        Assert.assertFalse(NumberUtils.isGte(-2L, -1L));
        Assert.assertFalse(NumberUtils.isGte(long1, 2L));
        Assert.assertFalse(NumberUtils.isGte(1L, long2));
        Assert.assertFalse(NumberUtils.isGte(long1, long2));
        // big decimal
        BigDecimal big1 = null;
        BigDecimal big2 = null;
        Assert.assertTrue(NumberUtils.isGte(new BigDecimal("2"), new BigDecimal("1")));
        Assert.assertTrue(NumberUtils.isGte(new BigDecimal("0"), new BigDecimal("0")));
        Assert.assertTrue(NumberUtils.isGte(new BigDecimal("-1"), new BigDecimal("-2")));
        // int false
        Assert.assertFalse(NumberUtils.isGte(new BigDecimal("1"), new BigDecimal("2")));
        Assert.assertFalse(NumberUtils.isGte(new BigDecimal("-2"), new BigDecimal("-1")));
        Assert.assertFalse(NumberUtils.isGte(big1, new BigDecimal("2")));
        Assert.assertFalse(NumberUtils.isGte(new BigDecimal("1"), big2));
        Assert.assertFalse(NumberUtils.isGte(big1, big2));
    }
}