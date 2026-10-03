package dev.rono.permissions.core.engine.casbin;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.util.Map;
import java.util.Objects;
import org.casbin.jcasbin.util.function.CustomFunction;

final class PexMatchFunction extends CustomFunction {
    private final ResolutionSupport support;

    PexMatchFunction(ResolutionSupport support) {
        this.support = Objects.requireNonNull(support, "support");
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        var requested = FunctionUtils.getStringValue(arg1, env);
        var expression = FunctionUtils.getStringValue(arg2, env);
        return AviatorBoolean.valueOf(support.matches(expression, requested));
    }

    @Override
    public String getName() {
        return "pexMatch";
    }
}
