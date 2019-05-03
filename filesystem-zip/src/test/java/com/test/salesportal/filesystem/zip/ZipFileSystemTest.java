package com.test.salesportal.filesystem.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.StringUtil;
import com.test.salesportal.filesystem.api.IFileSystem.FileInput;
import com.test.salesportal.filesystem.local.LocalFileSystem;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

public class ZipFileSystemTest extends TestCase {

	private final String [] zipFilePath = new String [] { "test", "testfile.zip" };
	private File baseDir;
	
	private File zipFilePathFile;
	private LocalFileSystem localFileSystem;
	private ZipFileSystem zipFileSystem;
	
	private void prepareTestZipFile() throws IOException {

		this.baseDir = IOUtil.makeTempFileAndDeleteOnExit("zipfilesystemtest");
		this.localFileSystem = new LocalFileSystem(baseDir);
		
		
		// Create an empty zipfile first
		
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
		
		zipOutputStream.close();

		final byte [] zipFileBytes = baos.toByteArray();
		localFileSystem.storeFile(zipFilePath, new ByteArrayInputStream(zipFileBytes), zipFileBytes.length);
	
		this.zipFilePathFile = new File(baseDir, StringUtil.join(zipFilePath, File.separatorChar));
		assertThat(zipFilePathFile.exists()).isTrue();
		
		try (ZipFile zipFile = new ZipFile(zipFilePathFile)) {
			assertThat(zipFile.entries().hasMoreElements()).isFalse();
		}
			
		this.zipFileSystem = new ZipFileSystem(
				() -> localFileSystem.readFile(zipFilePath),
				bytes -> localFileSystem.storeFile(zipFilePath, new ByteArrayInputStream(bytes), bytes.length));
	}
	
	public void testStoreAndRetrieve() throws IOException {

		prepareTestZipFile();
		
		final String [] path = new String [] { "path", "to", "file.txt" };
		
		final String text = "the text\n";
		
		final byte [] bytes = text.getBytes();
		
		zipFileSystem.storeFile(path, new ByteArrayInputStream(bytes), bytes.length);
		
		try (ZipFile fsFile = new ZipFile(zipFilePathFile)) {

			assertThat(fsFile.size()).isEqualTo(1);
			
			final ZipEntry zipEntry = fsFile.entries().nextElement();

			assertThat(zipEntry.getName()).isEqualTo("path/to/file.txt");
			assertThat(IOUtil.readAllAndClose(fsFile.getInputStream(zipEntry))).isEqualTo(text.getBytes());
		}
			
		final FileInput fileInput = zipFileSystem.readFileInput(path);
		
		assertThat(fileInput).isNotNull();
		assertThat(fileInput.getLength()).isEqualTo(bytes.length);
		assertThat(IOUtil.readAllAndClose(fileInput.getInputStream())).isEqualTo(bytes);
		
		final InputStream inputStream = zipFileSystem.readFile(path);
		assertThat(IOUtil.readAllAndClose(inputStream)).isEqualTo(bytes);
		
		assertThat(zipFileSystem.exists(path)).isTrue();
		
		try (ZipFile fsFile = new ZipFile(zipFilePathFile)) {
			assertThat(fsFile.getEntry("path/to/file.txt")).isNotNull();
		}
	
		zipFileSystem.deleteFile(path);
		
		assertThat(zipFileSystem.exists(path)).isFalse();
		
		try (ZipFile fsFile = new ZipFile(zipFilePathFile)) {
			assertThat(fsFile.getEntry("path/to/file.txt")).isNull();
		}
	}
	
	
	public void testListFiles() throws IOException {

		prepareTestZipFile();
		
		final byte [] textBytes = "Test text\n".getBytes();
		
		zipFileSystem.storeFile(
				new String [] { "path", "to", "file.txt" },
				new ByteArrayInputStream(textBytes),
				textBytes.length);
		
		assertThat(zipFileSystem.listFiles(new String [] { }).length).isEqualTo(0);
		assertThat(zipFileSystem.listFiles(new String [] { "path" }).length).isEqualTo(0);
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(1);
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" })[0]).isEqualTo("file.txt");
		
		final byte [] anotherTextBytes = "Test text\n".getBytes();
		
		zipFileSystem.storeFile(
				new String [] { "path", "to", "anotherFile.txt" },
				new ByteArrayInputStream(anotherTextBytes),
				anotherTextBytes.length);

		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(2);
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" })).contains("file.txt");
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" })).contains("anotherFile.txt");
		
		zipFileSystem.deleteFile(new String [] { "path", "to", "file.txt" });
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" }).length).isEqualTo(1);
		assertThat(zipFileSystem.listFiles(new String [] { "path", "to" })[0]).isEqualTo("anotherFile.txt");
	}
}
