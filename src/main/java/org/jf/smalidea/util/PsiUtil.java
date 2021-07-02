/*
 * Copyright 2016, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 * Neither the name of Google Inc. nor the names of its
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

package org.jf.smalidea.util;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.application.ReadAction;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiMatcherExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliLanguage;

import java.util.regex.Pattern;

public class PsiUtil {

    private static final Pattern REGISTER_PATTERN = Pattern.compile("^v\\d+$");

    public static boolean isSmaliToken(@Nullable PsiElement element, IElementType type) {
        return element instanceof ASTNode && ((ASTNode) element).getElementType() == type;
    }

    public static PsiElement searchBackward(PsiElement element, PsiMatcherExpression matcher,
                                            PsiMatcherExpression until) {
        while (!matcher.match(element)) {
            if (until.match(element)) {
                return null;
            }
            PsiElement prev = element.getPrevSibling();
            if (prev == null) {
                prev = element.getParent();
                if (prev == null) {
                    return null;
                }
            }
            element = prev;
        }
        return element;
    }

    public static PsiElement searchForward(PsiElement element, PsiMatcherExpression matcher,
                                           PsiMatcherExpression until) {
        while (!matcher.match(element)) {
            if (until.match(element)) {
                return null;
            }
            PsiElement next = element.getNextSibling();
            if (next == null) {
                next = element.getParent();
                if (next == null) {
                    return null;
                }
            }
            element = next;
        }
        return element;
    }


    public static boolean isJavaLangObject(@NotNull final PsiClass aClass) {
        return ReadAction.compute(() -> aClass.isValid() && CommonClassNames.JAVA_LANG_OBJECT.equals(aClass.getQualifiedName()));
    }

    public static boolean isFinal(@NotNull final PsiClass aClass) {
        return ReadAction.compute(() -> aClass.hasModifierProperty(PsiModifier.FINAL));
    }

    public static boolean isSmaliFile(PsiFile file) {
        return file != null && file.getLanguage() == SmaliLanguage.INSTANCE;
    }

    public static boolean isRegister(PsiElement element) {
        return element instanceof LeafPsiElement && REGISTER_PATTERN.matcher(element.getText()).matches();
    }
}
