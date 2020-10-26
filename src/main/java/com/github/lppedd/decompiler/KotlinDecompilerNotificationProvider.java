package com.github.lppedd.decompiler;

import static com.intellij.codeEditor.JavaEditorFileSwapper.findSourceFile;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.idea.internal.KotlinDecompilerAdapterKt;
import org.jetbrains.kotlin.psi.KtFile;

import com.intellij.ide.highlighter.JavaClassFileType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LibraryOrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex.SERVICE;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotifications.Provider;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.panels.HorizontalLayout;
import com.intellij.ui.components.panels.NonOpaquePanel;
import com.intellij.ui.scale.JBUIScale;
import com.intellij.util.SmartList;
import com.intellij.util.ui.JBUI.Borders;

/**
 * Provides an option to decompile a Kotlin {@code .class} file
 * to its Java representation even when the IDE doesn't offer that itself.
 *
 * @author Edoardo Luppi
 */
class KotlinDecompilerNotificationProvider extends Provider<EditorNotificationPanel> {
  private static final Key<EditorNotificationPanel> KEY = Key.create("Decompile Kotlin to Java");

  @Override
  @NotNull
  public Key<EditorNotificationPanel> getKey() {
    return KEY;
  }

  @Override
  @Nullable
  public EditorNotificationPanel createNotificationPanel(
      @NotNull final VirtualFile file,
      @NotNull final FileEditor fileEditor,
      @NotNull final Project project) {
    if (file.getFileType() != JavaClassFileType.INSTANCE ||
        findSourceFile(project, file) != null ||
        findLibraryEntriesForFile(file, project) != null) {
      return null;
    }

    final var psiFile = PsiManager.getInstance(project).findFile(file);

    if (!(psiFile instanceof KtFile)) {
      return null;
    }

    final var panel = new EditorNotificationPanel();
    final var decompileToJava = panel.createActionLabel(
        KotlinDecompilerBundle.message("action.decompile.java.name"),
        new DecompileActionHandler((KtFile) psiFile)
    );

    final var actionsPanel = new NonOpaquePanel(new HorizontalLayout(JBUIScale.scale(16)));
    actionsPanel.add(decompileToJava);

    final var iconLabel = new JBLabel();
    iconLabel.setIcon(KotlinDecompilerIcons.JAVA);

    final var mainPanel = (JPanel) panel.getComponent(0);
    mainPanel.removeAll();
    mainPanel.add(BorderLayout.WEST, iconLabel);
    mainPanel.add(BorderLayout.CENTER, actionsPanel);

    panel.removeAll();
    panel.add(BorderLayout.CENTER, mainPanel);
    panel.setBorder(Borders.empty(0, 7, 0, 10));

    return panel;
  }

  @Nullable
  private static List<LibraryOrderEntry> findLibraryEntriesForFile(
      @NotNull final VirtualFile file,
      @NotNull final Project project) {
    List<LibraryOrderEntry> entries = null;
    final var index = SERVICE.getInstance(project);

    for (final var entry : index.getOrderEntriesForFile(file)) {
      if (entry instanceof LibraryOrderEntry) {
        if (entries == null) {
          entries = new SmartList<>();
        }

        entries.add((LibraryOrderEntry) entry);
      }
    }

    return entries;
  }

  private static class DecompileActionHandler implements Runnable {
    private final KtFile ktFile;

    DecompileActionHandler(final KtFile ktFile) {
      this.ktFile = ktFile;
    }

    @Override
    public void run() {
      KotlinDecompilerAdapterKt.showDecompiledCode(ktFile);
    }
  }
}
