package idea.verlif.reflection.domain;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class MethodGrc {

    /**
     * 方法对象
     */
    private final Method method;

    /**
     * 方法返回值
     */
    private final ClassGrc result;

    /**
     * 方法参数
     */
    private final ClassGrc[] arguments;

    public MethodGrc(Method method, ClassGrc result, ClassGrc[] arguments) {
        this.method = method;
        this.result = result;
        this.arguments = arguments;
    }

    public Method getMethod() {
        return method;
    }

    public ClassGrc getResult() {
        return result;
    }

    public ClassGrc[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "MethodGrc{" +
                "method=" + method +
                ", result=" + result +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
