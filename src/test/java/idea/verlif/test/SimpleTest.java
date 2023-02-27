package idea.verlif.test;

import idea.verlif.reflection.domain.MethodGrc;
import idea.verlif.reflection.util.ReflectUtil;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class SimpleTest {

    @Test
    public void test() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
//        Method a = TestA.class.getDeclaredMethod("a");
//        ReflectUtil.getGenericsType(a);
//
//        Method b = TestA.class.getDeclaredMethod("b");
//        ReflectUtil.getGenericsType(b);
//
//        Method b2 = TestA.class.getDeclaredMethod("b2", Object.class);
//        ReflectUtil.getGenericsType(b2);
//
//        Method c = TestA.class.getDeclaredMethod("c", String.class, Integer.class);
//        ReflectUtil.getGenericsType(c);
//
//        Method d = TestA.class.getDeclaredMethod("d", Map.class);
//        ReflectUtil.getGenericsType(d);
//
//        Method ta = SimpleTest.class.getDeclaredMethod("testA");
//        ReflectUtil.getGenericsType(ta);

        Method te = TestB.class.getMethod("getT");
        MethodGrc methodGrc = ReflectUtil.getMethodGrc(te, TestB.class);
        System.out.println(methodGrc);
        Method tf = TestB.class.getMethod("f", Object.class, Object.class);
        methodGrc = ReflectUtil.getMethodGrc(tf, TestB.class);
        System.out.println(methodGrc);
    }

    public TestA<String, List<String>> testA() {
        return null;
    }

    public static class TestB<T> extends TestA<String, List<String>> implements InterfaceA<InterfaceA<Double>> {

    }

    public interface InterfaceA<T> {

        default T getT() {
            return null;
        }
    }

    public static class TestA<R, V> {

        private R r;

        private V v;

        public R getR() {
            return r;
        }

        public void setR(R r) {
            this.r = r;
        }

        public V getV() {
            return v;
        }

        public void setV(V v) {
            this.v = v;
        }

        public void a() {}

        public <T> T b() {
            return null;
        }

        public <TA, DA> TA b2(DA d) {
            return null;
        }

        public void c(String a, Integer b) {
        }

        public List<String> d(Map<Integer, Double> map) {
            return null;
        }

        public R e() {
            return null;
        }

        public R f(R t, V v) {
            return null;
        }
    }
}
