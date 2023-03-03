package idea.verlif.reflection.util;

import idea.verlif.reflection.domain.*;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.repository.ClassRepository;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 反射方法
 *
 * @author Verlif
 */
public class ReflectUtil {

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
     * @param classGrc 类信息
     * @return 参数类型包含的泛型表
     */
    public static Map<String, ClassGrc> getGenericsMap(ClassGrc classGrc) throws NoSuchFieldException, IllegalAccessException {
        if (classGrc.getGenericsInfos().length == 0) {
            return getGenericsMap(classGrc.getClass());
        }
        Map<String, ClassGrc> genericsMap = new HashMap<>();
        TypeVariable<? extends Class<?>>[] typeParameters = classGrc.getTarget().getTypeParameters();
        ClassGrc[] genericsInfos = classGrc.getGenericsInfos();
        for (int i = 0, size = Math.min(typeParameters.length, genericsInfos.length); i < size; i++) {
            genericsMap.put(typeParameters[i].getName(), genericsInfos[i]);
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
     * 获取类的泛型转换信息
     *
     * @param target 目标类
     * @return 真实类信息
     */
    public static ActualClass getActualClass(Class<?> target) throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        Map<String, ClassGrc> genericsMap = getGenericsMap(target);
        List<Field> allFields = FieldUtil.getAllFields(target);
        Map<String, FieldGrc> fieldGrcMap = new HashMap<>(allFields.size());
        for (Field field : allFields) {
            fieldGrcMap.put(field.getName(), FieldUtil.getFieldGrc(field, genericsMap));
        }
        List<Method> allMethods = MethodUtil.getAllMethods(target);
        Map<String, MethodGrc> methodGrcMap = new HashMap<>(allMethods.size());
        for (Method method : allMethods) {
            methodGrcMap.put(method.getName(), MethodUtil.getMethodGrc(method, target));
        }

        return new ActualClass(getClassGrc(target), fieldGrcMap, methodGrcMap);
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
    public static <T> T newInstance(Class<T> cla, Object... params) throws InstantiationException, IllegalAccessException, InvocationTargetException {
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
