
package org.jf.smalidea.psi.impl.search;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.DelegatingGlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.smalidea.SmaliFileType;

public class SmaliSourceFilterScope extends DelegatingGlobalSearchScope {

    @Nullable
    private final ProjectFileIndex myIndex;


    public SmaliSourceFilterScope(@NotNull final GlobalSearchScope delegate) {
        super(delegate);

        Project project = getProject();
        myIndex = project == null ? null : ProjectRootManager.getInstance(project).getFileIndex();
    }

    @Override
    public boolean contains(@NotNull final VirtualFile file) {
        if (!super.contains(file)) {
            return false;
        }

        if (myIndex == null) {
            return false;
        }

        if (SmaliFileType.INSTANCE == file.getFileType()) {
            return myIndex.isInLibraryClasses(file);
        }

        return myIndex.isInSourceContent(file) ||
                myBaseScope.isForceSearchingInLibrarySources() && myIndex.isInLibrarySource(file);
    }

}