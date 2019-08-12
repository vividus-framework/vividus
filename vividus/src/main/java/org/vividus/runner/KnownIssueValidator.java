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

package org.vividus.runner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.softassert.issue.IKnownIssueProvider;
import org.vividus.softassert.issue.KnownIssueChecker;
import org.vividus.softassert.model.KnownIssue;

public final class KnownIssueValidator
{
    private static final String FORMATTER = "%-12s| %s";

    private KnownIssueValidator()
    {
        // Nothing to do
    }

    public static void main(String[] args) throws ParseException, IOException
    {
        CommandLineParser parser = new DefaultParser();
        Option helpOption = new Option("h", "help", false, "print this message.");
        Option failuresListLocationOption = new Option("f", "file", true, "location of assertion failure list.");
        Options options = new Options();
        options.addOption(helpOption);
        options.addOption(failuresListLocationOption);
        CommandLine commandLine = parser.parse(options, args);

        if (commandLine.hasOption(helpOption.getOpt()))
        {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("KnownIssueValidator", options);
            return;
        }

        Vividus.init();
        KnownIssueChecker knownIssueChecker = BeanFactory.getBean(KnownIssueChecker.class);
        knownIssueChecker.setTestInfoProvider(null);
        IKnownIssueProvider knownIssueProvider = BeanFactory.getBean(IKnownIssueProvider.class);
        String failuresListPath = commandLine.getOptionValue(failuresListLocationOption.getOpt());
        if (failuresListPath == null)
        {
            Set<String> keySet = knownIssueProvider.getKnownIssueIdentifiers().keySet();
            if (!keySet.isEmpty())
            {
                System.out.println("Known issues found:");
                keySet.forEach(System.out::println);
            }
            return;
        }

        File file = new File(failuresListPath);

        try (FileInputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)))
        {
            print("Known Issue", "Assertion Error");
            String line;
            while ((line = reader.readLine()) != null)
            {
                KnownIssue knownIssue = knownIssueChecker.getKnownIssue(line);
                print(knownIssue != null ? knownIssue.getIdentifier() : "", line);
            }
        }
    }

    private static void print(String knownIssueId, String assertionError)
    {
        System.out.println(String.format(FORMATTER, knownIssueId, assertionError));
    }
}
