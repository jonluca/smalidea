/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jf.smalidea.codeInsight.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.patterns.ElementPattern;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.UnmodifiableIterator;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.character;

public class CompletionUtil {

  public static final @NonNls String DUMMY_IDENTIFIER = CompletionInitializationContext.DUMMY_IDENTIFIER;
  public static final @NonNls String DUMMY_IDENTIFIER_TRIMMED = DUMMY_IDENTIFIER.trim();


  public static boolean shouldShowFeature(final CompletionParameters parameters, @NonNls final String id) {
    if (FeatureUsageTracker.getInstance().isToBeAdvertisedInLookup(id, parameters.getPosition().getProject())) {
      FeatureUsageTracker.getInstance().triggerFeatureShown(id);
      return true;
    }
    return false;
  }

  public static String findJavaIdentifierPrefix(CompletionParameters parameters) {
    return findJavaIdentifierPrefix(parameters.getPosition(), parameters.getOffset());
  }

  public static String findJavaIdentifierPrefix(final PsiElement insertedElement, final int offset) {
    return findIdentifierPrefix(insertedElement, offset, character().javaIdentifierPart(), character().javaIdentifierStart());
  }

  public static String findReferenceOrAlphanumericPrefix(CompletionParameters parameters) {
    String prefix = findReferencePrefix(parameters);
    return prefix == null ? findAlphanumericPrefix(parameters) : prefix;
  }

  public static String findAlphanumericPrefix(CompletionParameters parameters) {
    return findIdentifierPrefix(parameters.getPosition().getContainingFile(), parameters.getOffset(), character().letterOrDigit(), character().letterOrDigit());
  }

  public static String findIdentifierPrefix(PsiElement insertedElement, int offset, ElementPattern<Character> idPart,
                                             ElementPattern<Character> idStart) {
    if(insertedElement == null) return "";
    final String text = insertedElement.getText();

    final int offsetInElement = offset - insertedElement.getTextRange().getStartOffset();
    int start = offsetInElement - 1;
    while (start >=0 ) {
      if (!idPart.accepts(text.charAt(start))) break;
      --start;
    }
    while (start + 1 < offsetInElement && !idStart.accepts(text.charAt(start + 1))) {
      start++;
    }

    return text.substring(start + 1, offsetInElement).trim();
  }

  @Nullable
  public static String findReferencePrefix(CompletionParameters parameters){
    return com.intellij.codeInsight.completion.CompletionUtil.findReferencePrefix(parameters.getPosition(), parameters.getOffset());
  }


  static InsertionContext emulateInsertion(InsertionContext oldContext, int newStart, final LookupElement item) {
    final InsertionContext newContext = newContext(oldContext, item);
    emulateInsertion(item, newStart, newContext);
    return newContext;
  }

  private static InsertionContext newContext(InsertionContext oldContext, LookupElement forElement) {
    final Editor editor = oldContext.getEditor();
    return new InsertionContext(new OffsetMap(editor.getDocument()), Lookup.AUTO_INSERT_SELECT_CHAR, new LookupElement[]{forElement}, oldContext.getFile(), editor,
                                oldContext.shouldAddCompletionChar());
  }

  public static InsertionContext newContext(InsertionContext oldContext, LookupElement forElement, int startOffset, int tailOffset) {
    final InsertionContext context = newContext(oldContext, forElement);
    setOffsets(context, startOffset, tailOffset);
    return context;
  }

  public static void emulateInsertion(LookupElement item, int offset, InsertionContext context) {
    setOffsets(context, offset, offset);

    final Editor editor = context.getEditor();
    final Document document = editor.getDocument();
    final String lookupString = item.getLookupString();

    document.insertString(offset, lookupString);
    editor.getCaretModel().moveToOffset(context.getTailOffset());
    PsiDocumentManager.getInstance(context.getProject()).commitDocument(document);
    item.handleInsert(context);
    PsiDocumentManager.getInstance(context.getProject()).doPostponedOperationsAndUnblockDocument(document);
  }

  private static void setOffsets(InsertionContext context, int offset, final int tailOffset) {
    final OffsetMap offsetMap = context.getOffsetMap();
    offsetMap.addOffset(CompletionInitializationContext.START_OFFSET, offset);
    offsetMap.addOffset(CompletionInitializationContext.IDENTIFIER_END_OFFSET, tailOffset);
    offsetMap.addOffset(CompletionInitializationContext.SELECTION_END_OFFSET, tailOffset);
    context.setTailOffset(tailOffset);
  }

  @Nullable
  public static PsiElement getTargetElement(LookupElement lookupElement) {
    PsiElement psiElement = lookupElement.getPsiElement();
    if (psiElement != null && psiElement.isValid()) {
      return getOriginalElement(psiElement);
    }

 /*   Object object = lookupElement.getObject();
    if (object instanceof LookupValueWithPsiElement) {
      final PsiElement element = ((LookupValueWithPsiElement)object).getElement();
      if (element != null && element.isValid()) return getOriginalElement(element);
    }*/

    return null;
  }

  @Nullable
  public static <T extends PsiElement> T getOriginalElement(@NotNull T psi) {
    return CompletionUtilCoreImpl.getOriginalElement(psi);
  }

  @NotNull
  public static <T extends PsiElement> T getOriginalOrSelf(@NotNull T psi) {
    final T element = getOriginalElement(psi);
    return element == null ? psi : element;
  }

  /**
   * Filters _names for strings that match given matcher and sorts them. 
   * "Start matching" items go first, then others. 
   * Within both groups names are sorted lexicographically in a case-insensitive way.
   */
  public static LinkedHashSet<String> sortMatching(final PrefixMatcher matcher, Collection<String> _names) {
    ProgressManager.checkCanceled();
    if (matcher.getPrefix().isEmpty()) {
      return ContainerUtil.newLinkedHashSet(_names);
    }

    List<String> sorted = new ArrayList<>();
    for (String name : _names) {
      if (matcher.prefixMatches(name)) {
        sorted.add(name);
      }
    }

    ProgressManager.checkCanceled();
    Collections.sort(sorted, String.CASE_INSENSITIVE_ORDER);
    ProgressManager.checkCanceled();

    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (String name : sorted) {
      if (matcher.isStartMatch(name)) {
        result.add(name);
      }
    }

    ProgressManager.checkCanceled();

    result.addAll(sorted);
    return result;
  }

  public static Iterable<String> iterateLookupStrings(@NotNull final LookupElement element) {
    return new Iterable<String>() {
      @NotNull
      @Override
      public Iterator<String> iterator() {
        final Iterator<String> original = element.getAllLookupStrings().iterator();
        return new UnmodifiableIterator<String>(original) {
          @Override
          public boolean hasNext() {
            try {
              return super.hasNext();
            }
            catch (ConcurrentModificationException e) {
              throw e;
            }
          }

          @Override
          public String next() {
            try {
              return super.next();
            }
            catch (ConcurrentModificationException e) {
              throw e;
            }
          }
        };
      }
    };
  }
}
