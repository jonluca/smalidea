/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
package org.jf.smalidea.codeInsight.daemon.impl.quickfix.integer;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.GeneratedMarkerVisitor;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliTokens;
import org.jf.smalidea.codeInsight.intention.SmaliIntention;

public abstract class ConvertNumberIntentionBase extends SmaliIntention {

    @Override
    protected void processIntention(@NotNull final PsiElement expression) throws IncorrectOperationException {

        String strValue = expression.getText();
        if (strValue == null) return;

        int radix = strValue.startsWith("0x") || strValue.startsWith("0X") ? 16 : 10;
        if (radix == 16) {
            strValue = strValue.substring(2);
        }

        Number value = 0L;

        final IElementType type = ((LeafPsiElement) expression).getElementType();
        if (type == SmaliTokens.LONG_LITERAL) {
            value = Long.parseLong(strValue, radix);
        } else if (type == SmaliTokens.NEGATIVE_INTEGER_LITERAL || type == SmaliTokens.POSITIVE_INTEGER_LITERAL) {
            value = Integer.parseInt(strValue, radix);
        } else if (type == SmaliTokens.BYTE_LITERAL) {
            value = Byte.parseByte(strValue, radix);
        } else if (type == SmaliTokens.SHORT_LITERAL) {
            value = Short.parseShort(strValue, radix);
        } else if (type == SmaliTokens.FLOAT_LITERAL) {
            value = Float.parseFloat(strValue);
        } else if (type == SmaliTokens.DOUBLE_LITERAL) {
            value = Double.parseDouble(strValue);
        }

        final String resultString = convertValue(value, type);
        if (resultString == null) return;

        replaceExpression(expression, type, resultString);
    }


    private static void replaceExpression(@NotNull PsiElement expression, IElementType type, @NotNull @NonNls String newExpressionText) {
        final Project project = expression.getProject();

        PsiElement parent = expression.getParent();
        PsiFile containingFile = expression.getContainingFile();

        PsiElement newElement = new LeafPsiElement(type, newExpressionText) {

            @Override
            public PsiElement getParent() {
                return parent;
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public PsiFile getContainingFile() {
                return containingFile;
            }

            @Override
            public PsiManagerEx getManager() {
                return (PsiManagerEx) expression.getManager();
            }

            @NotNull
            @Override
            public Project getProject() {
                return expression.getProject();
            }
        };
        GeneratedMarkerVisitor.markGenerated(newElement);
        final PsiElement replacementExpression = expression.replace(newElement);

        final CodeStyleManager styleManager = CodeStyleManager.getInstance(project);
        styleManager.reformat(replacementExpression);
    }

    @Nullable
    protected abstract String convertValue(final Number value, final IElementType type);
}
