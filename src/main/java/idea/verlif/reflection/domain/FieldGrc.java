package idea.verlif.reflection.domain;

import java.lang.reflect.Field;
import java.util.Arrays;

public final class FieldGrc extends ClassGrc {

    /**
     * 属性对象
     */
    private final Field field;

    public FieldGrc(Field field) {
        this(field, field.getType(), new ClassGrc[0]);
    }

    public FieldGrc(Field field, Class<?> target, ClassGrc[] genericsInfos) {
        super(target, genericsInfos);
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    @Override
    public String toString() {
        return "FieldGrc{" +
                "target=" + getTarget() +
                ", field=" + field +
                ", genericsInfos=" + Arrays.toString(getGenericsInfos()) +
                '}';
    }
}
