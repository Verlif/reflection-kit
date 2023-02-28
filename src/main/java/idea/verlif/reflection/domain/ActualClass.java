package idea.verlif.reflection.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * 真实类信息
 */
public class ActualClass {

    private final ClassGrc target;

    private final Map<String, FieldGrc> fieldGrcMap;

    private final Map<String, MethodGrc> methodGrcMap;

    public ActualClass() {
        this(new ClassGrc());
    }

    public ActualClass(ClassGrc target) {
        this(target, new HashMap<>(), new HashMap<>());
    }

    public ActualClass(Class<?> target, Map<String, FieldGrc> fieldGrcMap, Map<String, MethodGrc> methodGrcMap) {
        this.target = new ClassGrc(target);
        this.fieldGrcMap = fieldGrcMap;
        this.methodGrcMap = methodGrcMap;
    }

    public ActualClass(ClassGrc target, Map<String, FieldGrc> fieldGrcMap, Map<String, MethodGrc> methodGrcMap) {
        this.target = target;
        this.fieldGrcMap = fieldGrcMap;
        this.methodGrcMap = methodGrcMap;
    }

    public ClassGrc getTarget() {
        return target;
    }

    public Map<String, FieldGrc> getFieldGrcMap() {
        return fieldGrcMap;
    }

    public Map<String, MethodGrc> getMethodGrcMap() {
        return methodGrcMap;
    }

    @Override
    public String toString() {
        return "ActualClass{" +
                "target=" + target +
                ", fieldGrcMap=" + fieldGrcMap +
                ", methodGrcMap=" + methodGrcMap +
                '}';
    }
}
