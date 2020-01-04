/*
 * Copyright 2019-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.ssh.sftp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Vector;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class SftpCommandTests
{
    private static final String REMOTE_PATH = "/remote/path";
    private static final String PARAM_1 = "param1";
    private static final String PARAM_2 = "param2";
    private static final String PARAM_3 = "param3";

    private void shouldNotSupportUnexpectedParameters(SftpCommand command, String exceptionMessageFormat,
            String... parameters)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> command.execute(null, List.of(parameters)));
        assertEquals(String.format(exceptionMessageFormat, command), exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = SftpCommand.class, mode = Mode.EXCLUDE, names = "PWD")
    void shouldNotSupportZeroParameters(SftpCommand command)
    {
        shouldNotSupportUnexpectedParameters(command, "Command %s requires parameter(s)");
    }

    @ParameterizedTest
    @EnumSource(value = SftpCommand.class, mode = Mode.INCLUDE, names = { "PWD", "PUT" })
    void shouldNotSupportSingleParameter(SftpCommand command)
    {
        shouldNotSupportUnexpectedParameters(command, "Command %s doesn't support single parameter", PARAM_1);
    }

    @ParameterizedTest
    @EnumSource(value = SftpCommand.class, mode = Mode.EXCLUDE, names = {"PUT", "PUT_FROM_FILE"})
    void shouldNotSupportTwoParameters(SftpCommand command)
    {
        shouldNotSupportUnexpectedParameters(command, "Command %s doesn't support two parameters",
                PARAM_1, PARAM_2);
    }

    @ParameterizedTest
    @EnumSource(SftpCommand.class)
    void shouldNotSupportThreeParameters(SftpCommand command)
    {
        shouldNotSupportUnexpectedParameters(command, "Command %s doesn't support 3 parameters",
                PARAM_1, PARAM_2, PARAM_3);
    }

    @Test
    void shouldExecuteCdCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/path";
        String result = SftpCommand.CD.execute(channel, path);
        assertNull(result);
        verify(channel).cd(path);
    }

    @Test
    void shouldExecutePwdCommand() throws SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String pwd = "~";
        when(channel.pwd()).thenReturn(pwd);
        String result = SftpCommand.PWD.execute(channel);
        assertEquals(pwd, result);
    }

    @Test
    void shouldExecuteRmCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/file-to-remove";
        String result = SftpCommand.RM.execute(channel, path);
        assertNull(result);
        verify(channel).rm(path);
    }

    @Test
    void shouldExecuteRmdirCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/dir-to-remove";
        String result = SftpCommand.RMDIR.execute(channel, path);
        assertNull(result);
        verify(channel).rmdir(path);
    }

    @Test
    void shouldExecuteMkdirCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/dir-to-create";
        String result = SftpCommand.MKDIR.execute(channel, path);
        assertNull(result);
        verify(channel).mkdir(path);
    }

    @Test
    @SuppressWarnings({ "PMD.ReplaceVectorWithList", "PMD.UseArrayListInsteadOfVector" })
    void shouldExecuteLsCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/dir-to-list";
        LsEntry lsEntry1 = mock(LsEntry.class);
        when(lsEntry1.getFilename()).thenReturn("file1.txt");
        LsEntry lsEntry2 = mock(LsEntry.class);
        when(lsEntry2.getFilename()).thenReturn("file2.story");
        Vector<LsEntry> ls = new Vector<>();
        ls.add(lsEntry1);
        ls.add(lsEntry2);
        when(channel.ls(path)).thenReturn(ls);
        String result = SftpCommand.LS.execute(channel, path);
        assertEquals("file1.txt,file2.story", result);
    }

    @Test
    void shouldExecuteGetCommand() throws IOException, SftpException
    {
        ChannelSftp channel = mock(ChannelSftp.class);
        String path = "/file-to-get";
        String data = "data";
        when(channel.get(path)).thenReturn(IOUtils.toInputStream(data, StandardCharsets.UTF_8));
        String result = SftpCommand.GET.execute(channel, path);
        assertEquals(data, result);
    }

    @Test
    void shouldExecutePutCommand() throws IOException, SftpException
    {
        String content = "content";
        ChannelSftp channel = mock(ChannelSftp.class);
        ArgumentCaptor<InputStream> inputStreamArgumentCaptor = ArgumentCaptor.forClass(InputStream.class);
        doNothing().when(channel).put(inputStreamArgumentCaptor.capture(), eq(REMOTE_PATH));
        String result = SftpCommand.PUT.execute(channel, content, REMOTE_PATH);
        assertNull(result);
        assertEquals(content, new String(inputStreamArgumentCaptor.getValue().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void shouldExecutePutFileCommand() throws IOException, SftpException
    {
        String filePath = ResourceUtils.loadFile(SftpCommand.class, "test.txt").getPath();
        ChannelSftp channel = mock(ChannelSftp.class);
        doNothing().when(channel).put((InputStream) argThat(is -> {
            try
            {
                return "Hello".equals(new String(((InputStream) is).readAllBytes(), StandardCharsets.UTF_8));
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }),
            eq(REMOTE_PATH));
        String result = SftpCommand.PUT_FROM_FILE.execute(channel, filePath, REMOTE_PATH);
        assertNull(result);
    }

    @Test
    void shouldReturnEnumFromString()
    {
        assertEquals(SftpCommand.CD, SftpCommand.fromString("cd"));
    }

    @Test
    void shouldFailWhenNoEnumValueExistsForString()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> SftpCommand.fromString("del"));
        assertEquals("There is no SFTP command with name 'del'", exception.getMessage());
    }
}
