package org.jf.smalidea.codeInsight;

import com.intellij.codeInsight.TargetElementEvaluatorEx2;
import com.intellij.codeInsight.TargetElementUtilExtender;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by lovely3x on 2017/4/27.
 */
public class SmaliTargetElementEvaluator extends TargetElementEvaluatorEx2 implements TargetElementUtilExtender {

    @Nullable
    @Override
    public PsiElement adjustTargetElement(Editor editor, int offset, int flags, @NotNull PsiElement targetElement) {
        return super.adjustTargetElement(editor, offset, flags, targetElement);
    }

    @Nullable
    @Override
    public PsiElement getElementByReference(@NotNull PsiReference ref, int flags) {
        return null;
    }

    @Override
    public int getAllAdditionalFlags() {
        return 0;
    }

    @Override
    public int getAdditionalDefinitionSearchFlags() {
        return 0;
    }

    @Override
    public int getAdditionalReferenceSearchFlags() {
        return 0;
    }
}
