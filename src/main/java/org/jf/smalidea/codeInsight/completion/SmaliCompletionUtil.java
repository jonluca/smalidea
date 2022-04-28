package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.CodeInsightSettings;
import com.intellij.codeInsight.TailType;
import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.codeInsight.completion.util.ParenthesesInsertHandler;
import com.intellij.codeInsight.daemon.impl.analysis.LambdaHighlightingUtil;
import com.intellij.codeInsight.lookup.*;
import com.intellij.lang.StdLanguages;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import com.intellij.psi.impl.source.PostprocessReformattingAspect;
import com.intellij.psi.util.ClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.psi.util.proximity.ReferenceListWeigher;
import com.intellij.ui.JBColor;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliLanguage;
import org.jf.smalidea.psi.SmaliPsiKeyword;
import org.jf.smalidea.psi.impl.SmaliMethod;
import org.jf.smalidea.psi.leaf.SmaliClassDescriptor;
import org.jf.smalidea.util.NameUtils;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.intellij.codeInsight.completion.ReferenceExpressionCompletionContributor.findConstantsUsedInSwitch;
import static com.intellij.patterns.PlatformPatterns.psiElement;
import static com.intellij.psi.util.proximity.ReferenceListWeigher.ReferenceListApplicability.inapplicable;

public class SmaliCompletionUtil {


    public static void processSmaliReference(CompletionParameters parameters, CompletionResultSet result, String acceptPrefix) {

        PsiElement pos = parameters.getPosition();

        //获取到类名
        PsiElement psiElement = pos.getContainingFile().findElementAt((parameters.getOffset() - (acceptPrefix.length() + result.getPrefixMatcher().getPrefix().length() + 1)));
        if (!(psiElement instanceof SmaliClassDescriptor)) return;

        PsiClass psiClass = ClassUtil.findPsiClass(PsiManager.getInstance(pos.getProject()), NameUtils.smaliToJavaType(psiElement.getText()));

        if (psiClass == null) return;

        completionFields(result, psiClass);

        completionMethods(result, psiClass);

    }

    public static void completionFields(CompletionResultSet result, PsiClass psiClass) {
        PsiField[] allFields = psiClass.getAllFields();
        PsiField[] fields = psiClass.getFields();
        final AtomicBoolean isSuperMethod = new AtomicBoolean(false);

        Set<PsiField> processedMethods = new HashSet<>();

        Consumer<PsiField> consumer = field -> {
            if (processedMethods.contains(field)) return;

            if (field instanceof SmaliMethod) return;

            processedMethods.add(field);

            final boolean isSuper = isSuperMethod.get();

            LookupElementBuilder element = LookupElementBuilder.create(field);
            if (!isSuper) {
                element = element.bold();
            }
            result.addElement(LookupElementDecorator.withInsertHandler(PrioritizedLookupElement.withExplicitProximity(isSuper ? element : highlight(element), isSuper ? 0 : 1), SmaliFieldCallInsertHandler.INSTANCE));
        };

        consume(allFields, consumer);
        isSuperMethod.set(true);
        consume(fields, consumer);

    }

    public static void completionMethods(CompletionResultSet result, PsiClass psiClass) {
        PsiMethod[] methods = psiClass.getMethods();
        PsiMethod[] allMethods = psiClass.getAllMethods();

        Set<PsiMethod> processedMethods = new java.util.HashSet<>();

        final AtomicBoolean isSuperMethod = new AtomicBoolean(false);

        Consumer<PsiMethod> consumer = method -> {
            if (processedMethods.contains(method)) return;

            if (method instanceof SmaliMethod && ((SmaliMethod) method).isStaticConstructor()) return;

            processedMethods.add(method);

            JavaMethodCallElement item = new JavaMethodCallElement(method);

            final boolean isSuper = isSuperMethod.get();
            result.addElement(LookupElementDecorator.withInsertHandler(
                    PrioritizedLookupElement.withExplicitProximity(isSuper ? item : highlight(item), isSuper ? 0 : 1),
                    SmaliMethodCallInsertHandler.INSTANCE));
        };


        consume(methods, consumer);
        isSuperMethod.set(true);
        consume(allMethods, consumer);
    }

    private static <T> void consume(T[] array, Consumer<T> consumer) {
        for (T t : array) consumer.consume(t);
    }


    private static boolean shouldMarkRed(@NotNull Object object, @NotNull PsiElement place) {
        if (!(object instanceof PsiMember)) return false;
//        if (Java15APIUsageInspectionBase.isForbiddenApiUsage((PsiMember) object, PsiUtil.getLanguageLevel(place)))
//            return true;

        if (object instanceof PsiEnumConstant) {
            return findConstantsUsedInSwitch(place).contains(CompletionUtil.getOriginalOrSelf((PsiEnumConstant) object));
        }
        if (object instanceof PsiClass && ReferenceListWeigher.INSTANCE.getApplicability((PsiClass) object, place) == inapplicable) {
            return true;
        }
        return false;
    }


    @NotNull
    public static LookupElement highlightIfNeeded(@Nullable PsiType qualifierType,
                                                  @NotNull LookupElement item,
                                                  @NotNull Object object,
                                                  @NotNull PsiElement place) {
        if (shouldMarkRed(object, place)) {
            return PrioritizedLookupElement.withExplicitProximity(LookupElementDecorator.withRenderer(item, new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                @Override
                public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                    element.getDelegate().renderElement(presentation);
                    presentation.setItemTextForeground(JBColor.RED);
                }
            }), -1);
        }
        if (containsMember(qualifierType, object)) {
            LookupElementRenderer<LookupElementDecorator<LookupElement>> boldRenderer =
                    new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                        @Override
                        public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                            element.getDelegate().renderElement(presentation);
                            presentation.setItemTextBold(true);
                        }
                    };
            return PrioritizedLookupElement.withExplicitProximity(LookupElementDecorator.withRenderer(item, boldRenderer), 1);
        }
        return item;
    }


    public static LookupElement highlight(@NotNull LookupElement item) {
        LookupElementRenderer<LookupElementDecorator<LookupElement>> boldRenderer =
                new LookupElementRenderer<LookupElementDecorator<LookupElement>>() {
                    @Override
                    public void renderElement(LookupElementDecorator<LookupElement> element, LookupElementPresentation presentation) {
                        element.getDelegate().renderElement(presentation);
                        presentation.setItemTextBold(true);
                    }
                };
        return PrioritizedLookupElement.withExplicitProximity(LookupElementDecorator.withRenderer(item, boldRenderer), 1);
    }


    public static boolean containsMember(@Nullable PsiType qualifierType, @NotNull Object object) {
        if (qualifierType instanceof PsiArrayType && object instanceof PsiMember) { //length and clone()
            PsiFile file = ((PsiMember) object).getContainingFile();
            if (file == null || file.getVirtualFile() == null) { //yes, they're a bit dummy
                return true;
            }
        } else if (qualifierType instanceof PsiClassType) {
            PsiClass qualifierClass = ((PsiClassType) qualifierType).resolve();
            if (qualifierClass == null) return false;
            if (object instanceof PsiMethod && qualifierClass.findMethodBySignature((PsiMethod) object, false) != null) {
                return true;
            }
            if (object instanceof PsiMember) {
                return qualifierClass.equals(((PsiMember) object).getContainingClass());
            }
        }
        return false;
    }


    public static boolean inSomePackage(PsiElement context) {
        PsiFile contextFile = context.getContainingFile();
        return contextFile instanceof PsiClassOwner && StringUtil.isNotEmpty(((PsiClassOwner) contextFile).getPackageName());
    }

    public static void insertParentheses(final InsertionContext context,
                                         final LookupElement item,
                                         boolean overloadsMatter,
                                         boolean hasParams) {
        insertParentheses(context, item, overloadsMatter, hasParams, false);
    }

    public static void insertParentheses(final InsertionContext context,
                                         final LookupElement item,
                                         boolean overloadsMatter,
                                         boolean hasParams,
                                         final boolean forceClosingParenthesis) {
        final Editor editor = context.getEditor();
        final char completionChar = context.getCompletionChar();
        final PsiFile file = context.getFile();

        final TailType tailType = completionChar == '(' ? TailType.NONE :
                completionChar == ':' ? TailType.COND_EXPR_COLON :
                        LookupItem.handleCompletionChar(context.getEditor(), item, completionChar);
        final boolean hasTail = tailType != TailType.NONE && tailType != TailType.UNKNOWN;
        final boolean smart = completionChar == Lookup.COMPLETE_STATEMENT_SELECT_CHAR;

        if (completionChar == '(' || completionChar == '.' || completionChar == ',' || completionChar == ';' || completionChar == ':' || completionChar == ' ') {
            context.setAddCompletionChar(false);
        }

        if (hasTail) {
            hasParams = false;
        }
        final boolean needRightParenth = forceClosingParenthesis ||
                !smart && (CodeInsightSettings.getInstance().AUTOINSERT_PAIR_BRACKET ||
                        !hasParams && completionChar != '(');

        context.commitDocument();

        final CommonCodeStyleSettings styleSettings = CodeStyleSettings.getDefaults().getCommonSettings(SmaliLanguage.INSTANCE);
        final PsiElement elementAt = file.findElementAt(context.getStartOffset());
        if (elementAt == null || !(elementAt.getParent() instanceof PsiMethodReferenceExpression)) {
            final boolean hasParameters = hasParams;
            final boolean spaceBetweenParentheses = styleSettings.SPACE_WITHIN_METHOD_CALL_PARENTHESES && hasParams;
            new ParenthesesInsertHandler<LookupElement>(styleSettings.SPACE_BEFORE_METHOD_CALL_PARENTHESES, spaceBetweenParentheses,
                    needRightParenth, styleSettings.METHOD_PARAMETERS_LPAREN_ON_NEXT_LINE) {
                @Override
                protected boolean placeCaretInsideParentheses(InsertionContext context1, LookupElement item1) {
                    return hasParameters;
                }

                @Override
                protected PsiElement findExistingLeftParenthesis(@NotNull InsertionContext context) {
                    PsiElement token = super.findExistingLeftParenthesis(context);
                    return isPartOfLambda(token) ? null : token;
                }

                private boolean isPartOfLambda(PsiElement token) {
                    return token != null && token.getParent() instanceof PsiExpressionList &&
                            PsiUtilCore.getElementType(PsiTreeUtil.nextVisibleLeaf(token.getParent())) == JavaTokenType.ARROW;
                }
            }.handleInsert(context, item);
        }

        if (hasParams) {
            // Invoke parameters popup
            AutoPopupController.getInstance(file.getProject()).autoPopupParameterInfo(editor, overloadsMatter ? null : (PsiElement) item.getObject());
        }

        if (smart || !needRightParenth || !insertTail(context, item, tailType, hasTail)) {
            return;
        }

        if (completionChar == '.') {
            AutoPopupController.getInstance(file.getProject()).autoPopupMemberLookup(context.getEditor(), null);
        } else if (completionChar == ',') {
            AutoPopupController.getInstance(file.getProject()).autoPopupParameterInfo(context.getEditor(), null);
        }
    }

    public static boolean insertTail(InsertionContext context, LookupElement item, TailType tailType, boolean hasTail) {
        TailType toInsert = tailType;
        LookupItem<?> lookupItem = item.as(LookupItem.CLASS_CONDITION_KEY);
        if (lookupItem == null || lookupItem.getAttribute(LookupItem.TAIL_TYPE_ATTR) != TailType.UNKNOWN) {
            if (!hasTail && item.getObject() instanceof PsiMethod && PsiType.VOID.equals(((PsiMethod) item.getObject()).getReturnType())) {
                PsiDocumentManager.getInstance(context.getProject()).commitAllDocuments();
                if (psiElement().beforeLeaf(psiElement().withText(".")).accepts(context.getFile().findElementAt(context.getTailOffset() - 1))) {
                    return false;
                }

                boolean insertAdditionalSemicolon = true;
                PsiElement leaf = context.getFile().findElementAt(context.getStartOffset());
                PsiElement composite = leaf == null ? null : leaf.getParent();
                if (composite instanceof PsiMethodReferenceExpression && LambdaHighlightingUtil.insertSemicolon(composite.getParent())) {
                    insertAdditionalSemicolon = false;
                } else if (composite instanceof PsiReferenceExpression) {
                    PsiElement parent = composite.getParent();
                    if (parent instanceof PsiMethodCallExpression) {
                        parent = parent.getParent();
                    }
                    if (parent instanceof PsiLambdaExpression && !LambdaHighlightingUtil.insertSemicolonAfter((PsiLambdaExpression) parent)) {
                        insertAdditionalSemicolon = false;
                    }
                    if (parent instanceof PsiMethodReferenceExpression && LambdaHighlightingUtil.insertSemicolon(parent.getParent())) {
                        insertAdditionalSemicolon = false;
                    }
                }
                if (insertAdditionalSemicolon) {
                    toInsert = TailType.SEMICOLON;
                }

            }
        }
        toInsert.processTail(context.getEditor(), context.getTailOffset());
        return true;
    }


    public static boolean promptTypeArgs(InsertionContext context, int offset) {
        if (offset < 0) {
            return false;
        }

        OffsetKey key = context.trackOffset(offset, false);
        PostprocessReformattingAspect.getInstance(context.getProject()).doPostponedFormatting();
        offset = context.getOffset(key);
        if (offset < 0) {
            return false;
        }

        String open = escapeXmlIfNeeded(context, "<");
        context.getDocument().insertString(offset, open);
        context.getEditor().getCaretModel().moveToOffset(offset + open.length());
        context.getDocument().insertString(offset + open.length(), escapeXmlIfNeeded(context, ">"));
        if (context.getCompletionChar() != Lookup.COMPLETE_STATEMENT_SELECT_CHAR) {
            context.setAddCompletionChar(false);
        }
        return true;
    }

    public static String escapeXmlIfNeeded(InsertionContext context, String generics) {
        if (context.getFile().getViewProvider().getBaseLanguage() == XMLLanguage.INSTANCE) {
            return StringUtil.escapeXmlEntities(generics);
        }
        return generics;
    }

    public static void processSmaliRegisterDot(CompletionParameters parameters, CompletionResultSet withPrefixMatcher, String dot) {
        PsiElement register = parameters.getOriginalPosition().getPrevSibling();
        PsiClass type = Infer.infer(register);
        if (type != null) {
            completionMethods(withPrefixMatcher, type);
        }
    }


    public static class SmaliMethodCallInsertHandler implements InsertHandler<LookupElement> {

        public static final SmaliMethodCallInsertHandler INSTANCE = new SmaliMethodCallInsertHandler();

        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
            context.commitDocument();

            if (item.getObject() instanceof PsiMethod) {
                Document document = context.getDocument();
                PsiMethod psiMethod = (PsiMethod) item.getObject();
                PsiParameterList parametersList = psiMethod.getParameterList();
                StringBuffer parametersStr = new StringBuffer();

                for (int i = 0; i < parametersList.getParameters().length; i++) {
                    PsiParameter parameter = parametersList.getParameters()[i];
                    String type = parameter.getType().getCanonicalText();
                    parametersStr.append(NameUtils.isVoidJavaType(type) ? SmaliPsiKeyword.VOID :
                            NameUtils.javaToSmaliType(type));
                }

                String methodName = psiMethod.isConstructor() ? SmaliPsiKeyword.CONSTRUCTOR : psiMethod.getName();

                String methodreturnJavaType = psiMethod.getReturnType() == null ? PsiKeyword.VOID : psiMethod.getReturnType().getCanonicalText();

                String methodReturnType = NameUtils.isVoidJavaType(methodreturnJavaType) ? SmaliPsiKeyword.VOID :
                        NameUtils.javaToSmaliType(methodreturnJavaType);

                String insertStr = String.format(Locale.US, "%s(%s)%s", methodName, parametersStr, methodReturnType);
                document.replaceString(context.getStartOffset(), context.getTailOffset(), insertStr);

                final Editor editor = context.getEditor();
                editor.getCaretModel().moveToOffset(context.getSelectionEndOffset());
                editor.getSelectionModel().removeSelection();
            }
        }
    }

    public static class SmaliFieldCallInsertHandler implements InsertHandler<LookupElement> {

        public static final SmaliFieldCallInsertHandler INSTANCE = new SmaliFieldCallInsertHandler();

        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
            context.commitDocument();

            if (item.getObject() instanceof PsiField) {
                Document document = context.getDocument();
                PsiField psiField = (PsiField) item.getObject();


                String fieldName = psiField.getName();

                String fieldTypeStr = psiField.getType().getCanonicalText();

                String fieldType = NameUtils.isVoidJavaType(fieldTypeStr) ? SmaliPsiKeyword.VOID : NameUtils.javaToSmaliType(fieldTypeStr);

                String insertStr = String.format(Locale.US, "%s:%s", fieldName, fieldType);
                document.replaceString(context.getStartOffset(), context.getTailOffset(), insertStr);

                final Editor editor = context.getEditor();
                editor.getCaretModel().moveToOffset(context.getSelectionEndOffset());
                editor.getSelectionModel().removeSelection();
            }
        }
    }

}