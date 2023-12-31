package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.ExpectedTypeInfo;
import com.intellij.codeInsight.ExpectedTypesProvider;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.java.JavaBundle;
import com.intellij.lang.LangBundle;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.impl.CustomSyntaxTableFileType;
import com.intellij.openapi.keymap.KeymapUtil;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiJavaElementPattern;
import com.intellij.psi.*;
import com.intellij.psi.filters.ClassFilter;
import com.intellij.psi.filters.ElementFilter;
import com.intellij.psi.filters.TrueFilter;
import com.intellij.psi.filters.element.ExcludeDeclaredFilter;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.DirectClassInheritorsSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.Consumer;
import com.intellij.util.ObjectUtils;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.JBIterable;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.intellij.patterns.PsiJavaPatterns.psiClass;
import static com.intellij.patterns.PsiJavaPatterns.psiElement;


public class SmaliClassNameCompletionContributor extends CompletionContributor {

    public static final PsiJavaElementPattern.Capture<PsiElement> AFTER_NEW = psiElement().afterLeaf(PsiKeyword.NEW);

    private static final PsiJavaElementPattern.Capture<PsiElement> IN_TYPE_PARAMETER =
            psiElement().afterLeaf(PsiKeyword.EXTENDS, PsiKeyword.SUPER, "&").withParent(
                    psiElement(PsiReferenceList.class).withParent(PsiTypeParameter.class));

    private static final ElementPattern<PsiElement> IN_EXTENDS_IMPLEMENTS =
            psiElement().inside(psiElement(PsiReferenceList.class).withParent(psiClass()));

    public SmaliClassNameCompletionContributor() {

    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull final CompletionResultSet _result) {

        if (parameters.getCompletionType() == CompletionType.CLASS_NAME ||
                parameters.isExtendedCompletion() && mayContainClassName(parameters)) {
            addAllClasses(parameters, _result);
        }
    }

    static void addAllClasses(CompletionParameters parameters, final CompletionResultSet _result) {
        CompletionResultSet result = _result.withPrefixMatcher(CompletionUtil.findReferenceOrAlphanumericPrefix(parameters));
        addAllClasses(parameters, parameters.getInvocationCount() <= 1, result.getPrefixMatcher(), _result);
    }

    private static boolean mayContainClassName(CompletionParameters parameters) {
        PsiElement position = parameters.getPosition();
        PsiFile file = position.getContainingFile();
        if (file instanceof PsiPlainTextFile || file.getFileType() instanceof CustomSyntaxTableFileType) {
            return true;
        }
        if (SkipAutopopupInStrings.isInStringLiteral(position)) {
            return true;
        }
        PsiComment comment = PsiTreeUtil.getParentOfType(position, PsiComment.class, false);
        if (comment != null && !(comment instanceof PsiDocComment)) {
            return true;
        }
        return false;
    }

    public static void addAllClasses(@NotNull CompletionParameters parameters,
                                     final boolean filterByScope,
                                     @NotNull final PrefixMatcher matcher,
                                     @NotNull final Consumer<LookupElement> consumer) {
        final PsiElement insertedElement = parameters.getPosition();


        final ElementFilter filter =
                IN_EXTENDS_IMPLEMENTS.accepts(insertedElement) ? new ExcludeDeclaredFilter(new ClassFilter(PsiClass.class)) :
                        IN_TYPE_PARAMETER.accepts(insertedElement) ? new ExcludeDeclaredFilter(new ClassFilter(PsiTypeParameter.class)) :
                                TrueFilter.INSTANCE;

        final boolean inJavaContext = parameters.getPosition() instanceof PsiIdentifier;
        final boolean afterNew = AFTER_NEW.accepts(insertedElement);
        if (afterNew) {
            final PsiExpression expr = PsiTreeUtil.getContextOfType(insertedElement, PsiExpression.class, true);
            for (final ExpectedTypeInfo info : ExpectedTypesProvider.getExpectedTypes(expr, true)) {
                final PsiType type = info.getType();
                final PsiClass psiClass = PsiUtil.resolveClassInType(type);
                if (psiClass != null && psiClass.getName() != null) {
                    consumer.consume(createClassLookupItem(psiClass, inJavaContext));
                }
                final PsiType defaultType = info.getDefaultType();
                if (!defaultType.equals(type)) {
                    final PsiClass defClass = PsiUtil.resolveClassInType(defaultType);
                    if (defClass != null && defClass.getName() != null) {
                        consumer.consume(createClassLookupItem(defClass, true));
                    }
                }
            }
        }

        final boolean pkgContext = SmaliCompletionUtil.inSomePackage(insertedElement);
        AllClassesGetter.processJavaClasses(parameters, matcher, filterByScope, new Consumer<PsiClass>() {
            @Override
            public void consume(PsiClass psiClass) {
                processClass(psiClass, null, "");
            }

            private void processClass(PsiClass psiClass, @Nullable Set<PsiClass> visited, String prefix) {
                boolean isInnerClass = StringUtil.isNotEmpty(prefix);
                if (isInnerClass && isProcessedIndependently(psiClass)) {
                    return;
                }

                if (filter.isAcceptable(psiClass, insertedElement)) {
                    if (!inJavaContext) {
                        JavaPsiClassReferenceElement element = createLookupItem(psiClass, SmaliClassNameInsertHandler.SMALI_CLASS_INSERT_HANDLER);
                        element.setLookupString(prefix + element.getLookupString());
                        consumer.consume(element);
                    } else {
                        Condition<PsiClass> condition = eachClass ->
                                filter.isAcceptable(eachClass, insertedElement) &&
                                        AllClassesGetter.isAcceptableInContext(insertedElement, eachClass, filterByScope, pkgContext);
                        for (SmaliPsiClassReferenceElement element : createClassLookupItems(psiClass, afterNew, SmaliClassNameInsertHandler.SMALI_CLASS_INSERT_HANDLER, condition)) {
                            element.setLookupString(prefix + element.getLookupString());
                            JavaConstructorCallElement.wrap(element, insertedElement).forEach(consumer::consume);
                        }
                    }
                } else {
                    String name = psiClass.getName();
                    if (name != null) {
                        PsiClass[] innerClasses = psiClass.getInnerClasses();
                        if (innerClasses.length > 0) {
                            if (visited == null) visited = ContainerUtil.newHashSet();

                            for (PsiClass innerClass : innerClasses) {
                                if (visited.add(innerClass)) {
                                    processClass(innerClass, visited, prefix + name + ".");
                                }
                            }
                        }
                    }
                }
            }

            private boolean isProcessedIndependently(PsiClass psiClass) {
                String innerName = psiClass.getName();
                return innerName != null && matcher.prefixMatches(innerName);
            }
        });
    }

    @NotNull
    private static MultiMap<String, PsiClass> getAllAnnotationClasses(PsiElement context, PrefixMatcher matcher) {
        MultiMap<String, PsiClass> map = new MultiMap<>();
        GlobalSearchScope scope = context.getResolveScope();
        PsiClass annotation = JavaPsiFacade.getInstance(context.getProject()).findClass(CommonClassNames.JAVA_LANG_ANNOTATION_ANNOTATION, scope);
        if (annotation != null) {
            DirectClassInheritorsSearch.search(annotation, scope, false).forEach(psiClass -> {
                if (!psiClass.isAnnotationType() || psiClass.getQualifiedName() == null) return true;

                String name = ObjectUtils.assertNotNull(psiClass.getName());
                if (!matcher.prefixMatches(name)) {
                    name = getClassNameWithContainers(psiClass);
                    if (!matcher.prefixMatches(name)) return true;
                }
                map.putValue(name, psiClass);
                return true;
            });
        }
        return map;
    }

    @NotNull
    private static String getClassNameWithContainers(@NotNull PsiClass psiClass) {
        String name = ObjectUtils.assertNotNull(psiClass.getName());
        for (PsiClass parent : JBIterable.generate(psiClass, PsiClass::getContainingClass)) {
            name = parent.getName() + "." + name;
        }
        return name;
    }

    static LookupElement highlightIfNeeded(SmaliPsiClassReferenceElement element, CompletionParameters parameters) {
        return SmaliCompletionUtil.highlightIfNeeded(null, element, element.getObject(), parameters.getPosition());
    }

    public static SmaliPsiClassReferenceElement createClassLookupItem(final PsiClass psiClass, final boolean inJavaContext) {
        return createLookupItem(psiClass, /*inJavaContext ?*/ SmaliClassNameInsertHandler.SMALI_CLASS_INSERT_HANDLER
                /*: AllClassesGetter.TRY_SHORTENING*/);
    }

    public static List<SmaliPsiClassReferenceElement> createClassLookupItems(final PsiClass psiClass,
                                                                             boolean withInners,
                                                                             InsertHandler<SmaliPsiClassReferenceElement> insertHandler,
                                                                             Condition<PsiClass> condition) {
        List<SmaliPsiClassReferenceElement> result = new SmartList<>();
        if (condition.value(psiClass)) {
            result.add(createLookupItem(psiClass, insertHandler));
        }
        String name = psiClass.getName();
        if (withInners && name != null) {
            for (PsiClass inner : psiClass.getInnerClasses()) {
                if (inner.hasModifierProperty(PsiModifier.STATIC)) {
                    for (SmaliPsiClassReferenceElement lookupInner : createClassLookupItems(inner, true, insertHandler, condition)) {
                        String forced = lookupInner.getForcedPresentableName();
                        String qualifiedName = name + "." + (forced != null ? forced : inner.getName());
                        lookupInner.setForcedPresentableName(qualifiedName);
                        lookupInner.setLookupString(qualifiedName);
                        result.add(lookupInner);
                    }
                }
            }
        }
        return result;
    }


    @Override
    public String handleEmptyLookup(@NotNull final CompletionParameters parameters, final Editor editor) {
        if (!(parameters.getOriginalFile() instanceof PsiJavaFile)) return null;

        if (shouldShowSecondSmartCompletionHint(parameters)) {
            return LangBundle.message("completion.no.suggestions") +
                    "; " +
                    StringUtil.decapitalize(
                            JavaBundle.message("completion.class.name.hint.2", KeymapUtil.getFirstKeyboardShortcutText(IdeActions.ACTION_CODE_COMPLETION)));
        }

        return null;
    }

    private static boolean shouldShowSecondSmartCompletionHint(final CompletionParameters parameters) {
        return parameters.getCompletionType() == CompletionType.BASIC &&
                parameters.getInvocationCount() == 2 &&
                parameters.getOriginalFile().getLanguage().isKindOf(JavaLanguage.INSTANCE);
    }

    public static SmaliPsiClassReferenceElement createLookupItem(@NotNull final PsiClass psiClass,
                                                                final InsertHandler<SmaliPsiClassReferenceElement> insertHandler) {
        final SmaliPsiClassReferenceElement item = new SmaliPsiClassReferenceElement(psiClass);
        item.setInsertHandler(insertHandler);
        return item;
    }
}
