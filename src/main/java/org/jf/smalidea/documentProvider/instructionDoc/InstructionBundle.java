
package org.jf.smalidea.documentProvider.instructionDoc;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 *
 */
public class InstructionBundle {

//    public static String doc(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, @NotNull Object... params) {
//        return CommonBundle.message(getBundle(), key, params);
//    }

    private static Reference<ResourceBundle> ourBundle;

    @NonNls
    private static final String BUNDLE = "document.InstructionBundle";

    private InstructionBundle() {
    }

    private static ResourceBundle getBundle() {
        ResourceBundle bundle = com.intellij.reference.SoftReference.dereference(ourBundle);
        if (bundle == null) {
            bundle = ResourceBundle.getBundle(BUNDLE);
            ourBundle = new SoftReference<>(bundle);
        }
        return bundle;
    }
}
