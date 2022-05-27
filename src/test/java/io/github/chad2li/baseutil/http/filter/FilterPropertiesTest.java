package io.github.chad2li.baseutil.http.filter;

import org.junit.Assert;
import org.junit.Test;

import java.util.regex.Pattern;

public class FilterPropertiesTest {

    @Test
    public void testIsSkip() {
        FilterProperties properties = new FilterProperties();
        properties.setExclusionUrlPattern("/druid/.*,/abc");

        Assert.assertTrue(FilterProperties.isSkip(properties, "/druid/api.html"));
        Assert.assertTrue(FilterProperties.isSkip(properties, "/abc"));
    }

    @Test
    public void match(){
        String pattern = "/druid/.*";
        System.out.println(Pattern.matches(pattern, "/druid/api.html"));
    }
}