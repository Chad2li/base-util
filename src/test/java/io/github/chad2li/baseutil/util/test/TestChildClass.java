package io.github.chad2li.baseutil.util.test;

import java.util.Arrays;

/**
 * Created by chad on 2016/11/9.
 */
public class TestChildClass
{
    public static void main(String[] args)
    {
        Demo2 d2 = new Demo2();
        System.out.println("Demo2 instanceof Demo1 >> " + (d2 instanceof Demo1));

        Class c1 = Demo1.class;
        for (Class tmp = Demo3.class; !tmp.equals(Object.class); tmp = tmp.getSuperclass())
            System.out.println(tmp.getSimpleName() + " >> " + tmp.equals(c1));

        System.out.println("interface equals >> " + (Demo1.class.getInterfaces()[0].equals(DemoInterface.class)));
        System.out.println("interface isAssignableFrom >> " + (Demo1.class.getInterfaces()[0].isAssignableFrom(DemoInterface.class)));


        System.out.println("object interface >> " + Arrays.toString(Object.class.getInterfaces()));
    }

    public interface DemoInterface
    {

    }

    public static class Demo1 implements DemoInterface
    {

    }

    public static class Demo2 extends Demo1
    {

    }

    public static class Demo3 extends Demo2
    {

    }
}
