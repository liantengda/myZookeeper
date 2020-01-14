/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.zookeeper.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 需要的基础知识：
 * flush引用自马桶，先存储一堆水，当你需要的时候，再一顿操作，全部冲入下水道，很符合流这个形象。
 * fflush：标准I/O函数（如：fread，fwrite）会在内存建立缓冲，该函数刷新内存缓冲，将内容写入内核缓冲，要想将其写入磁盘，还需要调用fsync。（先调用fflush后调用fsync，否则不起作用）。
 * 文件输入输入流，相对于操作系统来说。所以，输入流————>操作系统内部————>输出流————>操作系统外部
 * 对于我们所操作的文件来说，是反过来的，文件————>输入流————操作系统内部————>输出流————>文件
 * A FileOutputStream that has the property that it will only show up at its
 * destination once it has been entirely written and flushed to disk. While
 * being written, it will use a .tmp suffix.
 * 这是一个有以下特性的文件输出流（文件写入流），它仅仅在所有数据已经被完全写入和刷入硬盘中后才展示出来。当它正在被写入一个文件时，将会以.tmp
 * 后缀形式文件出现。（这个和我们日常生活中看到的很相符。）
 * When the output stream is closed, it is flushed, fsynced, and will be moved
 * into place, overwriting any file that already exists at that location.
 * 当这个输出流被关闭时，他就会被刷入一下，同步到磁盘中，然后移动到目的地，覆盖在那个目录下已经存在的指定文件的内容。
 *
 * <b>NOTE</b>: on Windows platforms, it will not atomically replace the target
 * file - instead the target file is deleted before this one is moved into
 * place.
 * 在windows平台
 * 原子性文件输出流，也就是写入文件流。
 * @Reader liantengda
 */
public class AtomicFileOutputStream extends FilterOutputStream {
    private static final String TMP_EXTENSION = ".tmp";

    private final static Logger LOG = LoggerFactory
            .getLogger(AtomicFileOutputStream.class);

    private final File origFile;
    private final File tmpFile;

    public AtomicFileOutputStream(File f) throws FileNotFoundException {
        // Code unfortunately must be duplicated below since we can't assign
        // anything
        // before calling super
        super(new FileOutputStream(new File(f.getParentFile(), f.getName()
                + TMP_EXTENSION)));
        origFile = f.getAbsoluteFile();
        tmpFile = new File(f.getParentFile(), f.getName() + TMP_EXTENSION)
                .getAbsoluteFile();
    }

    /**
     * The default write method in FilterOutputStream does not call the write
     * method of its underlying input stream with the same arguments. Instead
     * it writes the data byte by byte, override it here to make it more
     * efficient.
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        boolean triedToClose = false, success = false;
        try {
            flush();
            ((FileOutputStream) out).getChannel().force(true);

            triedToClose = true;
            super.close();
            success = true;
        } finally {
            if (success) {
                boolean renamed = tmpFile.renameTo(origFile);
                if (!renamed) {
                    // On windows, renameTo does not replace.
                    if (!origFile.delete() || !tmpFile.renameTo(origFile)) {
                        throw new IOException(
                                "Could not rename temporary file " + tmpFile
                                        + " to " + origFile);
                    }
                }
            } else {
                if (!triedToClose) {
                    // If we failed when flushing, try to close it to not leak
                    // an FD
                    IOUtils.closeStream(out);
                }
                // close wasn't successful, try to delete the tmp file
                if (!tmpFile.delete()) {
                    LOG.warn("Unable to delete tmp file " + tmpFile);
                }
            }
        }
    }

    /**
     * Close the atomic file, but do not "commit" the temporary file on top of
     * the destination. This should be used if there is a failure in writing.
     */
    public void abort() {
        try {
            super.close();
        } catch (IOException ioe) {
            LOG.warn("Unable to abort file " + tmpFile, ioe);
        }
        if (!tmpFile.delete()) {
            LOG.warn("Unable to delete tmp file during abort " + tmpFile);
        }
    }
}
