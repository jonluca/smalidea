/*
 * Copyright 2015, Google Inc.
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

package org.jf.smalidea.util;

import org.antlr.runtime.CommonToken;
import org.jetbrains.annotations.Nullable;
import org.jf.smali.smaliFlexLexer;
import org.jf.smali.smaliParser;

import java.io.StringReader;

public class StringUtils {

    @Nullable
    public static String parseQuotedString(String str) {
        if (str.charAt(0) != '"') {
            return null;
        }

        // TODO: need to plumb in the api level..
        smaliFlexLexer lexer = new smaliFlexLexer(new StringReader(str), 18);
        lexer.setSuppressErrors(true);

        CommonToken token = (CommonToken)lexer.nextToken();
        if (token.getType() != smaliParser.STRING_LITERAL) {
            return null;
        }

        if (token.getStopIndex() != str.length()-1) {
            return null;
        }

        String text = token.getText();
        return text.substring(1, text.length()-1);
    }


    public static String parseString(String text, boolean deleteQuote) {
        return text == null ? null : deleteQuote ? deleteQuote(text) : text;
    }

    public static String deleteQuote(String text) {
        if (text.length() >= 2 && text.startsWith("\"") && text.endsWith("\"")) {
            return text.substring(1, text.length() - 1);
        }
        return text;
    }
}
