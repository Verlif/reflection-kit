package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.ClassGrc;
import idea.verlif.reflection.domain.FieldGrc;
import idea.verlif.reflection.domain.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 属性工具
 */
public class FieldUtil {

    /**
     * 获取类的所有属性
     *
     * @param cla 目标类
     * @return 目标类的属性列表，包括父类
     */
    public static List<Field> getAllFields(Class<?> cla) {
        return getAllFields(cla, null);
    }

    /**
     * 通过过滤获取类的所有属性
     *
     * @param cla    目标类
     * @param filter 属性过滤
     * @return 目标类的属性列表，包括父类
     */
    public static List<Field> getAllFields(Class<?> cla, Predicate<Field> filter) {
        List<Field> fields = new ArrayList<>();
        do {
            Collections.addAll(fields, cla.getDeclaredFields());
            cla = cla.getSuperclass();
        } while (cla != null);
        if (filter == null) {
            return fields;
        } else {
            return fields.stream().filter(filter).collect(Collectors.toList());
        }
    }

    /**
     * 从类中获取属性对象
     *
     * @param target    目标类
     * @param fieldName 属性名
     * @return 属性对象
     */
    public static Field getField(Class<?> target, String fieldName) {
        Field field = null;
        do {
            try {
                field = target.getDeclaredField(fieldName);
                break;
            } catch (NoSuchFieldException ignored) {
                target = target.getSuperclass();
            }
        } while (target != null);
        return field;
    }

    /**
     * 从对象中获取属性值
     *
     * @param target    目标对象
     * @param fieldName 属性名称
     * @return 目标对象中的属性值
     * @throws NoSuchFieldException 在目标对象中不存在对应属性
     */
    public static Object getFieldValue(Object target, String fieldName) throws NoSuchFieldException {
        Field field = getField(target.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        return getFieldValue(target, field);
    }

    /**
     * 从对象中获取属性值
     *
     * @param target 目标对象
     * @param field  属性对象
     * @return 目标对象中的属性值
     * @throws NoSuchFieldException 在目标对象中不存在对应属性
     */
    public static Object getFieldValue(Object target, Field field) throws NoSuchFieldException {
        Object value = null;
        boolean acc = field.isAccessible();
        if (!acc) {
            field.setAccessible(true);
        }
        try {
            value = field.get(target);
        } catch (IllegalAccessException ignored) {
        }
        if (!acc) {
            field.setAccessible(false);
        }
        return value;
    }

    /**
     * 设置对象的属性值
     *
     * @param target    目标对象
     * @param fieldName 属性名称
     * @param value     目标对象中的属性值
     * @throws NoSuchFieldException 在目标对象中不存在对应属性
     */
    public static void setFieldValue(Object target, String fieldName, Object value) throws NoSuchFieldException {
        Field field = getField(target.getClass(), fieldName);
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        setFieldValue(target, field, value);
    }

    /**
     * 设置对象的属性值
     *
     * @param target 目标对象
     * @param field  属性对象
     * @param value  目标对象中的属性值
     * @throws NoSuchFieldException 在目标对象中不存在对应属性
     */
    public static void setFieldValue(Object target, Field field, Object value) throws NoSuchFieldException {
        boolean acc = field.isAccessible();
        if (!acc) {
            field.setAccessible(true);
        }
        try {
            field.set(target, value);
        } catch (IllegalAccessException ignored) {
        }
        if (!acc) {
            field.setAccessible(false);
        }
    }

    /**
     * 获取Lambda表达式对应的属性
     *
     * @param function lambda表达式，对应get方法
     * @param <T>      泛型
     * @return 属性对象
     */
    public static <T> Field getFieldFromLambda(SFunction<T, ?> function) {
        SerializedLambda serializedLambda = ReflectUtil.getSerializedLambda(function);
        // 从Lambda表达式中获取属性名
        String implMethodName = serializedLambda.getImplMethodName();
        // 确保方法是符合规范的get方法，boolean类型是is开头
        if (!implMethodName.startsWith("is") && !implMethodName.startsWith("get")) {
            throw new RuntimeException("It's not the standard name - " + implMethodName);
        }

        // 构建属性名
        int prefixLen = implMethodName.startsWith("is") ? 2 : 3;
        String fieldName = implMethodName.substring(prefixLen);
        String firstChar = fieldName.substring(0, 1);
        fieldName = fieldName.replaceFirst(firstChar, firstChar.toLowerCase());
        // 获取属性
        try {
            return Class.forName(serializedLambda.getImplClass().replace("/", ".")).getDeclaredField(fieldName);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取属性的泛型信息
     *
     * @param field 目标属性
     * @return 属性的泛型信息
     */
    public static FieldGrc getFieldGrc(Field field) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        return getFieldGrc(field, field.getDeclaringClass());
    }

    /**
     * 获取属性的泛型信息
     *
     * @param field  目标属性
     * @param target 属性的目标所属类
     * @return 属性的泛型信息
     */
    public static FieldGrc getFieldGrc(Field field, Class<?> target) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Map<String, ClassGrc> genericsMap = ReflectUtil.getGenericsMap(target);
        return getFieldGrc(field, genericsMap);
    }

    /**
     * 获取属性的泛型信息
     *
     * @param field       目标属性
     * @param genericsMap 属性的泛型表
     * @return 属性的泛型信息
     */
    public static FieldGrc getFieldGrc(Field field, Map<String, ClassGrc> genericsMap) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        String sig = SignatureUtil.getSignature(field);
        if (sig == null) {
            return new FieldGrc(field);
        } else {
            ClassGrc classGrc = SignatureUtil.parseClassBySignature(sig.substring(0, sig.length() - 1), genericsMap);
            return new FieldGrc(field, classGrc.getTarget(), classGrc.getGenericsInfos());
        }
    }
}
