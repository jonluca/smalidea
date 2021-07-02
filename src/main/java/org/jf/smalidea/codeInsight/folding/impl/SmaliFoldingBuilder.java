package org.jf.smalidea.codeInsight.folding.impl;

import com.intellij.codeInsight.folding.impl.CommentFoldingUtil;
import com.intellij.lang.ASTNode;
import com.intellij.lang.folding.CustomFoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtilCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliTokens;
import org.jf.smalidea.codeInsight.folding.SmaliCodeFoldingSettings;
import org.jf.smalidea.psi.SmaliElementTypes;
import org.jf.smalidea.psi.impl.*;
import org.jf.smalidea.util.NameUtils;
import org.jf.smalidea.util.PsiUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SmaliFoldingBuilder extends CustomFoldingBuilder implements DumbAware {

    @Override
    protected void buildLanguageFoldRegions(@NotNull List<FoldingDescriptor> descriptors, @NotNull PsiElement root, @NotNull Document document, boolean quick) {
        if (!(root instanceof SmaliFile)) return;

        SmaliFile file = (SmaliFile) root;

        Set<PsiElement> processedComments = new HashSet<>();

        addCommentsToFold(descriptors, root, document, processedComments);

        PsiClass[] classes = file.getClasses();
        for (PsiClass aClass : classes) {
            ProgressManager.checkCanceled();
            ProgressIndicatorProvider.checkCanceled();
            addFoldsForClass(descriptors, aClass, document);
        }

    }

    private static void addCommentsToFold(@NotNull List<? super FoldingDescriptor> list,
                                          @NotNull PsiElement element,
                                          @NotNull Document document,
                                          @NotNull Set<? super PsiElement> processedComments) {
        final PsiComment[] comments = PsiTreeUtil.getChildrenOfType(element, PsiComment.class);
        if (comments == null) return;

        for (PsiComment comment : comments) {
            addCommentToFold(list, comment, document, processedComments);
        }
    }

    private static void addCommentToFold(@NotNull List<? super FoldingDescriptor> list,
                                         @NotNull PsiComment comment,
                                         @NotNull Document document,
                                         @NotNull Set<? super PsiElement> processedComments) {
        final FoldingDescriptor commentDescriptor = CommentFoldingUtil.getCommentDescriptor(comment, document, processedComments,
                CustomFoldingBuilder::isCustomRegionElement,
                isCollapseCommentByDefault(comment));
        if (commentDescriptor != null) list.add(commentDescriptor);
    }

    private void addFoldsForClass(@NotNull List<? super FoldingDescriptor> list,
                                  @NotNull PsiClass aClass,
                                  @NotNull Document document) {

        addAnnotationsToFold(list, aClass.getAnnotations(), document, SmaliCodeFoldingSettings.getInstance().isCollapseClassAnnotations());

        for (PsiElement child = aClass.getFirstChild(); child != null; child = child.getNextSibling()) {
            ProgressIndicatorProvider.checkCanceled();
            if (child instanceof SmaliMethod) {
                SmaliMethod method = (SmaliMethod) child;
                addFoldsForMethod(list, method, document);
            } else if (child instanceof SmaliField) {
                SmaliField field = (SmaliField) child;
                addFoldsForField(list, field, document);
            }
        }
    }

    private void addFoldsForField(List<? super FoldingDescriptor> list, SmaliField field, Document document) {
        addAnnotationsToFold(list, field.getAnnotations(), document, SmaliCodeFoldingSettings.getInstance().isCollapseFieldAnnotations());
        addToFold(list, field, document, true, "{...}", fieldRange(field), SmaliCodeFoldingSettings.getInstance().isCollapseFields());
    }


    private static void addAnnotationsToFold(@NotNull List<? super FoldingDescriptor> list,
                                             @Nullable PsiAnnotation[] annotations,
                                             @NotNull Document document,
                                             boolean collapseAnnotation) {
        if (annotations == null) return;

        for (PsiAnnotation child : annotations) {
            final String qualifiedName;
            if (child != null && (qualifiedName = child.getQualifiedName()) != null) {
                String shortName = NameUtils.shortNameFromQualifiedName(qualifiedName);
                if (shortName == null || shortName.isEmpty()) {
                    shortName = "...";
                }
                addToFold(list, child, document, false, "@{" + shortName + "}", annotationRange(child), collapseAnnotation);
            }
        }
    }

    private void addFoldsForMethod(@NotNull List<? super FoldingDescriptor> list,
                                   @NotNull SmaliMethod method,
                                   @NotNull Document document) {

        addAnnotationsToFold(list, method.getAnnotations(), document,
                SmaliCodeFoldingSettings.getInstance().isCollapseMethodAnnotations());

        SmaliMethodPrototype prototype = method.getMethodPrototype();

        PsiElement methodStart = prototype.getReturnTypeElement();
        PsiElement methodEnd = method.getLastChild();

        PsiIdentifier nameIdentifier = method.getNameIdentifier();

        if (methodStart == null || methodEnd == null || nameIdentifier == null) {
            return;
        }

        if (document.getLineNumber(nameIdentifier.getTextRange().getStartOffset()) !=
                document.getLineNumber(method.getParameterList().getTextRange().getEndOffset())) {
            return;
        }

        int start = methodStart.getTextRange().getEndOffset();
        int end = methodEnd.getTextRange().getEndOffset();

        if (start >= end) {
            return;
        }

        TextRange range = new TextRange(start, end);
        list.add(new FoldingDescriptor(methodStart.getNode(), range, null, "{...}",
                SmaliCodeFoldingSettings.getInstance().isCollapseMethods(), Collections.emptySet()));

    }


    private static void addToFold(@NotNull List<? super FoldingDescriptor> list,
                                  @NotNull PsiElement elementToFold,
                                  @NotNull Document document,
                                  boolean allowOneLiners,
                                  @NotNull String placeholder,
                                  @Nullable TextRange range,
                                  boolean isCollapsedByDefault) {
        if (range != null) {
            PsiUtilCore.ensureValid(elementToFold);
            addFoldRegion(list, elementToFold, document, allowOneLiners, range, placeholder, isCollapsedByDefault);
        }
    }

    private static void addFoldRegion(@NotNull List<? super FoldingDescriptor> list,
                                      @NotNull PsiElement elementToFold,
                                      @NotNull Document document,
                                      boolean allowOneLiners,
                                      @NotNull TextRange range, @NotNull String placeholder, boolean isCollapsedByDefault) {
        final TextRange fileRange = elementToFold.getContainingFile().getTextRange();
        if (range.equals(fileRange)) return;

        // PSI element text ranges may be invalid because of reparse exception (see, for example, IDEA-10617)
        if (range.getStartOffset() < 0 || range.getEndOffset() > fileRange.getEndOffset()) {
            return;
        }

        if (!allowOneLiners) {
            int startLine = document.getLineNumber(range.getStartOffset());
            int endLine = document.getLineNumber(range.getEndOffset() - 1);
            if (startLine >= endLine || range.getLength() <= 1) {
                return;
            }
        } else if (range.getLength() <= placeholder.length()) {
            return;
        }
        list.add(new FoldingDescriptor(elementToFold.getNode(), range, null, placeholder, isCollapsedByDefault, Collections.emptySet()));
    }


    @NotNull
    private static TextRange annotationRange(@NotNull PsiAnnotation annotation) {
        PsiElement element = annotation;
        int startOffset = element.getTextRange().getStartOffset();
        PsiElement last = element;
        while (element instanceof PsiAnnotation) {
            last = element;
            element = PsiTreeUtil.skipWhitespacesAndCommentsForward(element);
        }

        return new TextRange(startOffset, last.getTextRange().getEndOffset());
    }


    @NotNull
    private static TextRange fieldRange(@NotNull SmaliField field) {
        int startOffset = field.getTypeElement().getTextRange().getEndOffset();
        final PsiElement last = field.getLastChild();
        return new TextRange(startOffset, last.getTextRange().getEndOffset());
    }


    /**
     * Determines whether comment should be collapsed by default.
     * If comment has unknown type then it is not collapsed.
     */
    private static boolean isCollapseCommentByDefault(@NotNull PsiComment comment) {
        return false;
    }

    @Override
    protected String getLanguagePlaceholderText(@NotNull ASTNode node, @NotNull TextRange range) {
        return "...";
    }

    @Override
    protected boolean isRegionCollapsedByDefault(@NotNull ASTNode node) {
        return false;
    }
}
