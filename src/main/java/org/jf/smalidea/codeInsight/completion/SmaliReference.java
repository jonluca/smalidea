package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliIcons;

import java.util.ArrayList;
import java.util.List;

public class SmaliReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private String key;

    public SmaliReference(@NotNull PsiElement element, TextRange textRange) {
        super(element, textRange);
        key = element.getText().substring(textRange.getStartOffset(), textRange.getEndOffset());
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Project project = myElement.getProject();
        final List<PsiElement> elements = SimpleUtil.findProperties(project/*, key*/);
        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (PsiElement property : elements) {
            results.add(new PsiElementResolveResult(property));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        ResolveResult[] resolveResults = multiResolve(false);
        return resolveResults.length == 1 ? resolveResults[0].getElement() : null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Project project = myElement.getProject();
        List<PsiElement> elements = SimpleUtil.findProperties(project);
        List<LookupElement> variants = new ArrayList<LookupElement>();
        for (final PsiElement property : elements) {
            if (property != null) {
                variants.add(LookupElementBuilder.create(property).
                        withIcon(SmaliIcons.SmaliIcon).withTypeText(property.getContainingFile().getName())
                );
            }
        }
        return variants.toArray();
    }
}