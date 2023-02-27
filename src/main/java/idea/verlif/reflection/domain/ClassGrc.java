package idea.verlif.reflection.domain;

import java.util.Arrays;

/**
 * 类泛型信息
 */
public final class ClassGrc {

    /**
     * 目标类型
     */
    private final Class<?> target;

    /**
     * 目标类型包括的泛型
     */
    private final ClassGrc[] genericsInfos;

    public ClassGrc(Class<?> target, ClassGrc[] genericsInfos) {
        this.target = target;
        this.genericsInfos = genericsInfos;
    }

    public ClassGrc(Class<?> target) {
        this(target, new ClassGrc[0]);
    }

    public ClassGrc() {
        this(Object.class, new ClassGrc[0]);
    }

    public Class<?> getTarget() {
        return target;
    }

    public ClassGrc[] getGenericsInfos() {
        return genericsInfos;
    }

    @Override
    public String toString() {
        return "ClassInfo{" +
                "target=" + target +
                ", genericsInfos=" + Arrays.toString(genericsInfos) +
                '}';
    }
}
