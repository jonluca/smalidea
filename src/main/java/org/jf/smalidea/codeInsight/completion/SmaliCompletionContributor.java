package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.lang.ASTNode;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.SmaliTokens;

import static com.intellij.patterns.PlatformPatterns.psiElement;

/**
 * 用于支持自动提示Contributor
 * Created by lovely3x on 17/4/18.
 */
public class SmaliCompletionContributor extends CompletionContributor {

    public static final String ARROW = "->";
    public static final String DOT = ".";

    private final SmaliBasicCompletionContributorProvider mSmaliBasicCompletionContributorProvider;

    private String mCompletionPrefix;

    public static final ElementPattern<PsiElement> AFTER_ARROW = psiElement().afterLeaf(psiElement().withText(ARROW));
    public static final ElementPattern<PsiElement> MEMBER_ACCESS_DOT = psiElement().afterLeaf(psiElement().withText(DOT));

    private static final ElementPattern<PsiElement> REGISTER_ACCESS = psiElement()
            .afterLeaf(psiElement(SmaliTokens.REGISTER))
            .withText(".");

    public SmaliCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(PsiElement.class)/*.withLanguage(SmaliLanguage.INSTANCE)*/,
                this.mSmaliBasicCompletionContributorProvider = new SmaliBasicCompletionContributorProvider());
    }

    @Override
    public void fillCompletionVariants(@NotNull CompletionParameters parameters, @NotNull CompletionResultSet result) {
        if (parameters.getCompletionType() != CompletionType.BASIC) {
            return;
        }


        PsiElement originalPosition = parameters.getOriginalPosition();

        if (originalPosition == null) return;


        PsiElement position = parameters.getPosition();
        String prefix = obtainPrefix(parameters);

        CompletionResultSet myPrefixResultSet = result.withPrefixMatcher(prefix);

        String originalText = parameters.getOriginalPosition().getText();

        if (AFTER_ARROW.accepts(position)) {

            //当前类的方法,父类的方法
            //当前类的方法加黑显示,父类的方法普通显示
            //当子类和复写了父类的方法,那么只显示子类的方法

            prefix = result.getPrefixMatcher().getPrefix();
            if (prefix.contains(ARROW)) {
                prefix = prefix.substring(prefix.indexOf(ARROW) + ARROW.length());
            }

            SmaliCompletionUtil.processSmaliReference(parameters, result.withPrefixMatcher(prefix), ARROW);
        }else if(REGISTER_ACCESS.accepts(originalPosition)){
            SmaliCompletionUtil.processSmaliRegisterDot(parameters, result, DOT);
        }
        else if (originalText.equals(DOT)) {
            prefix = result.getPrefixMatcher().getPrefix();
            if (prefix.contains(DOT)) {
                prefix = prefix.substring(prefix.indexOf(DOT) + DOT.length());
            }

            SmaliCompletionUtil.processSmaliReference(parameters, result.withPrefixMatcher(prefix), DOT);
        }


        if (prefix.isEmpty()) return;


        super.fillCompletionVariants(parameters, result);
        SmaliClassNameCompletionContributor.addAllClasses(parameters, false, result.getPrefixMatcher(), myPrefixResultSet::addElement);
    }

    @Override
    public void beforeCompletion(@NotNull CompletionInitializationContext context) {
        super.beforeCompletion(context);
    }

    @Override
    public boolean invokeAutoPopup(@NotNull PsiElement position, char typeChar) {
        mCompletionPrefix = position.getText() + typeChar;
        mSmaliBasicCompletionContributorProvider.updateCompletionPrefix(mCompletionPrefix);
        return typeChar == '-' || (typeChar == '>' && arrowIsValid(position)) || '/' == typeChar;
    }

    private boolean arrowIsValid(@NotNull PsiElement position) {
        ASTNode node = position.getNode();
        return node != null && "-".equals(node.getText());
    }


    public static String obtainPrefix(CompletionParameters parameters) {
        PsiElement element = parameters.getPosition();

        if (element.getPrevSibling() == null) {
            PsiElement originalPosition = parameters.getOriginalPosition();
            if (originalPosition != null) {
                return parameters.getOriginalFile().getText().substring(originalPosition.getTextOffset(), parameters.getOffset());
            }
        }

        StringBuilder sb = new StringBuilder();

        while (element.getPrevSibling() != null) {
            element = element.getPrevSibling();
            sb.insert(0, element.getText());
        }

        return sb.toString();
    }


}
