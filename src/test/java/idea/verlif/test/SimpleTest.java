package idea.verlif.test;

import idea.verlif.reflection.util.FieldUtil;
import idea.verlif.reflection.util.MethodUtil;
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
        TestB<Object> objectTestB = new TestB<>();
        objectTestB.setList(new ArrayList<>());
        System.out.println(FieldUtil.getFieldValue(objectTestB, "list"));
        MethodUtil.invoke(new TestB<String>(), "aList", "123");
        System.out.println(ReflectUtil.likeClass(Integer.class, int.class));
        System.out.println(ReflectUtil.likeClass(int.class, Integer.class));
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

    public TestA<String, List<String>> testA() {
        return null;
    }

    public static class TestB<T> extends TestA<String, List<String>> implements InterfaceA<InterfaceA<Double>> {

        private List<String> list;

        private Map<Integer, Double> map;

        public List<String> getList() {
            return list;
        }

        public void setList(List<String> list) {
            this.list = list;
        }

        public Map<Integer, Double> getMap() {
            return map;
        }

        public void setMap(Map<Integer, Double> map) {
            this.map = map;
        }

        public void aList(String a) {
            System.out.println("single string " + a);
        }

        public void aList(int a) {
            System.out.println("single int " + a);
        }

        public void aList(int a, int... b) {
            System.out.println(a);
            System.out.println(Arrays.toString(b));
        }

        public void aList(String a, int... b) {
            System.out.println(a);
            System.out.println(Arrays.toString(b));
        }
    }

    public interface InterfaceA<T> {

        default T getT() {
            return null;
        }
    }

    public static class TestA<R, V> {

        private R r;

        private V v;

        private List<String> list;

        private Map<List<String>, R> map;

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

        public void a() {
        }

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
