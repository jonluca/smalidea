package org.jf.smalidea.codeInsight.completion.keywords;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lovely3x on 17/4/19.
 */
public abstract class BaseKeyWordsBundle {

    private List<LookupElement> keywordList = null;

    protected LookupElement createKeyword(String keyword) {
        return new LookupElement() {
            @NotNull
            @Override
            public String getLookupString() {
                return keyword;
            }
        };

    }

    public void consume(Consumer<LookupElement> consumer) {
        if (consumer == null) return;
        if (keywordList == null) {
            readKeywords();
        }
        keywordList.forEach(consumer::consume);
    }

    private void readKeywords() {
        keywordList = new ArrayList<>();

        InputStream is = InstructionCompletionKeywordsBundle.class.getResourceAsStream(getResourceName());
        InputStreamReader bis = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(bis);
        String line;
        try {
            while ((line = br.readLine()) != null) {
                String keyword = line.trim();
                keywordList.add(createKeyword(keyword));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                br.close();
            } catch (IOException ignored) {
            }
        }
    }


    protected abstract String getResourceName();
}
