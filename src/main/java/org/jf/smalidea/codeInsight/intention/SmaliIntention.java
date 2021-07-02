package org.jf.smalidea.codeInsight.intention;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.siyeh.ipp.base.Intention;
import com.siyeh.ipp.base.PsiElementEditorPredicate;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.SmaliLanguage;

public abstract class SmaliIntention extends Intention {


    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        return findMatchingElement(editor, element) != null;
    }


    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        final PsiElement matchingElement = findMatchingElement(editor, element);
        if (matchingElement == null) {
            return;
        }
        processIntention(editor, matchingElement);
    }


    protected PsiElement findMatchingElement(Editor editor, @NotNull PsiElement element) {
        while (element != null) {
            if (!SmaliLanguage.INSTANCE.equals(element.getLanguage())) {
                break;
            }
            if (getElementPredicate() instanceof PsiElementEditorPredicate) {
                if (((PsiElementEditorPredicate) getElementPredicate()).satisfiedBy(element, editor)) {
                    return element;
                }
            } else if (getElementPredicate().satisfiedBy(element)) {
                return element;
            }
            element = element.getParent();
            if (element instanceof PsiFile) {
                break;
            }
        }
        return null;
    }

}
