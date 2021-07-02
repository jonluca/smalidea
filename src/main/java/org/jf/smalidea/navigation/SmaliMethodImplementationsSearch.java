package org.jf.smalidea.navigation;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFunctionalExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.searches.DefinitionsScopedSearch;
import com.intellij.psi.search.searches.FunctionalExpressionSearch;
import com.intellij.psi.search.searches.OverridingMethodsSearch;
import com.intellij.util.CommonProcessors;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SmaliMethodImplementationsSearch implements QueryExecutor<PsiElement, DefinitionsScopedSearch.SearchParameters> {

    public static boolean processImplementations(final PsiMethod psiMethod, final Processor<? super PsiElement> consumer,
                                                 final SearchScope searchScope) {
        return processOverridingMethods(psiMethod, searchScope, consumer::process) &&
                FunctionalExpressionSearch.search(psiMethod, searchScope).forEach((Processor<PsiFunctionalExpression>)consumer::process);
    }

    public static void getOverridingMethods(PsiMethod method, List<PsiMethod> list, SearchScope scope) {
        processOverridingMethods(method, scope, new CommonProcessors.CollectProcessor<>(list));
    }

    private static boolean processOverridingMethods(PsiMethod method, SearchScope scope, Processor<PsiMethod> processor) {
        return OverridingMethodsSearch.search(method, scope, true).forEach(processor);
    }

    @Deprecated
    public static PsiMethod[] getMethodImplementations(final PsiMethod method, SearchScope scope) {
        List<PsiMethod> result = new ArrayList<>();
        processOverridingMethods(method, scope, new CommonProcessors.CollectProcessor<>(result));
        return result.toArray(new PsiMethod[result.size()]);
    }

    @Override
    public boolean execute(@NotNull DefinitionsScopedSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiElement> consumer) {
        final PsiElement sourceElement = queryParameters.getElement();
        if (sourceElement instanceof PsiMethod) {
            return processImplementations((PsiMethod)sourceElement, consumer, queryParameters.getScope());
        }
        return true;
    }
}
