/**
 * Original Author -> 杨海健 (taketoday@foxmail.com) https://taketoday.cn
 * Copyright © TODAY & 2017 - 2019 All Rights Reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *   
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */
package cn.taketoday.context.io;

import java.io.File;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import cn.taketoday.context.Constant;
import cn.taketoday.context.utils.ResourceUtils;

/**
 * @author TODAY <br>
 *         2019-05-15 10:20
 * @since 2.1.6
 */
public class JarEntryResource extends UrlBasedResource implements JarResource {

    private final String name;
    private final File jarFile;

    public JarEntryResource(URL url) throws IOException {
        this(url, new File(getJarFilePath(url.getPath())), getJarEntryName(url.getPath()));
    }

    public JarEntryResource(String path) throws IOException {
        this(new URL(getJarUrl(path)), new File(getJarFilePath(path)), getJarEntryName(path));
    }

    public JarEntryResource(URL url, File jarFile, String name) throws IOException {
        super(url);
        this.name = name;
        this.jarFile = jarFile;
    }

    private static String getJarUrl(String path) {

        final StringBuilder url = new StringBuilder(256);

        if (!path.startsWith(Constant.JAR_ENTRY_URL_PREFIX)) {
            url.append(Constant.JAR_ENTRY_URL_PREFIX);
        }

        return url.append(path).toString();
    }

    private static String getJarFilePath(String path) {

        if (path.startsWith("file:/")) {
            return path.substring(6, path.indexOf(Constant.JAR_SEPARATOR));
        }
        // jar:file:/xxxxxx.jar!/x
        return path.substring(0, path.indexOf(Constant.JAR_SEPARATOR));
    }

    private static String getJarEntryName(String path) {

        if (path.charAt(0) == Constant.PATH_SEPARATOR) {
            return path.substring(1);
        }
        // jar:file:/xxxxxx.jar!/x
        return path.substring(path.indexOf(Constant.JAR_SEPARATOR) + 2);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InputStream getInputStream() throws IOException {

        final JarFile jarFile = getJarFile();

        return new JarEntryInputStream(jarFile.getInputStream(jarFile.getEntry(name)), jarFile);
    }

    @Override
    public JarOutputStream getOutputStream() throws IOException {
        return new JarOutputStream(Files.newOutputStream(getFile().toPath()));
    }

    @Override
    public File getFile() {
        return jarFile;
    }

    @Override
    public boolean exists() {
        try (final JarFile jarFile = getJarFile()) {
            return jarFile.getEntry(name) != null;
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean isDirectory() throws IOException {
        try (final JarFile jarFile = getJarFile()) {
            return jarFile.getEntry(name).isDirectory();
        }
    }

    @Override
    public String[] list() throws IOException {
        try (final JarFile jarFile = getJarFile()) {

            Set<String> result = new HashSet<>();
            final Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                final String entryName = jarEntry.getName();
                if (!entryName.equals(name) && entryName.startsWith(name)) {
                    final String substring = entryName.substring(name.length());
                    final int index = substring.indexOf(Constant.PATH_SEPARATOR);

                    if (index > -1) { // is dir
                        result.add(substring.substring(0, index));
                    }
                    else {
                        result.add(substring);
                    }
                }
            }
            return result.toArray(Constant.EMPTY_STRING_ARRAY);
        }
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        return new JarEntryResource(new URL(getLocation(), relativePath), getFile(), ResourceUtils.getRelativePath(name, relativePath));
    }

    private static class JarEntryInputStream extends FilterInputStream {

        private final JarFile jarFile;

        protected JarEntryInputStream(InputStream in, JarFile jarFile) throws IOException {
            super(in);
            this.jarFile = jarFile;
        }

        public void close() throws IOException {
            in.close();
            jarFile.close();
        }
    }

}