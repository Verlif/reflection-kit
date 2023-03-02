package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.repository.ClassRepository;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 反射方法
 *
 * @author Verlif
 */
public class ReflectUtil {

    private static final Pattern PARAMETER_TYPE_PATTERN = Pattern.compile("\\((.*)\\).*");

    /**
     * 获取类的所有属性
     *
     * @param cla 目标类
     * @return 目标类的属性列表，包括父类
     */
    public static List<Field> getAllFields(Class<?> cla) {
        List<Field> fields = new ArrayList<>();
        do {
            Collections.addAll(fields, cla.getDeclaredFields());
            cla = cla.getSuperclass();
        } while (cla != null);
        return fields;
    }

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
     * 获取类的泛型标记表
     *
     * @param cl 目标类
     * @return 类包含的泛型表
     */
    public static Map<String, ClassGrc> getGenericsMap(Class<?> cl) throws NoSuchFieldException, IllegalAccessException {
        Map<String, ClassGrc> genericsMap = new HashMap<>();
        // 从继承的类中寻求泛型
        Type type = cl.getGenericSuperclass();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            genericsMap.putAll(getGenericsMap(pType));
        }

        // 从继承的接口中寻求泛型
        Type[] interfaces = cl.getGenericInterfaces();
        for (Type iType : interfaces) {
            if (iType instanceof ParameterizedType) {
                genericsMap.putAll(getGenericsMap((ParameterizedType) iType));
            }
        }
        return genericsMap;
    }

    /**
     * 获取参数类型的泛型标记表
     *
     * @param pType 参数类型
     * @return 参数类型包含的泛型表
     */
    public static Map<String, ClassGrc> getGenericsMap(ParameterizedType pType) throws NoSuchFieldException, IllegalAccessException {
        Map<String, ClassGrc> genericsMap = new HashMap<>();
        Type rawType = pType.getRawType();
        Type[] arguments = pType.getActualTypeArguments();
        if (arguments.length > 0) {
            Field genericInfoField = Class.class.getDeclaredField("genericInfo");
            boolean acc = genericInfoField.isAccessible();
            if (!acc) {
                genericInfoField.setAccessible(true);
            }
            ClassRepository genericInfo = (ClassRepository) genericInfoField.get(rawType);
            if (!acc) {
                genericInfoField.setAccessible(false);
            }
            TypeVariable<?>[] parameters = genericInfo.getTypeParameters();
            for (int i = 0; i < parameters.length; i++) {
                genericsMap.put(parameters[i].getName(), getClassGrc(arguments[i]));
            }
        }
        if (rawType instanceof Class) {
            genericsMap.putAll(getGenericsMap((Class<?>) rawType));
        }

        return genericsMap;
    }

    /**
     * 从Type中获取类泛型信息
     *
     * @param type 目标Type
     */
    public static ClassGrc getClassGrc(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedTypeImpl pType = (ParameterizedTypeImpl) type;
            Class<?> rawType = pType.getRawType();
            Type[] arguments = pType.getActualTypeArguments();
            ClassGrc[] argumentInfos = new ClassGrc[arguments.length];
            for (int i = 0; i < argumentInfos.length; i++) {
                argumentInfos[i] = getClassGrc(arguments[i]);
            }
            return new ClassGrc(rawType, argumentInfos);
        } else if (type instanceof Class) {
            return new ClassGrc((Class<?>) type);
        } else {
            return new ClassGrc();
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
        Map<String, ClassGrc> genericsMap = getGenericsMap(target);
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

    /**
     * 获取方法泛型信息
     *
     * @param method 目标方法
     * @param target 目标绑定类
     * @return 方法泛型信息
     */
    public static MethodGrc getMethodGrc(Method method, Class<?> target) throws ClassNotFoundException, IllegalAccessException, NoSuchFieldException {
        Map<String, ClassGrc> genericsMap = getGenericsMap(target);
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
            ClassGrc result = getClassGrc(method.getReturnType());
            Class<?>[] parameterTypes = method.getParameterTypes();
            ClassGrc[] argumentInfos = new ClassGrc[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                argumentInfos[i] = getClassGrc(parameterTypes[i]);
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
     * 获取类的泛型转换信息
     *
     * @param target 目标类
     * @return 真实类信息
     */
    public static ActualClass getActualClass(Class<?> target) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Map<String, ClassGrc> genericsMap = getGenericsMap(target);
        List<Field> allFields = getAllFields(target);
        Map<String, FieldGrc> fieldGrcMap = new HashMap<>(allFields.size());
        for (Field field : allFields) {
            fieldGrcMap.put(field.getName(), getFieldGrc(field, genericsMap));
        }
        List<Method> allMethods = getAllMethods(target);
        Map<String, MethodGrc> methodGrcMap = new HashMap<>(allMethods.size());
        for (Method method : allMethods) {
            methodGrcMap.put(method.getName(), getMethodGrc(method, target));
        }

        return new ActualClass(getClassGrc(target), fieldGrcMap, methodGrcMap);
    }

    /**
     * 获取Lambda表达式对应的属性
     *
     * @param function lambda表达式，对应get方法
     * @param <T>      泛型
     * @return 属性对象
     */
    public static <T> Field getFieldFromLambda(SFunction<T, ?> function) {
        SerializedLambda serializedLambda = getSerializedLambda(function);
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
     * 获取Lambda表达式对应的属性
     *
     * @param function lambda表达式，对应get方法
     * @param <T>      泛型
     * @return 属性对象
     */
    public static <T, R> Method getMethodFromLambda(SFunction<T, R> function) {
        SerializedLambda serializedLambda = getSerializedLambda(function);

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
     * 获取Lambda表达式的SerializedLambda对象
     *
     * @param function Lambda表达式
     * @return SerializedLambda对象
     */
    public static <T> SerializedLambda getSerializedLambda(SFunction<T, ?> function) {
        // 获取序列化方法
        Method writeReplaceMethod;
        try {
            writeReplaceMethod = function.getClass().getDeclaredMethod("writeReplace");
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        // 从序列化方法中获取Lambda表达式信息
        boolean isAccessible = writeReplaceMethod.isAccessible();
        // 如果isAccessible为false则进行设定
        if (!isAccessible) {
            writeReplaceMethod.setAccessible(true);
        }
        SerializedLambda serializedLambda;
        try {
            serializedLambda = (SerializedLambda) writeReplaceMethod.invoke(function);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        // 还原access设定
        if (!isAccessible) {
            writeReplaceMethod.setAccessible(false);
        }
        return serializedLambda;
    }

    /**
     * 通过构造器产生实例对象
     *
     * @param cla    目标类
     * @param params 构造器参数
     * @param <T>    实例类
     * @return 实例对象
     */
    public static <T> T newInstance(Class<T> cla, Object... params) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        // 当没有参数是直接调用无参构造器方法
        if (params.length == 0) {
            return cla.newInstance();
        }
        // 遍历构造器查询对应参数的构造器方法
        Constructor<?>[] constructors = cla.getConstructors();
        LOOP_constructor:
        for (Constructor<?> constructor : constructors) {
            // 如果构造器参数数量相同则进一步验证
            if (constructor.getParameterCount() == params.length) {
                Class<?>[] types = constructor.getParameterTypes();
                // 遍历构造器参数并逐一校验参数类型是否相同
                for (int i = 0; i < types.length; i++) {
                    // 参数不相同则跳过此构造器
                    if (!recalculate(types[i]).isAssignableFrom(params[i].getClass())) {
                        continue LOOP_constructor;
                    }
                }
                return (T) constructor.newInstance(params);
            }
        }
        return null;
    }

    private static Class<?> recalculate(Class<?> cl) {
        switch (cl.getSimpleName()) {
            case "int":
                cl = Integer.class;
                break;
            case "double":
                cl = Double.class;
                break;
            case "float":
                cl = Float.class;
                break;
            case "byte":
                cl = Byte.class;
                break;
            case "short":
                cl = Short.class;
                break;
            case "long":
                cl = Long.class;
                break;
            case "boolean":
                cl = Boolean.class;
                break;
            case "char":
                cl = Character.class;
                break;
            default:
        }
        return cl;
    }

}
