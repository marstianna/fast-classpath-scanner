/*
 * This file is part of FastClasspathScanner.
 * 
 * Author: Luke Hutchison
 * 
 * Hosted at: https://github.com/lukehutch/fast-classpath-scanner
 * 
 * --
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Luke Hutchison
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.lukehutch.fastclasspathscanner.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.StrictAssertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.FileMatchContentsProcessorWithContext;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.StaticFinalFieldMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubclassMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.matchprocessor.SubinterfaceMatchProcessor;
import io.github.lukehutch.fastclasspathscanner.test.blacklisted.BlacklistedAnnotation;
import io.github.lukehutch.fastclasspathscanner.test.blacklisted.BlacklistedInterface;
import io.github.lukehutch.fastclasspathscanner.test.blacklisted.BlacklistedSubclass;
import io.github.lukehutch.fastclasspathscanner.test.blacklisted.BlacklistedSubinterface;
import io.github.lukehutch.fastclasspathscanner.test.blacklisted.BlacklistedSuperclass;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Cls;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.ClsSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.ClsSubSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls1;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls2;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls3;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls4;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls5;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls6;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.HasFieldWithTypeCls.HasFieldWithTypeCls7;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Iface;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.IfaceSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.IfaceSubSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl1;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl1Sub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl1SubSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl2;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl2Sub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Impl2SubSub;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.StaticField;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.Whitelisted;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.WhitelistedInterface;
import io.github.lukehutch.fastclasspathscanner.test.whitelisted.blacklistedsub.BlacklistedSub;
import io.github.lukehutch.fastclasspathscanner.utils.HashClassfileContents;

public class FastClasspathScannerTest {
    private static final String ROOT_PACKAGE = FastClasspathScannerTest.class.getPackage().getName();
    private static final String WHITELIST_PACKAGE = Whitelisted.class.getPackage().getName();

    @Test
    public void scan() throws Exception {
        final List<String> allClasses = new FastClasspathScanner().scan().getNamesOfAllClasses();
        assertThat(allClasses).contains(Cls.class.getName());
        assertThat(allClasses).contains(FastClasspathScanner.class.getName());
        assertThat(allClasses).contains(FastClasspathScannerTest.class.getName());
        assertThat(allClasses).doesNotContain(String.class.getName());
        assertThat(allClasses).contains(BlacklistedSub.class.getName());
    }

    @Test
    public void scanWithWhitelist() throws Exception {
        final List<String> allClasses = new FastClasspathScanner(WHITELIST_PACKAGE).scan().getNamesOfAllClasses();
        assertThat(allClasses).contains(Cls.class.getName());
        assertThat(allClasses).doesNotContain(FastClasspathScanner.class.getName());
        assertThat(allClasses).doesNotContain(FastClasspathScannerTest.class.getName());
        assertThat(allClasses).doesNotContain(String.class.getName());
        assertThat(allClasses).contains(BlacklistedSub.class.getName());
    }

    @Test
    public void scanWithWhitelistAndBlacklist() throws Exception {
        final List<String> allClasses = new FastClasspathScanner(WHITELIST_PACKAGE,
                "-" + BlacklistedSub.class.getPackage().getName()).scan().getNamesOfAllClasses();
        assertThat(allClasses).contains(Cls.class.getName());
        assertThat(allClasses).doesNotContain(FastClasspathScanner.class.getName());
        assertThat(allClasses).doesNotContain(FastClasspathScannerTest.class.getName());
        assertThat(allClasses).doesNotContain(String.class.getName());
        assertThat(allClasses).doesNotContain(BlacklistedSub.class.getName());
    }

    @Test
    public void scanSubAndSuperclass() throws Exception {
        final HashSet<String> subclasses = new HashSet<>();
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE)
                .matchSubclassesOf(Cls.class, new SubclassMatchProcessor<Cls>() {
                    @Override
                    public void processMatch(final Class<? extends Cls> matchingClass) {
                        subclasses.add(matchingClass.getName());
                    }
                }).scan();
        assertThat(subclasses).doesNotContain(Cls.class.getName());
        assertThat(subclasses).contains(ClsSub.class.getName());
        assertThat(subclasses).contains(ClsSubSub.class.getName());
        assertThat(scanner.getNamesOfSubclassesOf(Cls.class)).doesNotContain(Cls.class.getName());
        assertThat(scanner.getNamesOfSubclassesOf(Cls.class)).contains(ClsSub.class.getName());
        assertThat(scanner.getNamesOfSubclassesOf(Cls.class)).contains(ClsSubSub.class.getName());
        assertThat(scanner.getNamesOfSuperclassesOf(ClsSubSub.class)).doesNotContain(ClsSubSub.class.getName());
        assertThat(scanner.getNamesOfSuperclassesOf(ClsSubSub.class)).contains(ClsSub.class.getName());
        assertThat(scanner.getNamesOfSuperclassesOf(ClsSubSub.class)).contains(Cls.class.getName());
    }

    @Test
    public void scanSubAndSuperinterface() throws Exception {
        final HashSet<String> subinterfaces = new HashSet<>();
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE)
                .matchSubinterfacesOf(Iface.class, new SubinterfaceMatchProcessor<Iface>() {
                    @Override
                    public void processMatch(final Class<? extends Iface> matchingInterface) {
                        subinterfaces.add(matchingInterface.getName());
                    }
                }).scan();
        assertThat(subinterfaces).doesNotContain(Iface.class.getName());
        assertThat(subinterfaces).contains(IfaceSub.class.getName());
        assertThat(subinterfaces).contains(IfaceSubSub.class.getName());
        assertThat(scanner.getNamesOfSubinterfacesOf(Iface.class)).doesNotContain(Iface.class.getName());
        assertThat(scanner.getNamesOfSubinterfacesOf(Iface.class)).contains(IfaceSub.class.getName());
        assertThat(scanner.getNamesOfSubinterfacesOf(Iface.class)).contains(IfaceSubSub.class.getName());
        assertThat(scanner.getNamesOfSuperinterfacesOf(IfaceSubSub.class))
                .doesNotContain(IfaceSubSub.class.getName());
        assertThat(scanner.getNamesOfSuperinterfacesOf(IfaceSubSub.class)).contains(IfaceSub.class.getName());
        assertThat(scanner.getNamesOfSuperinterfacesOf(IfaceSubSub.class)).contains(Iface.class.getName());
    }

    @Test
    public void scanTransitiveImplements() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE).scan();
        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).doesNotContain(Iface.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).doesNotContain(Cls.class.getName());

        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl1.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).contains(Impl1.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).contains(Impl1.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl1Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).contains(Impl1Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).contains(Impl1Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl1SubSub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).contains(Impl1SubSub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).contains(Impl1SubSub.class.getName());

        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl2.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).doesNotContain(Impl2.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).doesNotContain(Impl2.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl2Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).doesNotContain(Impl2Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class))
                .doesNotContain(Impl2Sub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(Iface.class)).contains(Impl2SubSub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSub.class)).contains(Impl2SubSub.class.getName());
        assertThat(scanner.getNamesOfClassesImplementing(IfaceSubSub.class)).contains(Impl2SubSub.class.getName());
    }

    @Test
    public void testWhitelistedWithoutException() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSubclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSuperinterfacesOf(WhitelistedInterface.class)).isEmpty();
        assertThat(scanner.getNamesOfSubinterfacesOf(WhitelistedInterface.class)).isEmpty();
    }

    public void testCanQueryWithBlacklistedAnnotation() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfClassesWithAnnotation(BlacklistedAnnotation.class))
                .containsExactly(Whitelisted.class.getName());
    }

    @Test
    public void testBlacklistedPlaceholderNotReturned() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(ROOT_PACKAGE,
                "-" + BlacklistedAnnotation.class.getPackage().getName()).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSubclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSuperinterfacesOf(WhitelistedInterface.class)).isEmpty();
        assertThat(scanner.getNamesOfSubinterfacesOf(WhitelistedInterface.class)).isEmpty();
        assertThat(scanner.getNamesOfAnnotationsOnClass(WhitelistedInterface.class)).isEmpty();
    }

    @Test
    public void testBlacklistedPackageOverridesWhitelistedClassWithWhitelistedOverrideReturned() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(ROOT_PACKAGE,
                "-" + BlacklistedAnnotation.class.getPackage().getName(), BlacklistedAnnotation.class.getName())
                        .scan();
        assertThat(scanner.getNamesOfAnnotationsOnClass(Whitelisted.class)).isEmpty();
    }

    @Test
    public void testNonWhitelistedAnnotationNotReturned() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE).scan();
        assertThat(scanner.getNamesOfAnnotationsOnClass(Whitelisted.class)).isEmpty();
    }

    public void testBlacklistedPackage() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(ROOT_PACKAGE,
                "-" + BlacklistedSuperclass.class.getPackage().getName()).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSubclassesOf(Whitelisted.class)).isEmpty();
        assertThat(scanner.getNamesOfSuperinterfacesOf(WhitelistedInterface.class)).isEmpty();
        assertThat(scanner.getNamesOfSubinterfacesOf(WhitelistedInterface.class)).isEmpty();
        assertThat(scanner.getNamesOfClassesWithAnnotation(BlacklistedAnnotation.class));
    }

    public void testNoExceptionIfQueryingBlacklisted() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE,
                "-" + BlacklistedSuperclass.class.getPackage().getName()).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(BlacklistedSuperclass.class)).isEmpty();
    }

    public void testNoExceptionIfExplicitlyWhitelistedClassInBlacklistedPackage() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(WHITELIST_PACKAGE,
                "-" + BlacklistedSuperclass.class.getPackage().getName() + BlacklistedSuperclass.class.getName())
                        .scan();
        assertThat(scanner.getNamesOfSuperclassesOf(BlacklistedSuperclass.class)).isEmpty();
    }

    @Test
    public void testVisibleIfNotBlacklisted() throws Exception {
        final FastClasspathScanner scanner = new FastClasspathScanner(ROOT_PACKAGE).scan();
        assertThat(scanner.getNamesOfSuperclassesOf(Whitelisted.class))
                .containsExactly(BlacklistedSuperclass.class.getName());
        assertThat(scanner.getNamesOfSubclassesOf(Whitelisted.class))
                .containsExactly(BlacklistedSubclass.class.getName());
        assertThat(scanner.getNamesOfSuperinterfacesOf(WhitelistedInterface.class))
                .containsExactly(BlacklistedInterface.class.getName());
        assertThat(scanner.getNamesOfSubinterfacesOf(WhitelistedInterface.class))
                .containsExactly(BlacklistedSubinterface.class.getName());
        assertThat(scanner.getNamesOfClassesWithAnnotation(BlacklistedAnnotation.class))
                .containsExactly(Whitelisted.class.getName());
    }

    @Test
    public void scanFilePattern() throws Exception {
        final AtomicBoolean readFileContents = new AtomicBoolean(false);
        new FastClasspathScanner()
                .matchFilenamePattern("[[^/]*/]*file-content-test\\.txt", new FileMatchContentsProcessor() {
                    @Override
                    public void processMatch(final String relativePath, final byte[] contents) throws IOException {
                        readFileContents.set("File contents".equals(new String(contents, "UTF-8")));
                    }
                }).scan();
        assertThat(readFileContents.get()).isTrue();
    }

    @Test
    public void scanFilePatternWithContext() throws Exception {
        new FastClasspathScanner().matchFilenamePattern("[[^/]*/]*file-content-test\\.txt",
                new FileMatchContentsProcessorWithContext() {
                    @Override
                    public void processMatch(final File classpathElt, final String relativePath,
                            final byte[] contents) throws IOException {
                        assertThat(classpathElt.getPath()).isNotEmpty();
                    }
                }).scan();
    }

    @Test
    public void scanStaticFinalFieldName() throws Exception {
        final AtomicInteger readStaticFieldCount = new AtomicInteger(0);
        final HashSet<String> fieldNames = new HashSet<String>();
        for (final String fieldName : new String[] { "stringField", "intField", "boolField", "charField",
                "integerField", "booleanField" }) {
            fieldNames.add(StaticField.class.getName() + "." + fieldName);
        }
        new FastClasspathScanner(WHITELIST_PACKAGE)
                .matchStaticFinalFieldNames(fieldNames, new StaticFinalFieldMatchProcessor() {
                    @Override
                    public void processMatch(final String className, final String fieldName,
                            final Object fieldConstantValue) {
                        switch (fieldName) {
                        case "stringField":
                            assertThat("Static field contents").isEqualTo(fieldConstantValue);
                            break;
                        case "intField":
                            assertThat(new Integer(3)).isEqualTo(fieldConstantValue);
                            break;
                        case "boolField":
                            assertThat(new Boolean(true)).isEqualTo(fieldConstantValue);
                            break;
                        case "charField":
                            assertThat(new Character('y')).isEqualTo(fieldConstantValue);
                            break;
                        case "integerField":
                        case "booleanField":
                            throw new RuntimeException("Non-constant field should not be matched");
                        default:
                            throw new RuntimeException("Unknown field");
                        }
                        readStaticFieldCount.incrementAndGet();
                    }
                }).scan();
        assertThat(readStaticFieldCount.get()).isEqualTo(4);
    }

    @Test
    public void hashContents() throws Exception {
        final Map<String, String> classNameToClassfileHash = new HashClassfileContents(WHITELIST_PACKAGE).scan()
                .getClassNameToClassfileHash();
        final String hash = classNameToClassfileHash.get(Cls.class.getName());
        assertThat(hash).isNotNull();
        assertThat(hash.length()).isEqualTo(32);
    }

    @Test
    public void generateGraphVizFile() {
        assertThat(new FastClasspathScanner(ROOT_PACKAGE).scan().generateClassGraphDotFile(20, 20))
                .contains("\"io.github.lukehutch.fastclasspathscanner.test.whitelisted.\\nClsSub\" "
                        + "-> \"io.github.lukehutch.fastclasspathscanner.test.whitelisted.\\nCls\"");
    }

    @Test
    public void hasFieldWithRequestedType() {
        assertThat(
                new FastClasspathScanner(ROOT_PACKAGE).scan().getNamesOfClassesWithFieldOfType(Cls.class.getName()))
                        .containsOnly(HasFieldWithTypeCls1.class.getName(), HasFieldWithTypeCls2.class.getName(),
                                HasFieldWithTypeCls3.class.getName(), HasFieldWithTypeCls4.class.getName(),
                                HasFieldWithTypeCls5.class.getName(), HasFieldWithTypeCls6.class.getName(),
                                HasFieldWithTypeCls7.class.getName());
    }
}
