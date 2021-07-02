package org.jf.smalidea.structureView;

import com.intellij.ide.structureView.impl.java.JavaFileTreeModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClassOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class SmaliStructureViewModel extends JavaFileTreeModel {

    public SmaliStructureViewModel(@NotNull PsiClassOwner file, @Nullable Editor editor) {
        super(file, editor);
    }
}
