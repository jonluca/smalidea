package org.jf.smalidea.codeInsight.completion;

import com.intellij.lang.jvm.JvmModifier;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiMatcherExpression;
import com.intellij.psi.util.PsiMatchers;
import org.jf.dexlib2.Opcode;
import org.jf.smalidea.SmaliTokens;
import org.jf.smalidea.psi.SmaliElementTypes;
import org.jf.smalidea.psi.impl.*;
import org.jf.smalidea.util.PsiUtil;

import java.util.Arrays;
import java.util.regex.Pattern;

public class Infer {

    public static PsiClass infer(PsiElement variable) {
        // 寄存器可能引用的值有：
        // 1、字面值
        // 2、引用类型

        final Integer registerNum = Integer.parseInt(variable.getText().substring(1));


        SmaliInstruction instruction;
        do {
            instruction = (SmaliInstruction) PsiUtil.searchBackward(variable, PsiMatchers.hasClass(SmaliInstruction.class),
                    PsiMatchers.hasClass(SmaliMethod.class));

            if (instruction != null) {
                if (instruction.getRegister(0) == registerNum) {
                    break;
                }
                variable = instruction.getPrevSibling();
            }
        } while (instruction != null);

        if (instruction == null) {
            return null;
        }

        if (instruction.getOpcode() == Opcode.INVOKE_DIRECT) {
            SmaliMethodReference callingMethod = instruction.getMethodReference();
            SmaliTypeElement returnType = callingMethod.getReturnType();
            SmaliClassTypeElement containingType = callingMethod.getContainingType();
            PsiMethod resolvedMethod = callingMethod.resolve();
            if (resolvedMethod != null && resolvedMethod.isConstructor()) {
                //v0 = new Type()
                return resolvedMethod.getContainingClass();
            }
        }

        return null;
    }

}
