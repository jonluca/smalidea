package org.jf.smalidea.psi.impl;

import com.intellij.psi.PsiConstantEvaluationHelper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.ConstantExpressionEvaluator;
import org.jetbrains.annotations.Nullable;
import org.jf.smali.LiteralTools;
import org.jf.smalidea.util.StringUtils;

/**
 * smali表达式计算器
 * Created by lovely3x on 2017/4/27.
 */
public class SmaliPsiExpressionEvaluator implements ConstantExpressionEvaluator {

    @Override
    public Object computeConstantExpression(PsiElement expression, boolean throwExceptionOnOverflow) {
        if (expression instanceof SmaliFieldInitializer) {
            Object literal = ((SmaliLiteral) (expression.getLastChild())).getValue();
            if (literal instanceof String) {
                return StringUtils.deleteQuote((String) literal);
            }
            return literal;
        }
        return null;
    }

    @Override
    public Object computeExpression(PsiElement expression, boolean throwExceptionOnOverflow, @Nullable PsiConstantEvaluationHelper.AuxEvaluator auxEvaluator) {
        return null;
    }
}
