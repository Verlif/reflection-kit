package idea.verlif.reflection.domain;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class FieldGrc {

    /**
     * 属性对象
     */
    private final Field field;

    /**
     * 属性类型
     */
    private final Class<?> target;

    /**
     * 属性包括的泛型
     */
    private final ClassGrc[] genericsInfos;

    public FieldGrc(Field field, Class<?> target, ClassGrc[] genericsInfos) {
        this.field = field;
        this.target = target;
        this.genericsInfos = genericsInfos;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getTarget() {
        return target;
    }

    public ClassGrc[] getGenericsInfos() {
        return genericsInfos;
    }

    @Override
    public String toString() {
        return "FieldGrc{" +
                "target=" + target +
                ", field=" + field +
                ", genericsInfos=" + Arrays.toString(genericsInfos) +
                '}';
    }
}
