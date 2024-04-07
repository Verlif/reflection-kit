package idea.verlif.reflection.util;

import java.lang.reflect.Field;
import java.util.List;

/**
 * 对象工具
 */
public class ObjectUtil {

    /**
     * 浅拷贝对象属性，从source的属性值拷贝到target对象中
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        List<Field> allFields = FieldUtil.getAllFields(sourceClass);
        if (sourceClass == targetClass || sourceClass.isAssignableFrom(targetClass)) {
            for (Field field : allFields) {
                try {
                    FieldUtil.setFieldValue(target, field, FieldUtil.getFieldValue(source, field));
                } catch (NoSuchFieldException ignored) {
                }
            }
        } else {
            for (Field field : allFields) {
                Field targetField = FieldUtil.getField(targetClass, field.getName());
                if (targetField != null) {
                    try {
                        FieldUtil.setFieldValue(target, targetField, FieldUtil.getFieldValue(source, field));
                    } catch (NoSuchFieldException | IllegalArgumentException ignored) {
                    }
                }
            }
        }
    }

}
