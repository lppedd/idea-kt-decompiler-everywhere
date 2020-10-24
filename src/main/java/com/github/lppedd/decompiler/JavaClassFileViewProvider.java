package com.github.lppedd.decompiler;

import static com.github.lppedd.decompiler.KotlinAsJavaClassFileDecompiler.getDecompilerEp;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.java.decompiler.IdeaDecompiler;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.ClassFileViewProvider;
import com.intellij.psi.PsiManager;

/**
 * @author Edoardo Luppi
 */
class JavaClassFileViewProvider extends ClassFileViewProvider {
  JavaClassFileViewProvider(
      @NotNull final PsiManager manager,
      @NotNull final VirtualFile file,
      final boolean eventSystemEnabled) {
    super(manager, file, eventSystemEnabled);
  }

  @NotNull
  @Override
  public CharSequence getContents() {
    return decompile(getVirtualFile());
  }

  @NotNull
  private CharSequence decompile(@NotNull final VirtualFile file) {
    final var decompiler = getDecompilerEp().findExtensionOrFail(IdeaDecompiler.class);

    try {
      final var decompile = IdeaDecompiler.class.getDeclaredMethod("decompile", VirtualFile.class);
      decompile.setAccessible(true);
      return (CharSequence) decompile.invoke(decompiler, file);
    } catch (final NoSuchMethodException e) {
      throw new IllegalStateException("IdeaDecompiler should have a 'decompile(VirtualFile)' method");
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
