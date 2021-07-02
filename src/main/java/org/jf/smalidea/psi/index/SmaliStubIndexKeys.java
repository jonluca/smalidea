
package org.jf.smalidea.psi.index;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.stubs.StubIndexKey;
import org.jf.smalidea.psi.impl.SmaliAnnotation;
import org.jf.smalidea.psi.impl.SmaliClass;
import org.jf.smalidea.psi.impl.SmaliField;
import org.jf.smalidea.psi.impl.SmaliMethod;

/**
 *
 */
public class SmaliStubIndexKeys {

    public static final StubIndexKey<String, SmaliClass> CLASS = StubIndexKey.createIndexKey("smali.class.name");
    public static final StubIndexKey<String, SmaliAnnotation> ANNOTATIONS = StubIndexKey.createIndexKey("smali.annotations");
    public static final StubIndexKey<String, PsiReferenceList> SUPER_CLASSES = StubIndexKey.createIndexKey("smali.class.extlist");
    public static final StubIndexKey<String, SmaliField> FIELDS = StubIndexKey.createIndexKey("smali.field.name");
    public static final StubIndexKey<String, SmaliMethod> METHODS = StubIndexKey.createIndexKey("smali.method.name");
    public static final StubIndexKey<Integer, PsiClass> CLASS_FQN = StubIndexKey.createIndexKey("smali.class.fqn");

    private SmaliStubIndexKeys() {
    }
}