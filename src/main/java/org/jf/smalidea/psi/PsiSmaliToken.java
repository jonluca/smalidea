package org.jf.smalidea.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * Created by lovely3x on 2017/4/24.
 */
public interface PsiSmaliToken extends PsiElement{

    /**
     * Returns the type of the token.
     *
     * @return the token type.
     */
    IElementType getTokenType();
}
