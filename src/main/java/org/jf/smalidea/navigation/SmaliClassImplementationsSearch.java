package org.jf.smalidea.navigation;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFunctionalExpression;
import com.intellij.psi.search.PsiElementProcessorAdapter;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.search.searches.FunctionalExpressionSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class SmaliClassImplementationsSearch extends QueryExecutorBase<PsiElement, DefinitionsScopedSearch.SearchParameters> {

    @Override
    public void processQuery(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        PsiElement element = queryParameters.getElement();
        if (element instanceof PsiClass) {
            SearchScope scope = queryParameters.getScope();
            processImplementations((PsiClass) element, consumer, scope);
        }
    }

    public static boolean processImplementations(final PsiClass psiClass, final Processor<? super PsiElement> processor, SearchScope scope) {
        final boolean showInterfaces = Registry.is("ide.goto.implementation.show.interfaces");
        if (!ClassInheritorsSearch.search(psiClass, scope, true).forEach(new PsiElementProcessorAdapter<>(element -> {
            if (!showInterfaces && element.isInterface()) {
                return true;
            }
            return processor.process(element);
        }))) {
            return false;
        }

        return FunctionalExpressionSearch.search(psiClass, scope).forEach((Processor<PsiFunctionalExpression>) processor::process);
    }



}
