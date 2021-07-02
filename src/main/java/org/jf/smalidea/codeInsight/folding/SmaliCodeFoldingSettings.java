package org.jf.smalidea.codeInsight.folding;

import com.intellij.openapi.components.ServiceManager;

public abstract class SmaliCodeFoldingSettings {

  public static SmaliCodeFoldingSettings getInstance() {
    return ServiceManager.getService(SmaliCodeFoldingSettings.class);
  }


  public abstract boolean isCollapseFileHeader();
  public abstract void setCollapseFileHeader(boolean value);

  public abstract boolean isCollapseFields();
  public abstract void setCollapseFields(boolean value);


  public abstract boolean isCollapseFieldAnnotations();
  public abstract void setCollapseFieldAnnotations(boolean value);


  public abstract boolean isCollapseClassAnnotations();
  public abstract void setCollapseClassAnnotations(boolean value);

  public abstract boolean isCollapseMethods();
  public abstract void setCollapseMethods(boolean value);


  public abstract boolean isCollapseMethodAnnotations();
  public abstract void setCollapseMethodAnnotations(boolean value);


  public abstract boolean isCollapseEndOfLineComments();
  public abstract void setCollapseEndOfLineComments(boolean value);

}
