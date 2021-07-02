/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jf.smalidea.codeInsight.intention;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.io.IOUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliLanguage;
import org.jf.smalidea.SmaliTokens;
import org.jf.smalidea.util.PsiUtil;

public class ConvertToBasicLatinAction extends PsiElementBaseIntentionAction {
    private static final Logger LOG = Logger.getInstance("#org.jf.smalidea.codeInsight.intention.ConvertToBasicLatinAction");

    @Override
    public boolean isAvailable(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) {
        if (!element.getLanguage().isKindOf(SmaliLanguage.INSTANCE)) return false;
        final Pair<PsiElement, Handler> pair = findHandler(element);
        if (pair == null) return false;

        String text = pair.first.getText();
        return !IOUtil.isAscii(text);
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return CodeInsightBundle.message("intention.convert.to.basic.latin");
    }

    @NotNull
    @Override
    public String getText() {
        return getFamilyName();
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, @NotNull final PsiElement element) throws IncorrectOperationException {
        final Pair<PsiElement, Handler> pair = findHandler(element);
        if (pair == null) return;
        final PsiElement workElement = pair.first;
        final Handler handler = pair.second;
        final String newText = handler.processText(workElement);
        final PsiElement newElement = handler.createReplacement(workElement, newText);
        workElement.replace(newElement);
    }

    @Nullable
    private static Pair<PsiElement, Handler> findHandler(final PsiElement element) {
        for (final Handler handler : ourHandlers) {
            final PsiElement applicable = handler.findApplicable(element);
            if (applicable != null) {
                return Pair.create(applicable, handler);
            }
        }

        return null;
    }

    private static boolean shouldConvert(final char ch) {
        return Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.BASIC_LATIN;
    }

    private abstract static class Handler {
        @Nullable
        public abstract PsiElement findApplicable(final PsiElement element);

        public String processText(final PsiElement element) {
            final String text = element.getText();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                final char ch = text.charAt(i);
                if (!shouldConvert(ch)) {
                    sb.append(ch);
                } else {
                    convert(sb, ch);
                }
            }
            return sb.toString();
        }

        protected abstract void convert(StringBuilder sb, char ch);

        public abstract PsiElement createReplacement(final PsiElement element, final String newText);
    }

    private static final Handler[] ourHandlers = {new MyLiteralHandler()};

    private static class MyLiteralHandler extends Handler {

        @Override
        public PsiElement findApplicable(final PsiElement element) {
            return PsiUtil.isSmaliToken(element, SmaliTokens.STRING_LITERAL) ? element : null;
        }

        @Override
        public PsiElement createReplacement(final PsiElement element, final String newText) {
            return JavaPsiFacade.getElementFactory(element.getProject()).createExpressionFromText(newText, element.getParent());
        }

        @Override
        protected void convert(final StringBuilder sb, final char ch) {
            sb.append(String.format("\\u%04x", (int) ch));
        }
    }
}
