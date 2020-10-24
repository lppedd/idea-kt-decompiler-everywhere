package com.github.lppedd.decompiler;

import static com.intellij.openapi.components.ServiceManager.getService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.idea.caches.IDEKotlinBinaryClassCache;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiManager;
import com.intellij.psi.compiled.ClassFileDecompilers;
import com.intellij.psi.compiled.ClassFileDecompilers.Decompiler;
import com.intellij.psi.compiled.ClassFileDecompilers.Full;
import com.intellij.psi.compiled.ClsStubBuilder;

/**
 * Allows viewing decompiled Kotlin {@code .class} files as Java code.
 *
 * @author Edoardo Luppi
 */
class KotlinAsJavaClassFileDecompiler extends Full {
  @Override
  public boolean accepts(@NotNull final VirtualFile file) {
    return getIDEKotlinBinaryClassCache().isKotlinJvmCompiledFile(file, null);
  }

  @NotNull
  @Override
  public ClsStubBuilder getStubBuilder() {
    return new JavaClassStubBuilder();
  }

  @NotNull
  @Override
  public FileViewProvider createFileViewProvider(
      @NotNull final VirtualFile file,
      @NotNull final PsiManager manager,
      final boolean physical) {
    return new JavaClassFileViewProvider(manager, file, physical);
  }

  @NotNull
  static ExtensionPointName<Decompiler> getDecompilerEp() {
    final var service = getService(ClassFileDecompilers.class);

    if (service != null) {
      // IDEA 202.5792+
      // noinspection AccessStaticViaInstance
      return service.EP_NAME;
    }

    try {
      final var epName = ClassFileDecompilers.class.getDeclaredField("EP_NAME");
      epName.setAccessible(true);
      return (ExtensionPointName<Decompiler>) epName.get(null);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("ClassFileDecompilers.EP_NAME should be static", e);
    }
  }

  @NotNull
  private static IDEKotlinBinaryClassCache getIDEKotlinBinaryClassCache() {
    final var service = getService(IDEKotlinBinaryClassCache.class);

    if (service != null) {
      // Kotlin 1.3.60+
      return service;
    }

    try {
      // noinspection JavaReflectionMemberAccess
      final var instance = IDEKotlinBinaryClassCache.class.getDeclaredField("INSTANCE");
      instance.setAccessible(true);
      return (IDEKotlinBinaryClassCache) instance.get(null);
    } catch (final NoSuchFieldException | IllegalAccessException e) {
      throw new IllegalStateException("IDEKotlinBinaryClassCache should be a Kotlin 'object'", e);
    }
  }
}
