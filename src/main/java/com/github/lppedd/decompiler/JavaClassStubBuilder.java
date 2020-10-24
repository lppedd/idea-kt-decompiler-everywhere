package com.github.lppedd.decompiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.psi.compiled.ClsStubBuilder;
import com.intellij.psi.impl.compiled.ClsFileImpl;
import com.intellij.psi.impl.source.JavaFileElementType;
import com.intellij.psi.stubs.PsiFileStub;
import com.intellij.util.cls.ClsFormatException;
import com.intellij.util.indexing.FileContent;

/**
 * @author Edoardo Luppi
 */
class JavaClassStubBuilder extends ClsStubBuilder {
  @Override
  public int getStubVersion() {
    return JavaFileElementType.STUB_VERSION;
  }

  @Nullable
  @Override
  public PsiFileStub<?> buildFileStub(@NotNull final FileContent fileContent) throws ClsFormatException {
    return ClsFileImpl.buildFileStub(fileContent.getFile(), fileContent.getContent());
  }
}
