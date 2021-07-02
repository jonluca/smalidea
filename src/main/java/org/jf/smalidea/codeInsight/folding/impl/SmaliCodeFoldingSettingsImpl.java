package org.jf.smalidea.codeInsight.folding.impl;

import com.intellij.codeInsight.folding.JavaCodeFoldingSettings;
import com.intellij.codeInsight.folding.impl.JavaCodeFoldingSettingsBase;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jf.smalidea.codeInsight.folding.SmaliCodeFoldingSettings;

@State(name = "SmaliCodeFoldingSettings", storages = @Storage("editor.codeinsight.xml"))
public class SmaliCodeFoldingSettingsImpl extends SmaliCodeFoldingSettingsBase implements
        PersistentStateComponent<SmaliCodeFoldingSettingsImpl> {

    @Override
    public SmaliCodeFoldingSettingsImpl getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull final SmaliCodeFoldingSettingsImpl state) {
        XmlSerializerUtil.copyBean(state, this);
    }
}
