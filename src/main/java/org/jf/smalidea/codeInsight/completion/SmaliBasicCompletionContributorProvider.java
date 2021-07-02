package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementDecorator;
import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Consumer;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.SmaliLanguage;
import org.jf.smalidea.codeInsight.completion.keywords.DirectivesCompletionKeywordsBundle;
import org.jf.smalidea.codeInsight.completion.keywords.InstructionCompletionKeywordsBundle;

public class SmaliBasicCompletionContributorProvider extends CompletionProvider<CompletionParameters> {


    private String mCompletionPrefix;

    @Override
    protected void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet result) {

        if (parameters.getCompletionType() != CompletionType.BASIC) return;

        if (!isInSmaliContext(parameters.getPosition())) return;

        PsiElement originalPosition = parameters.getOriginalPosition();

        if (originalPosition == null) return;

        String prefix = SmaliCompletionContributor.obtainPrefix(parameters);

        if (prefix.isEmpty()) return;

        CompletionResultSet myPrefixResultSet = result.withPrefixMatcher(prefix);

        Consumer<LookupElement> lookupElementConsumer = element -> {
            if (myPrefixResultSet.getPrefixMatcher().isStartMatch(element)) {
                myPrefixResultSet.addElement(LookupElementDecorator.withInsertHandler(element, KeywordInsertHandler.INSTANCE));
            }
        };

        //指令关键字
        InstructionCompletionKeywordsBundle.getInstance().consume(lookupElementConsumer);

        //命令关键字
        DirectivesCompletionKeywordsBundle.getInstance().consume(lookupElementConsumer);
    }


    private static boolean isInSmaliContext(PsiElement position) {
        return PsiUtilCore.findLanguageFromElement(position).isKindOf(SmaliLanguage.INSTANCE);
    }

    public void updateCompletionPrefix(String completionPrefix) {
        this.mCompletionPrefix = completionPrefix;
    }

    public static class KeywordInsertHandler implements InsertHandler<LookupElement> {
        public static final KeywordInsertHandler INSTANCE = new KeywordInsertHandler();

        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {
            Document doc = context.getDocument();
            doc.replaceString(context.getStartOffset(), context.getTailOffset(), item.getLookupString() + " ");
            context.commitDocument();
            context.getEditor().getCaretModel().moveToOffset(context.getTailOffset());
        }
    }

    public static class CurlyBracesInsertHandler implements InsertHandler<LookupElement> {

        @Override
        public void handleInsert(InsertionContext context, LookupElement item) {

        }
    }
}