package org.jf.smalidea.documentProvider;

import com.intellij.lang.java.JavaDocumentationProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by lovely3x on 16/11/6.
 */
public class SmaliDocumentationProvider extends JavaDocumentationProvider {

    @Override
    public String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        return super.getQuickNavigateInfo(element, originalElement);
    }

    public SmaliDocumentationProvider() {
        super();
    }

    @Override
    public List<String> getUrlFor(PsiElement element, PsiElement originalElement) {
        return super.getUrlFor(element, originalElement);
    }


    @Override
    public PsiComment findExistingDocComment(PsiComment comment) {
        return super.findExistingDocComment(comment);
    }

    @Nullable
    @Override
    public Pair<PsiElement, PsiComment> parseContext(@NotNull PsiElement startPoint) {
        return super.parseContext(startPoint);
    }

    @Override
    public String generateDocumentationContentStub(PsiComment _comment) {
        return super.generateDocumentationContentStub(_comment);
    }

    @Override
    public String generateDoc(PsiElement element, PsiElement originalElement) {
        return super.generateDoc(element, originalElement);
    }

    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object object, PsiElement element) {
        return super.getDocumentationElementForLookupItem(psiManager, object, element);
    }

    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String link, PsiElement context) {
        return super.getDocumentationElementForLink(psiManager, link, context);
    }

    @Override
    public String fetchExternalDocumentation(Project project, PsiElement element, List<String> docUrls) {
//        for (String url : docUrls) {
//
//            if (url.startsWith("instruction://")) {
//                String document = InstructionBundle.doc(element.getText(), element.getText());
//                Pattern compile = Pattern.compile("<dlink>.*?</dlink>");
//                while (compile.matcher(document).find()) {
//                    Matcher matcher = compile.matcher(document);
//                    if (matcher.find()) {
//                        String value = matcher.group();
//                        String dLink = value.substring("<dlink>".length(), value.length() - "</dlink>".length());
//                        String dLinkContent = super.fetchExternalDocumentation(project, element, Collections.singletonList(dLink));
//                        document = document.replace(value, dLinkContent == null ? "" : dLinkContent);
//                    }
//                }
//                return document;
//            }
//        }
        return super.fetchExternalDocumentation(project, element, docUrls);
    }

    @Override
    public boolean hasDocumentationFor(PsiElement element, PsiElement originalElement) {
//        if (originalElement instanceof LeafPsiElement) {
//            return true;
//        }
        return super.hasDocumentationFor(element, originalElement);
    }

    @Override
    public boolean canPromptToConfigureDocumentation(PsiElement element) {
        return super.canPromptToConfigureDocumentation(element);
    }

    @Override
    public void promptToConfigureDocumentation(PsiElement element) {
        super.promptToConfigureDocumentation(element);
    }

    @Nullable
    @Override
    public PsiElement getCustomDocumentationElement(@NotNull Editor editor, @NotNull PsiFile file, @Nullable PsiElement contextElement) {
        return super.getCustomDocumentationElement(editor, file, contextElement);
//        if (contextElement instanceof PsiWhiteSpace) return null;
//        if (contextElement instanceof LeafPsiElement) {
//            com.intellij.psi.tree.IElementType elementType = ((LeafPsiElement) contextElement).getElementType();
//            if (elementType.equals(SmaliTokens.POSITIVE_INTEGER_LITERAL)) {//字面量
//
//            }
//        }
//
//        SmaliStubBasedPsiElement element = PsiTreeUtil.getParentOfType(contextElement, SmaliStubBasedPsiElement.class, false);
//        return element != null ? element : super.getCustomDocumentationElement(editor, file, contextElement);
    }
}
