
package org.jf.smalidea.psi.index;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiReferenceList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.psi.stubs.StubIndexKey;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.psi.impl.search.SmaliSourceFilterScope;

import java.util.Collection;

public class SmaliSuperClassNameOccurenceIndex extends StringStubIndexExtension<PsiReferenceList> {

    private static final int VERSION = 1;

    private static final SmaliSuperClassNameOccurenceIndex ourInstance = new SmaliSuperClassNameOccurenceIndex();

    public static SmaliSuperClassNameOccurenceIndex getInstance() {
        return ourInstance;
    }

    @NotNull
    @Override
    public StubIndexKey<String, PsiReferenceList> getKey() {
        return SmaliStubIndexKeys.SUPER_CLASSES;
    }

    @Override
    public Collection<PsiReferenceList> get(@NotNull final String s, @NotNull final Project project, @NotNull final GlobalSearchScope scope) {
        return StubIndex.getElements(getKey(), s, project, new SmaliSourceFilterScope(scope), PsiReferenceList.class);
    }

    @Override
    public int getVersion() {
        return super.getVersion() + VERSION;
    }
}
