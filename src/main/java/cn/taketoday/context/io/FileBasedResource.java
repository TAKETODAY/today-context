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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import cn.taketoday.context.utils.ResourceUtils;
import cn.taketoday.context.utils.StringUtils;

/**
 * @author TODAY <br>
 *         2019-05-14 22:50
 * @since 2.1.6
 */
public class FileBasedResource extends AbstractResource implements WritableResource {

    private final File file;
    private final String path;
    private final Path filePath;

    public FileBasedResource(String path) {
        this.path = StringUtils.cleanPath(path);
        this.file = new File(path);
        this.filePath = this.file.toPath();
    }

    public FileBasedResource(File file) {
        this.path = StringUtils.cleanPath(file.getPath());
        this.file = file;
        this.filePath = file.toPath();
    }

    public FileBasedResource(Path filePath) {
        this.path = StringUtils.cleanPath(filePath.toString());
        this.file = null;
        this.filePath = filePath;
    }

    /**
     * Return the file path for this resource.
     */
    public final String getPath() {
        return this.path;
    }

    /**
     * This implementation returns whether the underlying file exists.
     * 
     */
    @Override
    public boolean exists() {
        return (this.file != null ? this.file.exists() : Files.exists(this.filePath));
    }

    /**
     * This implementation opens a NIO file stream for the underlying file.
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return Files.newInputStream(this.filePath);
    }

    /**
     * This implementation opens a FileOutputStream for the underlying file.
     * 
     * @see java.io.FileOutputStream
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        return Files.newOutputStream(this.filePath);
    }

    /**
     * This implementation returns a URL for the underlying file.
     */
    @Override
    public URL getLocation() throws IOException {
        return (this.file != null ? this.file.toURI().toURL() : this.filePath.toUri().toURL());
    }

    /**
     * This implementation returns the underlying File reference.
     */
    @Override
    public File getFile() {
        return (this.file != null ? this.file : this.filePath.toFile());
    }

    /**
     * This implementation returns the underlying File/Path length.
     */
    @Override
    public long contentLength() throws IOException {
        if (this.file != null) {
            long length = this.file.length();
            if (length == 0L && !this.file.exists()) {
                throw new FileNotFoundException(getName() + " cannot be resolved its content length");
            }
            return length;
        }
        try {
            return Files.size(this.filePath);
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    /**
     * This implementation returns the underlying File/Path last-modified time.
     */
    @Override
    public long lastModified() throws IOException {
        if (this.file != null) {
            return super.lastModified();
        }
        try {
            return Files.getLastModifiedTime(this.filePath).toMillis();
        }
        catch (NoSuchFileException ex) {
            throw new FileNotFoundException(ex.getMessage());
        }
    }

    @Override
    public Resource createRelative(String relativePath) throws IOException {
        final String pathToUse = ResourceUtils.getRelativePath(path, relativePath);
        return (this.file != null //
                ? new FileBasedResource(pathToUse) //
                : new FileBasedResource(this.filePath.getFileSystem().getPath(pathToUse).normalize()));
    }

    @Override
    public String getName() {
        if (file != null) {
            return file.getName();
        }
        if (filePath != null) {
            final Path fileName = filePath.getFileName();
            if (fileName != null) {
                return fileName.toString();
            }
        }
        return new File(path).getName();
    }

    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof FileBasedResource && this.path.equals(((FileBasedResource) other).path)));
    }

    @Override
    public int hashCode() {
        return this.path.hashCode();
    }

}
