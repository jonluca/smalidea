/*
 * Copyright 2003-2008 Dave Griffith, Bas Leijdekkers
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

import com.intellij.psi.tree.IElementType;
import com.siyeh.ipp.base.PsiElementPredicate;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.SmaliTokens;

public class ConvertIntegerToHexIntention extends ConvertNumberIntentionBase {
    @Override
    @NotNull
    public PsiElementPredicate getElementPredicate() {
        return new ConvertIntegerToHexPredicate();
    }

    @Override
    protected String convertValue(final Number value, final IElementType type) {
        if (SmaliTokens.NEGATIVE_INTEGER_LITERAL.equals(type) || SmaliTokens.POSITIVE_INTEGER_LITERAL.equals(type)) {
            final int intValue = value.intValue();
            return "0x" + Integer.toHexString(intValue);
        } else if (SmaliTokens.LONG_LITERAL.equals(type)) {
            final long longValue = value.longValue();
            return "0x" + Long.toHexString(longValue) + "L";
        } else if (SmaliTokens.FLOAT_LITERAL.equals(type)) {
            final float floatValue = value.floatValue();
            return Float.toHexString(floatValue) + 'f';
        } else if (SmaliTokens.DOUBLE_LITERAL.equals(type)) {
            final double doubleValue = value.doubleValue();
            return Double.toHexString(doubleValue);
        }

        return null;
    }
}