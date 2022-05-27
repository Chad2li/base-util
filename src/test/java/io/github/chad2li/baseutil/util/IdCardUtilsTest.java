package io.github.chad2li.baseutil.util;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class IdCardUtilsTest
{
    private static final String idcard = "411081199004235955";

    @Test
    public void validate()
    {
        // succ
        boolean valid = IdCardUtils.validate(idcard);
        Assert.assertTrue(valid);

        // length
        valid = IdCardUtils.validate("123123");
        Assert.assertFalse(valid);

        // check code
        valid = IdCardUtils.validate(idcard.substring(0, idcard.length()) + "0");
        Assert.assertFalse(valid);
    }

    @Test
    public void getAddressCode()
    {
        String addr = IdCardUtils.getAddressCode(idcard);
        Assert.assertEquals(idcard.substring(0, 6), addr);
    }

    @Test
    public void parseBirthDate()
    {
        LocalDate birth = IdCardUtils.parseBirthDate(idcard);

        String part = idcard.substring(6, 14);
        LocalDate expect = LocalDate.parse(part, DateTimeFormatter.ofPattern("yyyyMMdd"));

        Assert.assertEquals(expect, birth);
    }

    @Test
    public void isMale()
    {
        boolean ismale = IdCardUtils.isMale(idcard);
        Assert.assertTrue(ismale);
    }

    @Test
    public void isFemale()
    {
        boolean isfemale = IdCardUtils.isFemale(idcard);
        Assert.assertFalse(isfemale);
    }
}