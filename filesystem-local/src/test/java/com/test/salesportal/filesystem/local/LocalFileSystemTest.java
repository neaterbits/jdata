package com.test.salesportal.filesystem.local;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.StringUtil;
import com.test.salesportal.filesystem.api.IFileSystem.FileInput;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class LocalFileSystemTest extends TestCase {

	public void testStoreAndRetrieve() throws IOException {
		
		final File baseDir = IOUtil.makeTempFileAndDeleteOnExit("localfilesystemtest");
		
		final LocalFileSystem fileSystem = new LocalFileSystem(baseDir);
		
		final String [] path = new String [] { "path", "to", "file.txt" };
		
		final String text = "the text\n";
		
		final byte [] bytes = text.getBytes();
		
		fileSystem.storeFile(path, new ByteArrayInputStream(bytes), bytes.length);
		
		final File fsFile = new File(baseDir, StringUtil.join(path, File.separatorChar));
		assertThat(fsFile.exists()).isTrue();
		assertThat(fsFile.isFile()).isTrue();
		assertThat(fsFile.canRead()).isTrue();
		
		assertThat(IOUtil.readFileToString(fsFile)).isEqualTo(text);
		
		final FileInput fileInput = fileSystem.readFileInput(path);
		
		assertThat(fileInput).isNotNull();
		assertThat(fileInput.getLength()).isEqualTo(bytes.length);
		assertThat(IOUtil.readAllAndClose(fileInput.getInputStream())).isEqualTo(bytes);
		
		final InputStream inputStream = fileSystem.readFile(path);
		assertThat(IOUtil.readAllAndClose(inputStream)).isEqualTo(bytes);
		
		assertThat(fileSystem.exists(path)).isTrue();
		
		fileSystem.deleteFile(path);
		
		assertThat(fileSystem.exists(path)).isFalse();
		assertThat(fsFile.exists()).isFalse();
	}
	
	public void testListFiles() throws IOException {

		final File baseDir = IOUtil.makeTempFileAndDeleteOnExit("localfilesystemtest");
		
		final LocalFileSystem fileSystem = new LocalFileSystem(baseDir);
		
		final byte [] textBytes = "Test text\n".getBytes();
		
		fileSystem.storeFile(
				new String [] { "path", "to", "file.txt" },
				new ByteArrayInputStream(textBytes),
				textBytes.length);
		
		assertThat(fileSystem.listFiles(new String [] { }).length).isEqualTo(0);
		assertThat(fileSystem.listFiles(new String [] { "path" }).length).isEqualTo(0);
		assertThat(fileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(1);
		assertThat(fileSystem.listFiles(new String [] { "path", "to" })[0]).isEqualTo("file.txt");
		
		final byte [] anotherTextBytes = "Test text\n".getBytes();
		
		fileSystem.storeFile(
				new String [] { "path", "to", "anotherFile.txt" },
				new ByteArrayInputStream(anotherTextBytes),
				anotherTextBytes.length);

		assertThat(fileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(2);
		assertThat(fileSystem.listFiles(new String [] { "path", "to" })).contains("file.txt");
		assertThat(fileSystem.listFiles(new String [] { "path", "to" })).contains("anotherFile.txt");
		
		fileSystem.deleteFile(new String [] { "path", "to", "file.txt" });
		assertThat(fileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(1);
		assertThat(fileSystem.listFiles(new String [] { "path", "to" })[0]).isEqualTo("anotherFile.txt");
	}
}
