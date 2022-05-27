package io.github.chad2li.baseutil.util.test;

public class LockTest {

    public static void main(String[] args) throws InterruptedException {
        A a = new A();
        B b = new B();
        System.out.println(A.LOCK_A.equals(B.LOCK_B));
        a.sayA();
        b.sayB();
    }

}

class A {
    public static final String LOCK_A = "LOCK_1231ffw";

    public void sayA() throws InterruptedException {
        synchronized (LOCK_A) {
            Thread.sleep(5000);
            System.out.println("Hello A");
        }
    }
}

class B {
    public static final String LOCK_B = "LOCK_1231ffw";

    public void sayB() throws InterruptedException {
        synchronized (LOCK_B){
            Thread.sleep(1000);
            System.out.println("Hello B");
        }
    }
}
