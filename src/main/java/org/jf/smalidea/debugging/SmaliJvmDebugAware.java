package org.jf.smalidea.debugging;

import com.intellij.debugger.engine.JavaDebugAware;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.SmaliLanguage;

public class SmaliJvmDebugAware extends JavaDebugAware {

    @Override
    public boolean isBreakpointAware(@NotNull PsiFile psiFile) {
        return psiFile.getLanguage() == SmaliLanguage.INSTANCE;
    }
}
