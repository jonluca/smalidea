package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.codeInsight.lookup.LookupElementPresentation;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

class SuperCalls {



    @NotNull
    private static LookupElement withQualifiedSuper(final String className, LookupElement item) {
        return PrioritizedLookupElement.withExplicitProximity(new LookupElementDecorator<LookupElement>(item) {

            @Override
            public void renderElement(LookupElementPresentation presentation) {
                super.renderElement(presentation);
                presentation.setItemText(className + ".super." + presentation.getItemText());
            }

            @Override
            public void handleInsert(InsertionContext context) {
                context.commitDocument();
                PsiJavaCodeReferenceElement ref = PsiTreeUtil
                        .findElementOfClassAtOffset(context.getFile(), context.getStartOffset(), PsiJavaCodeReferenceElement.class, false);
                if (ref != null) {
                    context.getDocument().insertString(ref.getTextRange().getStartOffset(), className + ".");
                }

                super.handleInsert(context);
            }
        }, -1);
    }

    private static Set<String> getContainingClassNames(PsiElement position) {
        Set<String> result = ContainerUtil.newLinkedHashSet();
        boolean add = false;
        while (position != null) {
            if (position instanceof PsiAnonymousClass) {
                add = true;
            } else if (add && position instanceof PsiClass) {
                ContainerUtil.addIfNotNull(result, ((PsiClass) position).getName());
            }
            position = position.getParent();
        }
        return result;
    }
}
