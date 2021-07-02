package org.jf.smalidea.codeInsight.completion.keywords;

/**
 * Created by lovely3x on 17/4/19.
 */
public class DirectivesCompletionKeywordsBundle extends BaseKeyWordsBundle{

    private static final DirectivesCompletionKeywordsBundle INSTANCE = new DirectivesCompletionKeywordsBundle();

    public static DirectivesCompletionKeywordsBundle getInstance() {
        return INSTANCE;
    }

    protected String getResourceName() {
        return "/completion/directives";
    }
}
