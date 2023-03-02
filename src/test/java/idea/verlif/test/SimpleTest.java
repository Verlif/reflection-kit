package idea.verlif.test;

import idea.verlif.reflection.domain.ActualClass;
import idea.verlif.reflection.domain.FieldGrc;
import idea.verlif.reflection.util.ReflectUtil;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class SimpleTest {

    @Test
    public void test() throws IOException, ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException {
        ActualClass aClass = ReflectUtil.getActualClass(TestA.class);
        ActualClass bClass = ReflectUtil.getActualClass(TestB.class);
        Field field = TestB.class.getDeclaredField("list");
        FieldGrc fieldGrc = ReflectUtil.getFieldGrc(field);
        System.out.println(bClass);
        System.out.println(int[].class);
        System.out.println(Map[].class);
        System.out.println(Class.forName("[[Ljava.lang.Integer;"));
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
