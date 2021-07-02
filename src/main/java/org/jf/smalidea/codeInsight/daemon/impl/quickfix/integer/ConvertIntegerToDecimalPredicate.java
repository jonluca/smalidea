/*
 * Copyright 2003-2017 Dave Griffith, Bas Leijdekkers
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

import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.siyeh.ipp.base.PsiElementPredicate;
import org.jetbrains.annotations.NonNls;
import org.jf.smalidea.SmaliTokens;

class ConvertIntegerToDecimalPredicate implements PsiElementPredicate {

    public boolean satisfiedBy(PsiElement element) {
        if (!(element instanceof LeafPsiElement)) return false;

        @NonNls final String text = element.getText();

        if (text.length() < 2) return false;

        final IElementType type = ((LeafPsiElement) element).getElementType();

        if (type == SmaliTokens.LONG_LITERAL ||
                type == SmaliTokens.NEGATIVE_INTEGER_LITERAL ||
                type == SmaliTokens.POSITIVE_INTEGER_LITERAL ||
                type == SmaliTokens.BYTE_LITERAL ||
                type == SmaliTokens.SHORT_LITERAL ||
                type == SmaliTokens.FLOAT_LITERAL ||
                type == SmaliTokens.DOUBLE_LITERAL) {
            return text.startsWith("0x") || text.startsWith("0X");
        }

        return false;
    }
}