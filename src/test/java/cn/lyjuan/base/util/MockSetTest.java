package cn.lyjuan.base.util;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Created by chad on 2016/11/9.
 */
public class MockSetTest
{
    public static class DemoMember
    {

    }

    public static class DemoMemberChild extends DemoMember
    {

    }

    public static class Demo1
    {
        private DemoMember m;

        public DemoMember getM()
        {
            return m;
        }
    }

    @Test
    public void setDirect()
    {
        Demo1 d1 = new Demo1();
        DemoMember m1 = new DemoMember();

        MockSet.set(d1, m1);

        Assert.assertEquals(m1, d1.getM());
    }

    @Test
    public void setChildClass()
    {
        Demo1 d1 = new Demo1();
        DemoMember m1 = new DemoMemberChild();

        MockSet.set(d1, m1);

        Assert.assertEquals(m1, d1.getM());
    }

    @Test
    public void setMock()
    {
        Demo1 d1 = new Demo1();
        DemoMember m1 = Mockito.mock(DemoMemberChild.class);

        System.out.println(m1.getClass().getName());

        MockSet.set(d1, m1);

        Assert.assertEquals(m1, d1.getM());
    }

    public static class DemoInterfaceChild implements DemoInterface
    {

    }

    public static class Demo2
    {
        private DemoInterface m;

        public DemoInterface getM()
        {
            return m;
        }
    }

    public interface DemoInterface
    {
    }

    @Test
    public void setMock_Interface()
    {
        Demo2 d2 = new Demo2();
        DemoInterface m1 = Mockito.mock(DemoInterfaceChild.class);

        MockSet.set(d2, m1);

        Assert.assertEquals(m1, d2.getM());
    }
}