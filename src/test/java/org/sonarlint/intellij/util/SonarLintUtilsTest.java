/**
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.util;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.junit.Before;
import org.junit.Test;
import org.sonarlint.intellij.SonarApplication;
import org.sonarlint.intellij.SonarTest;
import org.sonarlint.intellij.config.global.SonarQubeServer;
import org.sonarsource.sonarlint.core.client.api.connected.ServerConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SonarLintUtilsTest extends SonarTest {
  private VirtualFile testFile;

  private FileType binary;
  private FileType notBinary;

  @Before
  public void setUp() {
    super.setUp();
    testFile = mock(VirtualFile.class);

    notBinary = mock(FileType.class);
    binary = mock(FileType.class);
    when(binary.isBinary()).thenReturn(true);

    when(testFile.getParent()).thenReturn(mock(VirtualFile.class));
    when(testFile.isInLocalFileSystem()).thenReturn(true);
    when(testFile.isValid()).thenReturn(true);
    when(testFile.isInLocalFileSystem()).thenReturn(true);
    when(testFile.getFileType()).thenReturn(notBinary);
  }

  @Test
  public void testShouldAnalyze() {
    assertThat(SonarLintUtils.shouldAnalyze(testFile, module)).isTrue();

    assertThat(SonarLintUtils.shouldAnalyze(null, module)).isFalse();
    assertThat(SonarLintUtils.shouldAnalyze(testFile, null)).isFalse();

    when(testFile.getFileType()).thenReturn(binary);
    assertThat(SonarLintUtils.shouldAnalyze(testFile, module)).isFalse();
  }

  @Test
  public void testGetModuleRoot() {
    assertThat(SonarLintUtils.getModuleRootPath(module)).isEqualTo("/src");
  }

  @Test
  public void testShouldAnalyzeDisposed() {
    Project disposed = mock(Project.class);
    Module module = mock(Module.class);

    when(disposed.isDisposed()).thenReturn(true);
    when(module.getProject()).thenReturn(disposed);

    assertThat(SonarLintUtils.shouldAnalyze(testFile, module)).isFalse();
  }

  @Test
  public void testShouldAnalyzeInvalid() {
    VirtualFile f = mock(VirtualFile.class);
    when(f.isValid()).thenReturn(false);

    assertThat(SonarLintUtils.shouldAnalyze(f, module)).isFalse();
  }

  @Test
  public void testServerConfigurationToken() {
    SonarApplication app = mock(SonarApplication.class);
    when(app.getVersion()).thenReturn("1.0");
    super.register(ApplicationManager.getApplication(), SonarApplication.class, app);

    SonarQubeServer server = new SonarQubeServer();
    server.setHostUrl("http://myhost");
    server.setEnableProxy(false);
    server.setToken("token");
    ServerConfiguration config = SonarLintUtils.getServerConfiguration(server);
    assertThat(config.getLogin()).isEqualTo(server.getToken());
    assertThat(config.getPassword()).isNull();
    assertThat(config.getConnectTimeoutMs()).isEqualTo(SonarLintUtils.CONNECTION_TIMEOUT_MS);
    assertThat(config.getReadTimeoutMs()).isEqualTo(SonarLintUtils.CONNECTION_TIMEOUT_MS);
    assertThat(config.getUserAgent()).contains("SonarLint");
    assertThat(config.getUrl()).isEqualTo(server.getHostUrl());
  }

  @Test
  public void testServerConfigurationPassword() {
    SonarApplication app = mock(SonarApplication.class);
    when(app.getVersion()).thenReturn("1.0");
    super.register(ApplicationManager.getApplication(), SonarApplication.class, app);

    SonarQubeServer server = new SonarQubeServer();
    server.setHostUrl("http://myhost");
    server.setLogin("token");
    server.setPassword("pass");
    ServerConfiguration config = SonarLintUtils.getServerConfiguration(server);
    assertThat(config.getLogin()).isEqualTo(server.getLogin());
    assertThat(config.getPassword()).isEqualTo(server.getPassword());
  }
}
