
package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.JavaPsiClassReferenceElement;
import com.intellij.psi.PsiClass;
import org.jf.smalidea.util.NameUtils;

public class SmaliPsiClassReferenceElement extends JavaPsiClassReferenceElement {
    private final String myQualifiedName;

    public SmaliPsiClassReferenceElement(PsiClass psiClass) {
        super(psiClass);
        myQualifiedName = NameUtils.javaToSmaliType(psiClass.getQualifiedName());
    }


    public String getQualifiedName() {
        return myQualifiedName;
    }


}
