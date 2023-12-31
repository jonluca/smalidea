/*
 * Copyright 2014, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.smalidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.tree.IElementType;
import org.jf.smali.LiteralTools;
import org.jf.smalidea.SmaliTokens;
import org.jf.smalidea.psi.SmaliCompositeElementFactory;
import org.jf.smalidea.psi.SmaliElementTypes;
import org.jf.smalidea.util.StringUtils;

public class SmaliLiteral extends SmaliCompositeElement implements PsiAnnotationMemberValue {
    public static final SmaliCompositeElementFactory FACTORY = new SmaliCompositeElementFactory() {
        @Override public SmaliCompositeElement createElement() {
            return new SmaliLiteral();
        }
    };

    public SmaliLiteral() {
        super(SmaliElementTypes.LITERAL);
    }

    public long getIntegralValue() {
        ASTNode literalNode = getNode().getFirstChildNode();
        IElementType literalType = literalNode.getElementType();

        if (literalType == SmaliTokens.LONG_LITERAL) {
            return LiteralTools.parseLong(literalNode.getText());
        } else if (literalType == SmaliTokens.NEGATIVE_INTEGER_LITERAL ||
                literalType == SmaliTokens.POSITIVE_INTEGER_LITERAL) {
            return LiteralTools.parseInt(literalNode.getText());
        } else if (literalType == SmaliTokens.SHORT_LITERAL) {
            return LiteralTools.parseShort(literalNode.getText());
        } else if (literalType == SmaliTokens.CHAR_LITERAL) {
            // TODO: implement this
            return -1;
        } else if (literalType == SmaliTokens.BYTE_LITERAL) {
            return LiteralTools.parseByte(literalNode.getText());
        } else if (literalType == SmaliTokens.BOOL_LITERAL) {
            return Boolean.parseBoolean(literalNode.getText())?1:0;
        } else {
            throw new RuntimeException("Not an integral literal");
        }
    }



    public Object getValue() {

        try {
            ASTNode literalNode = getNode().getFirstChildNode();
            IElementType literalType = literalNode.getElementType();

            if (literalType == SmaliTokens.STRING_LITERAL) {
                return StringUtils.parseString(literalNode.getText(), false);
            } else if (literalType == SmaliTokens.BOOL_LITERAL) {
                return Boolean.valueOf(literalNode.getText());
            }
            return getIntegralValue();
        } catch (Exception e) {
            throw new RuntimeException("Not an integral literal");
        }

    }
}
