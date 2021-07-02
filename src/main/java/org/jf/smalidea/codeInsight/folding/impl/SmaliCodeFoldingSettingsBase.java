package org.jf.smalidea.codeInsight.folding.impl;

import com.intellij.codeInsight.folding.CodeFoldingSettings;
import org.jf.smalidea.codeInsight.folding.SmaliCodeFoldingSettings;

public class SmaliCodeFoldingSettingsBase extends SmaliCodeFoldingSettings {

    private boolean COLLAPSE_END_OF_LINE_COMMENTS;
    private boolean COLLAPSE_FIELDS;

    private boolean COLLAPSE_CLASS_ANNOTATIONS;
    private boolean COLLAPSE_FIELD_ANNOTATIONS;
    private boolean COLLAPSE_METHODS_ANNOTATIONS;


    @Override
    public boolean isCollapseFileHeader() {
        return CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER;
    }

    @Override
    public void setCollapseFileHeader(boolean value) {
        CodeFoldingSettings.getInstance().COLLAPSE_FILE_HEADER = value;
    }


    @Override
    public boolean isCollapseFields() {
        return COLLAPSE_FIELDS;
    }

    @Override
    public void setCollapseFields(boolean value) {
        COLLAPSE_FIELDS = value;
    }

    @Override
    public boolean isCollapseFieldAnnotations() {
        return COLLAPSE_FIELD_ANNOTATIONS;
    }

    @Override
    public void setCollapseFieldAnnotations(boolean value) {
        COLLAPSE_FIELD_ANNOTATIONS = value;
    }

    @Override
    public boolean isCollapseClassAnnotations() {
        return COLLAPSE_CLASS_ANNOTATIONS;
    }

    @Override
    public void setCollapseClassAnnotations(boolean value) {
        COLLAPSE_CLASS_ANNOTATIONS = value;
    }

    @Override
    public boolean isCollapseMethods() {
        return CodeFoldingSettings.getInstance().COLLAPSE_METHODS;
    }

    @Override
    public void setCollapseMethods(boolean value) {
        CodeFoldingSettings.getInstance().COLLAPSE_METHODS = value;
    }


    @Override
    public boolean isCollapseMethodAnnotations() {
        return COLLAPSE_METHODS_ANNOTATIONS;
    }

    @Override
    public void setCollapseMethodAnnotations(boolean value) {
        COLLAPSE_METHODS_ANNOTATIONS = value;
    }

    @Override
    public boolean isCollapseEndOfLineComments() {
        return COLLAPSE_END_OF_LINE_COMMENTS;
    }

    @Override
    public void setCollapseEndOfLineComments(boolean value) {
        COLLAPSE_END_OF_LINE_COMMENTS = value;
    }
}
