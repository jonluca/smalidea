package org.jf.smalidea.codeInsight.daemon.impl;

import com.intellij.codeInsight.daemon.impl.JavaLineMarkerProvider;

public class SmaliLineMarkerProvider extends JavaLineMarkerProvider {

    @Override
    public String getName() {
        return "Smali line markers";
    }
}
