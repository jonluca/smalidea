/*
 * Copyright 2014, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.smalidea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.jvm.JvmElementVisitor;
import com.intellij.lang.jvm.JvmModifier;
import com.intellij.model.psi.PsiSymbolDeclaration;
import com.intellij.model.psi.PsiSymbolReference;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.PsiModifier.ModifierConstant;
import com.intellij.psi.impl.PsiImplUtil;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jf.dexlib2.AccessFlags;
import org.jf.smalidea.SmaliIcons;
import org.jf.smalidea.psi.SmaliElementTypes;
import org.jf.smalidea.psi.iface.SmaliModifierListOwner;
import org.jf.smalidea.psi.stub.SmaliFieldStub;
import org.jf.smalidea.util.IconUtils;
import org.jf.smalidea.util.NameUtils;

import javax.swing.*;
import java.util.Collection;

public class SmaliField extends SmaliStubBasedPsiElement<SmaliFieldStub> implements PsiField, SmaliModifierListOwner, ItemPresentation {
    public SmaliField(@NotNull SmaliFieldStub stub) {
        super(stub, SmaliElementTypes.FIELD);
    }

    public SmaliField(@NotNull ASTNode node) {
        super(node);
    }

    @Nullable @Override public String getName() {
        SmaliFieldStub stub = getStub();
        if (stub != null) {
            return stub.getName();
        }

        SmaliMemberName smaliMemberName = findChildByClass(SmaliMemberName.class);
        if (smaliMemberName == null || smaliMemberName.getText().isEmpty()) {
            return null;
        }
        return smaliMemberName.getText();
    }

    @NotNull @Override public SmaliModifierList getModifierList() {
        SmaliModifierList modifierList = getStubOrPsiChild(SmaliElementTypes.MODIFIER_LIST);
        assert modifierList != null;
        return modifierList;
    }

    @NotNull @Override public SmaliMemberName getNameIdentifier() {
        SmaliMemberName memberName = findChildByClass(SmaliMemberName.class);
        assert memberName != null;
        return memberName;
    }

    @Override
    public @Nullable PsiElement getIdentifyingElement() {
        return PsiField.super.getIdentifyingElement();
    }

    @Override
    public ItemPresentation getPresentation() {
        return this;
    }

    @Nullable @Override public PsiDocComment getDocComment() {
        return null;
    }

    @Override public boolean isDeprecated() {
        return PsiImplUtil.isDeprecatedByAnnotation(this);
    }

    @Nullable @Override public PsiClass getContainingClass() {
        return (PsiClass)getParentByStub();
    }

    @NotNull @Override public PsiType getType() {
        SmaliFieldStub stub = getStub();
        if (stub != null) {
            return NameUtils.resolveSmaliToPsiType(this, stub.getSmaliTypeName());
        }
        PsiTypeElement typeElement = getTypeElement();
        if (typeElement == null) {
            // If we don't have a type (i.e. syntax error), use Object as a safe-ish fallback
            PsiElementFactory factory = JavaPsiFacade.getInstance(getProject()).getElementFactory();
            return factory.createTypeFromText("java.lang.Object", this);
        }
        return getTypeElement().getType();
    }

    @Override
    public <T> T accept(@NotNull JvmElementVisitor<T> visitor) {
        return PsiField.super.accept(visitor);
    }

    @Nullable @Override public SmaliTypeElement getTypeElement() {
        return findChildByClass(SmaliTypeElement.class);
    }

    @Nullable @Override public PsiExpression getInitializer() {
        // TODO: implement this
        return null;
    }

    @Override public boolean hasInitializer() {
        // TODO: implement this
        return false;
    }

    @Override public void normalizeDeclaration() throws IncorrectOperationException {
        // not applicable
    }

    @Nullable @Override public Object computeConstantValue() {
        // TODO: implement this
        return null;
    }

    @Override public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
        SmaliMemberName smaliMemberName = getNameIdentifier();
        smaliMemberName.setName(name);
        return this;
    }

    @Override public boolean hasModifierProperty(@ModifierConstant @NonNls @NotNull String name) {
        return getModifierList().hasModifierProperty(name);
    }

    @NotNull @Override public SmaliAnnotation[] getAnnotations() {
        return getStubOrPsiChildren(SmaliElementTypes.ANNOTATION, new SmaliAnnotation[0]);
    }

    @Override
    public @Nullable PsiAnnotation getAnnotation(@NotNull @NonNls String fqn) {
        return PsiField.super.getAnnotation(fqn);
    }

    @NotNull @Override public SmaliAnnotation[] getApplicableAnnotations() {
        return getAnnotations();
    }

    @Nullable @Override public SmaliAnnotation findAnnotation(@NotNull @NonNls String qualifiedName) {
        for (SmaliAnnotation annotation: getAnnotations()) {
            if (qualifiedName.equals(annotation.getQualifiedName())) {
                return annotation;
            }
        }
        return null;
    }

    @NotNull @Override public SmaliAnnotation addAnnotation(@NotNull @NonNls String qualifiedName) {
        // TODO: implement this
        return null;
    }

    @Override public boolean hasAnnotation(@NotNull String fqn) {
        return SmaliModifierListOwner.super.hasAnnotation(fqn);
    }

    @Override
    public boolean hasModifier(@NotNull JvmModifier modifier) {
        return PsiField.super.hasModifier(modifier);
    }

    @Override
    public @Nullable PsiElement getSourceElement() {
        return PsiField.super.getSourceElement();
    }

    @Override public void setInitializer(@Nullable PsiExpression initializer) throws IncorrectOperationException {
        // TODO: implement this
    }

    @Override
    public @NotNull TextRange getTextRangeInParent() {
        return super.getTextRangeInParent();
    }

    @Override public int getTextOffset() {
        SmaliMemberName smaliMemberName = getNameIdentifier();
        return smaliMemberName.getTextOffset();
    }

    @Override
    public @NotNull Collection<? extends @NotNull PsiSymbolDeclaration> getOwnDeclarations() {
        return super.getOwnDeclarations();
    }

    @Override
    public @NotNull Collection<? extends @NotNull PsiSymbolReference> getOwnReferences() {
        return super.getOwnReferences();
    }


    @Nullable
    @Override
    public Icon getIcon(int flags) {
        if (getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
            return SmaliIcons.StaticFieldIcon;
        } else {
            return SmaliIcons.InstanceFieldIcon;
        }
    }

    @Nullable
    @Override
    public String getPresentableText() {
        return getName() + ": " + getType().getPresentableText();
    }

    @Nullable
    @Override
    public String getLocationString() {
        return "";
    }

    @Override
    public @Nullable Icon getIcon(boolean unused) {
        return null;
    }
}
