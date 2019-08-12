/*
 * Copyright 2019 the original author or authors.
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.SftpException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.EnumUtils;

public enum SftpCommand
{
    CD
    {
        @Override
        protected String execute(ChannelSftp channel, String path) throws SftpException
        {
            channel.cd(path);
            return null;
        }
    },
    PWD
    {
        @Override
        protected String execute(ChannelSftp channel) throws SftpException
        {
            return channel.pwd();
        }
    },
    RM
    {
        @Override
        protected String execute(ChannelSftp channel, String path) throws SftpException
        {
            channel.rm(path);
            return null;
        }
    },
    RMDIR
    {
        @Override
        protected String execute(ChannelSftp channel, String path) throws SftpException
        {
            channel.rmdir(path);
            return null;
        }
    },
    MKDIR
    {
        @Override
        protected String execute(ChannelSftp channel, String path) throws SftpException
        {
            channel.mkdir(path);
            return null;
        }
    },
    LS
    {
        @SuppressWarnings("unchecked")
        @Override
        protected String execute(ChannelSftp channel, String path) throws SftpException
        {
            return ((Stream<Object>) channel.ls(path).stream())
                    .map(LsEntry.class::cast)
                    .map(LsEntry::getFilename)
                    .collect(Collectors.joining(","));
        }
    },
    GET
    {
        @Override
        protected String execute(ChannelSftp channel, String src) throws SftpException, IOException
        {
            try (InputStream is = channel.get(src))
            {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    },
    PUT
    {
        @Override
        protected String execute(ChannelSftp channel, String content, String destination)
                throws SftpException, IOException
        {
            try (InputStream is = IOUtils.toInputStream(content, StandardCharsets.UTF_8))
            {
                channel.put(is, destination);
            }
            return null;
        }
    };

    protected String execute(ChannelSftp channel, String parameter1, String parameter2)
            throws SftpException, IOException
    {
        throw new IllegalArgumentException(String.format("Command %s doesn't support two parameters", name()));
    }

    protected String execute(ChannelSftp channel, String parameter) throws SftpException, IOException
    {
        throw new IllegalArgumentException(String.format("Command %s doesn't support single parameter", name()));
    }

    protected String execute(ChannelSftp channel) throws SftpException
    {
        throw new IllegalArgumentException(String.format("Command %s requires parameter(s)", name()));
    }

    public String execute(ChannelSftp channel, List<String> parameters) throws SftpException, IOException
    {
        int numberOfParameters = parameters.size();
        switch (numberOfParameters)
        {
            case 0:
                return execute(channel);
            case 1:
                return execute(channel, parameters.get(0));
            case 2:
                return execute(channel, parameters.get(0), parameters.get(1));
            default:
                throw new IllegalArgumentException(
                        String.format("Command %s doesn't support %d parameters", name(), numberOfParameters));
        }
    }

    public static SftpCommand fromString(String text)
    {
        SftpCommand command = EnumUtils.getEnumIgnoreCase(SftpCommand.class, text);
        if (command == null)
        {
            throw new IllegalArgumentException(String.format("There is no SFTP command with name '%s'", text));
        }
        return command;
    }
}
