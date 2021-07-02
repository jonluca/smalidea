package org.jf.smalidea.debugging;

import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.DebuggerUtils;
import com.intellij.debugger.engine.evaluation.CodeFragmentKind;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl;
import com.intellij.debugger.engine.jdi.StackFrameProxy;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.diagnostic.Logger;
import com.sun.jdi.Value;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.psi.impl.SmaliFile;
import org.jf.smalidea.util.NameUtils;

import java.util.*;

/**
 * Show method arguments of stack frame.
 */
public class SmaliFrameArgumentsProvider implements com.intellij.debugger.engine.FrameExtraVariablesProvider {

    private static final Logger LOG = Logger.getInstance(SmaliFrameArgumentsProvider.class);

    @Override
    public boolean isAvailable(@NotNull SourcePosition sourcePosition, @NotNull EvaluationContext evalContext) {
        return sourcePosition.getFile() instanceof SmaliFile && evalContext.getDebugProcess().isAttached();
    }

    @NotNull
    @Override
    public Set<TextWithImports> collectVariables(@NotNull SourcePosition sourcePosition,
                                                 @NotNull EvaluationContext evalContext,
                                                 @NotNull Set<String> alreadyCollected) {
        try {
            StackFrameProxy frameProxy = evalContext.getFrameProxy();
            if (frameProxy != null) {
                List<Value> arguments = DebuggerUtilsEx.getArgumentValues(frameProxy.getStackFrame());
                List<String> types = frameProxy.location().method().argumentTypeNames();

                //当前的方法是否为静态方法
                boolean isStatic = evalContext.computeThisObject() == null;
                int registerIndex = isStatic ? 0 : 1;

                Set<TextWithImports> results = new HashSet<>();

                for (int i = 0; i < types.size(); i++) {
                    String type = NameUtils.javaToSmaliType(types.get(i));
                    int registerCount = typeRegisterCount(type);

                    Value parameter = arguments.get(i);
                    boolean appendToString = shouldToString(type);

                    String variable = "p" + (registerIndex);
                    if (appendToString && parameter != null) {
                        variable = variable + " == null ? null : " + variable + ".toString()";
                    }

                    TextWithImportsImpl textWithImport = new TextWithImportsImpl(CodeFragmentKind.CODE_BLOCK, variable);
                    results.add(textWithImport);

                    registerIndex += registerCount;
                }

                return results;
            }
        } catch (Exception e) {
            LOG.error(e);
        }

        return Collections.emptySet();
    }

    /**
     * 判断给定的类型是否需要进行String操作
     * 如果是包装类型和字符串的话可以直接进行值展示
     *
     * @param smaliArgumentType 需要判断的类型
     * @return true 或者 false
     */
    private static boolean shouldToString(String smaliArgumentType) {
        switch (smaliArgumentType) {
            case "Ljava/lang/String;":
            case "Ljava/lang/Byte;":
            case "Ljava/lang/Short;":
            case "Ljava/lang/Char;":
            case "Ljava/lang/Integer;":
            case "Ljava/lang/Long;":
            case "Ljava/lang/Float;":
            case "Ljava/lang/Double;":
            case "Ljava/lang/Number;":
                return true;
        }

        return false;
    }

    /**
     * 判断给定的数据类型需要的寄存器数量
     *
     * @param smaliType 需要判断的数据类型
     * @return 所需寄存器数量
     */
    private static int typeRegisterCount(String smaliType) {
        if (NameUtils.isPrimitiveType(smaliType)) {
            switch (smaliType.charAt(0)) {
                case 'D':
                case 'J':
                    return 2/*double 和long 需要两个寄存器*/;
            }
        }
        return 1;
    }
}
