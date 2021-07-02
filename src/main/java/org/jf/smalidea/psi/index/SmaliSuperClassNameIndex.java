package org.jf.smalidea.psi.index;

import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;

/**
 * Created by lovely3x on 17/4/17.
 */
public class SmaliSuperClassNameIndex extends StringStubIndexExtension {


    @NotNull
    @Override
    public StubIndexKey getKey() {
        return SmaliStubIndexKeys.SUPER_CLASSES;
    }
}
