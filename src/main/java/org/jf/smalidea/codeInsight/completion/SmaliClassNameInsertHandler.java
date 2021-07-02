package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.completion.JavaCompletionUtil;
import org.jf.smalidea.util.NameUtils;


class SmaliClassNameInsertHandler implements InsertHandler<SmaliPsiClassReferenceElement> {

    static final InsertHandler<SmaliPsiClassReferenceElement> SMALI_CLASS_INSERT_HANDLER = new SmaliClassNameInsertHandler();

    @Override
    public void handleInsert(final InsertionContext context, final SmaliPsiClassReferenceElement item) {

        String qName = item.getQualifiedName();

        if (qName != null && item.getType() != null) {
            int start = JavaCompletionUtil.findQualifiedNameStart(context);
            context.getDocument().replaceString(start, context.getTailOffset(), NameUtils.javaToSmaliType(item.getType()));
        }
    }
}
