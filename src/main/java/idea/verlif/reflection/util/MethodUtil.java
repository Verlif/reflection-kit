package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.ClassGrc;
import idea.verlif.reflection.domain.MethodGrc;
import idea.verlif.reflection.domain.SFunction;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        List<Method> methods = new ArrayList<>();
        do {
            Collections.addAll(methods, cla.getDeclaredMethods());
            cla = cla.getSuperclass();
        } while (cla != null);
        return methods;
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

}
