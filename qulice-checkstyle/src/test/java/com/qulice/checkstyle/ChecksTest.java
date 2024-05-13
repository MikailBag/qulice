/*
 * Copyright (c) 2011-2024 Qulice.com
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the Qulice.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.qulice.checkstyle;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.EqualsAndHashCode;
import org.apache.commons.io.IOUtils;
import org.cactoos.text.Joined;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.InputSource;

/**
 * Integration test case for all checkstyle checks.
 * @since 0.3
 */
final class ChecksTest {

    /**
     * Test checkstyle for true positive.
     * @param dir Directory where test scripts are located.
     * @throws Exception If something goes wrong
     */
    @ParameterizedTest
    @MethodSource("checks")
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testCheckstyleTruePositive(final String dir) throws Exception {
        final AuditListener listener = Mockito.mock(AuditListener.class);
        final Collector collector = new ChecksTest.Collector();
        Mockito.doAnswer(collector).when(listener)
            .addError(Mockito.any(AuditEvent.class));
        this.check(dir, "/Invalid.java", listener);
        final String[] violations = IOUtils.toString(
            this.getClass().getResourceAsStream(
                String.format("%s/violations.txt", dir)
            ),
            StandardCharsets.UTF_8
            ).split("\n");
        final List<Matcher<? super SimpleAuditEvent>> expected = Arrays
                .stream(violations)
                .map(line -> {
                    final String[] sectors = line.split(":");
                    final int pos = Integer.parseInt(sectors[0]);
                    final String needle = sectors[1].trim();
                    return new SimpleAuditEvent(pos, needle);
                })
                .map(Matchers::equalTo)
                .collect(Collectors.toList());

        MatcherAssert.assertThat(
                "Reported violations don't match violations.txt: " + collector.summary(),
                collector.events(),
                Matchers.containsInAnyOrder(expected)
        );
    }

    /**
     * Test checkstyle for true negative.
     * @param dir Directory where test scripts are located.
     * @throws Exception If something goes wrong
     */
    @ParameterizedTest
    @MethodSource("checks")
    void testCheckstyleTrueNegative(final String dir) throws Exception {
        final AuditListener listener = Mockito.mock(AuditListener.class);
        final Collector collector = new ChecksTest.Collector();
        Mockito.doAnswer(collector).when(listener)
            .addError(Mockito.any(AuditEvent.class));
        this.check(dir, "/Valid.java", listener);
        MatcherAssert.assertThat(
            "Log should be empty for valid files",
            collector.summary(),
            Matchers.equalTo("")
        );
        Mockito.verify(listener, Mockito.times(0))
            .addError(Mockito.any(AuditEvent.class));
    }

    /**
     * Check one file.
     * @param dir Directory where test scripts are located.
     * @param name The name of the check
     * @param listener The listener
     * @throws Exception If something goes wrong inside
     */
    private void check(
        final String dir, final String name, final AuditListener listener
    ) throws Exception {
        final Checker checker = new Checker();
        final InputSource src = new InputSource(
            this.getClass().getResourceAsStream(
                String.format("%s/config.xml", dir)
            )
        );
        checker.setModuleClassLoader(
            Thread.currentThread().getContextClassLoader()
        );
        checker.configure(
            ConfigurationLoader.loadConfiguration(
                src,
                new PropertiesExpander(new Properties()),
                ConfigurationLoader.IgnoredModulesOptions.OMIT
            )
        );
        final List<File> files = new ArrayList<>(0);
        files.add(
            new File(
                this.getClass().getResource(
                    String.format("%s%s", dir, name)
                ).getFile()
            )
        );
        checker.addListener(listener);
        checker.process(files);
        checker.destroy();
    }

    /**
     * Returns full list of checks.
     * @return The list
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private static Stream<String> checks() {
        return Stream.of(
            "MethodsOrderCheck",
            "MultilineJavadocTagsCheck",
            "StringLiteralsConcatenationCheck",
            "EmptyLinesCheck",
            "ImportCohesionCheck",
            "BracketsStructureCheck",
            "CurlyBracketsStructureCheck",
            "JavadocLocationCheck",
            "JavadocParameterOrderCheck",
            "MethodBodyCommentsCheck",
            "RequireThisCheck",
            "ProtectedMethodInFinalClassCheck",
            "NoJavadocForOverriddenMethodsCheck",
            "NonStaticMethodCheck",
            "ConstantUsageCheck",
            "JavadocEmptyLineCheck",
            "JavadocTagsCheck",
            "ProhibitNonFinalClassesCheck",
            "QualifyInnerClassCheck"
        ).map(s -> String.format("ChecksTest/%s", s));
    }


    /**
     * Simplified version of AuditEvent class with nicer toString
     */
    @EqualsAndHashCode
    private static final class SimpleAuditEvent {
        private final int line;
        private final String message;
        SimpleAuditEvent(int line, String message) {
            this.line = line;
            this.message = message;
        }

        @Override
        public String toString() {
            return String.format("%d:%s", line, message);
        }

        int line() {
            return line;
        }

        String message() {
            return message;
        }
    }

    /**
     * Mocked collector of checkstyle events.
     *
     * @since 0.1
     */
    private static final class Collector implements Answer<Object> {

        /**
         * List of events received.
         */
        private final List<SimpleAuditEvent> events = new LinkedList<>();

        @Override
        public Object answer(final InvocationOnMock invocation) {
            final AuditEvent event = (AuditEvent) invocation.getArguments()[0];
            this.events.add(new SimpleAuditEvent(event.getLine(), event.getMessage()));
            return null;
        }

        /**
         * Returns all events recorded so far.
         * @return Collection of events
         */
        public Collection<SimpleAuditEvent> events() {
            return Collections.unmodifiableCollection(this.events);
        }

        /**
         * Returns full summary.
         * @return The test summary of all events
         */
        public String summary() {
            final List<String> msgs = this.events.stream().map(Object::toString).collect(Collectors.toList());
            return new Joined("; ", msgs).toString();
        }
    }
}
