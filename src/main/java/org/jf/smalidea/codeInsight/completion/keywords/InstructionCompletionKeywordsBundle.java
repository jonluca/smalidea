package org.jf.smalidea.codeInsight.completion.keywords;

/**
 *
 */
public class InstructionCompletionKeywordsBundle extends BaseKeyWordsBundle{

    private static final InstructionCompletionKeywordsBundle INSTANCE = new InstructionCompletionKeywordsBundle();

    public static InstructionCompletionKeywordsBundle getInstance() {
        return INSTANCE;
    }

    protected String getResourceName() {
        return "/completion/instructions";
    }
}
