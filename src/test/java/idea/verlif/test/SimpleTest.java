package idea.verlif.test;

import idea.verlif.reflection.domain.ClassGrc;
import idea.verlif.reflection.util.FieldUtil;
import idea.verlif.reflection.util.MethodUtil;
import idea.verlif.reflection.util.ObjectUtil;
import idea.verlif.reflection.util.ReflectUtil;
import org.junit.Test;
import stopwatch.Stopwatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SimpleTest {

    @Test
    public void test() throws Exception {
        A a = new A();
        a.a = "123";
        a.b = 1;
        a.d = 'd';
        B b = new B();
        a.o = b;
        C c = new C();
        A a2 = new A();
        ObjectUtil.copy(a, b);
        ObjectUtil.copy(a, c);
        ObjectUtil.copy(a, a2);
        System.out.println(b.a);
    }

    @Test
    public void recalculate() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (int i = 0; i < 1000; i++) {
            ReflectUtil.noPrimitive(short.class);
        }
        stopwatch.pin();
        for (int i = 0; i < 1000; i++) {
            ReflectUtil.toPrimitive(short.class);
        }
        stopwatch.stop();
        System.out.println(Arrays.toString(stopwatch.getIntervalLine(TimeUnit.MICROSECONDS).toArray()));
    }

    public static class A {
        private String a;
        private int b;
        private char d;
        private B o;
    }

    public static class B {
        private String a;
        private double b;
        private boolean c;
        private A o;
    }

    public static final class C extends A {

    }
}
