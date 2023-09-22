package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.ClassGrc;
import idea.verlif.reflection.domain.MethodGrc;
import idea.verlif.reflection.domain.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 方法工具类
 */
public class MethodUtil {

    private static final Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\((.*)\\).*");

    /**
     * 获取类的所有方法
     *
     * @param cla 目标类
     * @return 目标类的方法列表，包括父类
     */
    public static List<Method> getAllMethods(Class<?> cla) {
        return getAllMethods(cla, null);
    }

    /**
     * 通过过滤获取类的所有方法
     *
     * @param cla    目标类
     * @param filter 方法过滤
     * @return 目标类的方法列表，包括父类
     */
    public static List<Method> getAllMethods(Class<?> cla, Predicate<Method> filter) {
        List<Method> methods = new ArrayList<>();
        do {
            Collections.addAll(methods, cla.getDeclaredMethods());
            cla = cla.getSuperclass();
        } while (cla != null);
        if (filter == null) {
            return methods;
        } else {
            return methods.stream().filter(filter).collect(Collectors.toList());
        }
    }

    /**
     * 获取方法泛型信息
     *
     * @param method 目标方法
     * @param target 目标绑定类
     * @return 方法泛型信息
     */
    public static MethodGrc getMethodGrc(Method method, Class<?> target) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Map<String, ClassGrc> genericsMap = ReflectUtil.getGenericsMap(target);
        String sig = SignatureUtil.getSignature(method);
        // 有泛型类
        if (sig != null) {
            // 解析泛型
            if (sig.charAt(0) == '<') {
                String generics = sig.substring(1, sig.indexOf('>') - 1);
                sig = sig.substring(generics.length() + 3);
                List<String> sigList = SignatureUtil.splitSignature(generics);
                for (String s : sigList) {
                    if (s.length() > 0) {
                        int i = s.indexOf(':');
                        String name = s.substring(0, i);
                        String className = s.substring(i + 2).replace("/", ".");
                        Class<?> aClass = Class.forName(className);
                        genericsMap.put(name, new ClassGrc(aClass));
                    }
                }
            }
            // 解析参数
            ClassGrc[] infos = new ClassGrc[0];
            if (sig.charAt(0) == '(') {
                int end = sig.indexOf(')');
                // 无参方法
                if (end == 1) {
                    sig = sig.substring(2);
                } else {
                    String params = sig.substring(1, end - 1);
                    sig = sig.substring(params.length() + 3);
                    List<String> sigList = SignatureUtil.splitSignature(params);
                    infos = new ClassGrc[sigList.size()];
                    for (int i = 0; i < sigList.size(); i++) {
                        ClassGrc info = SignatureUtil.parseClassBySignature(sigList.get(i), genericsMap);
                        infos[i] = info;
                    }
                }
            }
            ClassGrc info = null;
            // 解析返回值
            if (sig.length() > 0) {
                // 是否是泛型
                String sigName = sig.substring(0, sig.length() - 1);
                if (sigName.length() > 0) {
                    info = SignatureUtil.parseClassBySignature(sigName, genericsMap);
                }
            }
            return new MethodGrc(method, info, infos);
        } else {
            ClassGrc result = ReflectUtil.getClassGrc(method.getReturnType());
            Class<?>[] parameterTypes = method.getParameterTypes();
            ClassGrc[] argumentInfos = new ClassGrc[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                argumentInfos[i] = ReflectUtil.getClassGrc(parameterTypes[i]);
            }
            return new MethodGrc(method, result, argumentInfos);
        }
    }

    /**
     * 获取方法泛型信息
     *
     * @param method 目标方法
     * @return 方法泛型信息
     */
    public static MethodGrc getMethodGrc(Method method) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        return getMethodGrc(method, method.getDeclaringClass());
    }

    /**
     * 从类中获取指定方法
     *
     * @param target     目标类
     * @param name       方法名
     * @param paramTypes 方法参数类型数组
     * @return 获取到的方法
     */
    public static Method getMethod(Class<?> target, String name, Class<?>... paramTypes) {
        Method like = null;
        METHOD_LOOP:
        for (Method method : getAllMethods(target)) {
            if (method.getName().equals(name)) {
                int mpl = method.getParameterCount();
                int pl = paramTypes.length;
                // 匹配时，pl的值必定小于或等于mpl。小于的情况必定只小1且方法最后一个参数是数组
                if (mpl > pl + 1 || mpl < pl) {
                    continue;
                }
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (mpl == pl + 1 && !parameterTypes[mpl - 1].isArray()) {
                    continue;
                }
                for (int i = 0; i < pl; i++) {
                    if (!ReflectUtil.likeClass(parameterTypes[i], paramTypes[i])) {
                        // I know what I do.
                        continue METHOD_LOOP;
                    }
                }
                if (pl == mpl) {
                    return method;
                } else {
                    like = method;
                }
            }
        }
        return like;
    }


    /**
     * 获取类的属性set方法
     *
     * @param target 目标类
     * @param field  属性对象
     * @return 属性set方法
     */
    public static Method getSetter(Class<?> target, Field field) {
        String name = field.getName();
        char first = name.charAt(0);
        return getMethod(target, "set" + toUpper(first) + name.substring(1), field.getType());
    }

    /**
     * 获取类的属性set方法
     *
     * @param target    目标类
     * @param fieldName 属性名
     * @return 属性set方法
     */
    public static <T> Method getSetter(Class<T> target, String fieldName) {
        Field field = FieldUtil.getField(target, fieldName);
        return getSetter(target, field);
    }

    /**
     * 获取类的属性set方法
     *
     * @param target   目标类
     * @param function 属性getter的lambda表达式
     * @return 属性set方法
     */
    public static <T> Method getSetter(Class<T> target, SFunction<T, ?> function) {
        Field field = FieldUtil.getFieldFromLambda(function);
        return getSetter(target, field);
    }

    private static char toUpper(char c) {
        if (c >= 'a' && c <= 'z') {
            return (char) (c - 'a' + 'A');
        }
        return c;
    }

    /**
     * 获取类的属性get方法
     *
     * @param target 目标类
     * @param field  属性对象
     * @return 属性get方法
     */
    public static Method getGetter(Class<?> target, Field field) {
        return getGetter(target, field.getName());
    }

    /**
     * 获取类的属性get方法
     *
     * @param target    目标类
     * @param fieldName 属性名
     * @return 属性get方法
     */
    public static <T> Method getGetter(Class<T> target, String fieldName) {
        char first = fieldName.charAt(0);
        return getMethod(target, "get" + toUpper(first) + fieldName.substring(1));
    }

    /**
     * 获取类的属性get方法
     *
     * @param target   目标类
     * @param function 属性getter的lambda表达式
     * @return 属性get方法
     */
    public static <T> Method getGetter(Class<T> target, SFunction<T, ?> function) {
        Field field = FieldUtil.getFieldFromLambda(function);
        return getGetter(target, field);
    }

    /**
     * 获取Lambda表达式对应的属性
     *
     * @param function lambda表达式，对应get方法
     * @param <T>      泛型
     * @return 属性对象
     */
    public static <T, R> Method getMethodFromLambda(SFunction<T, R> function) {
        SerializedLambda serializedLambda = ReflectUtil.getSerializedLambda(function);

        // 获取方法参数类型
        String expr = serializedLambda.getImplMethodSignature();
        Matcher matcher = PARAMETER_TYPE_PATTERN.matcher(expr);
        if (!matcher.find() || matcher.groupCount() != 1) {
            throw new RuntimeException("Lambda parsing failed!");
        }
        expr = matcher.group(1);
        Class<?>[] claArray;
        if (expr.length() == 0) {
            claArray = new Class[0];
        } else {
            claArray = Arrays.stream(expr.split(";"))
                    .filter(s -> !s.isEmpty())
                    .map(s -> s.replace("L", "").replace("/", "."))
                    .map(s -> {
                        try {
                            return Class.forName(s);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException("Can not found class - " + s, e);
                        }
                    }).toArray(Class[]::new);
        }
        try {
            Class<?> aClass = Class.forName(serializedLambda.getImplClass().replace("/", "."));
            return aClass.getMethod(serializedLambda.getImplMethodName(), claArray);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 执行对象的方法。自动识别方法最后一位参数是数组的方法。
     *
     * @param target     目标对象
     * @param methodName 执行的方法名
     * @param params     执行方法的参数
     * @return 方法执行返回值
     */
    public static Object invoke(Object target, String methodName, Object... params) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?>[] paramTypes = new Class[params.length];
        for (int i = 0; i < params.length; i++) {
            paramTypes[i] = params[i].getClass();
        }
        Method method = getMethod(target.getClass(), methodName, paramTypes);
        if (method == null) {
            throw new NoSuchMethodException();
        }
        return invoke(target, method, params);
    }

    public static Object invoke(Object target, Method method, Object... params) throws InvocationTargetException, IllegalAccessException {
        int pl = params.length;
        if (method.getParameterCount() > pl) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes[pl].isArray()) {
                Object[] tmp = Arrays.copyOf(params, pl + 1);
                tmp[pl] = Array.newInstance(parameterTypes[pl].getComponentType(), 0);
                params = tmp;
            }
        }
        return method.invoke(target, params);
    }

}
