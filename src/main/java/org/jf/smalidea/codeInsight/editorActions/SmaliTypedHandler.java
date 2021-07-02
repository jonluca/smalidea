package org.jf.smalidea.codeInsight.editorActions;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.codeInsight.lookup.LookupManager;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

/**
 * Created by lovely3x on 2017/4/24.
 */
public class SmaliTypedHandler extends TypedHandlerDelegate {

    @Override
    public Result checkAutoPopup(char charTyped, Project project, Editor editor, PsiFile file) {
        LookupImpl lookup = (LookupImpl) LookupManager.getActiveLookup(editor);

        if (lookup != null) {
            if (editor.getSelectionModel().hasSelection()) {
                lookup.performGuardedChange(() -> EditorModificationUtil.deleteSelectedText(editor));
            }
            return Result.STOP;
        }

        if (Character.isLetter(charTyped) || charTyped == '_' || charTyped == '-' || charTyped == '/') {
            AutoPopupController.getInstance(project).scheduleAutoPopup(editor);
            return Result.STOP;
        }

        return Result.CONTINUE;
    }

}
