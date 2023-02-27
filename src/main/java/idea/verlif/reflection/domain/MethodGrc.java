package idea.verlif.reflection.domain;

import java.lang.reflect.Method;
import java.util.Arrays;

public final class MethodGrc {

    private final ClassGrc result;

    private final ClassGrc[] arguments;

    public MethodGrc(ClassGrc result, ClassGrc[] arguments) {
        this.result = result;
        this.arguments = arguments;
    }

    public ClassGrc getResult() {
        return result;
    }

    public ClassGrc[] getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "MethodInfo{" +
                "result=" + result +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
