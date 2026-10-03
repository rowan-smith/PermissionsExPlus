package dev.rono.permissions.core.engine.casbin;

import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;
import dev.rono.permissions.core.resolver.ResolutionSupport;
import java.util.Map;
import org.casbin.jcasbin.util.function.CustomFunction;

final class PexContextFunction extends CustomFunction {
    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
        var required = ResolutionSupport.decodeContexts(FunctionUtils.getStringValue(arg1, env));
        var active = ResolutionSupport.decodeContexts(FunctionUtils.getStringValue(arg2, env));
        return AviatorBoolean.valueOf(ResolutionSupport.applies(required, active));
    }

    @Override
    public String getName() {
        return "pexCtx";
    }
}
