package org.jf.smalidea.debugging;

import com.intellij.debugger.SourcePosition;
import com.intellij.debugger.engine.FrameExtraVariablesProvider;
import com.intellij.debugger.engine.evaluation.CodeFragmentKind;
import com.intellij.debugger.engine.evaluation.EvaluationContext;
import com.intellij.debugger.engine.evaluation.TextWithImports;
import com.intellij.debugger.engine.evaluation.TextWithImportsImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.psi.impl.SmaliRegisterReference;
import org.jf.smalidea.util.PsiUtil;
import org.jf.smalidea.util.Ref;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * 方法局部变量提供器
 */
public class SmaliFrameLocalVariableProvider implements FrameExtraVariablesProvider {

    @Override
    public boolean isAvailable(@NotNull SourcePosition sourcePosition, @NotNull EvaluationContext evalContext) {
        return PsiUtil.isSmaliFile(sourcePosition.getFile()) && evalContext.getDebugProcess().isAttached();
    }

    @NotNull
    @Override
    public Set<TextWithImports> collectVariables(@NotNull SourcePosition sourcePosition,
                                                 @NotNull EvaluationContext evalContext,
                                                 @NotNull Set<String> alreadyCollected) {

        Set<TextWithImports> registers = new TreeSet<>(Comparator.comparingInt(o -> Integer.parseInt(o.getText().substring(1))));

        final Ref<Boolean> isFound = new Ref<>(false);

        ApplicationManager.getApplication().runReadAction(new Runnable() {
            @Override
            public void run() {
                sourcePosition.getElementAt().getParent().accept(new PsiRecursiveElementWalkingVisitor() {
                    @Override
                    public void visitElement(PsiElement element) {
                        super.visitElement(element);

                        if (element == sourcePosition.getElementAt()) {
                            isFound.set(true);
                        }
                        
                        if (!isFound.get()) {
                            if (element instanceof SmaliRegisterReference) {
                                String text = element.getText();
                                if (text.startsWith("v")) {
                                    registers.add(new TextWithImportsImpl(CodeFragmentKind.CODE_BLOCK, text));
                                }
                            }
                        }
                    }
                });
            }
        });

        return registers;
    }
}
