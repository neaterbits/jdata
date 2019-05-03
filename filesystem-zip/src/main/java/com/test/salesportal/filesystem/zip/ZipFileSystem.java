package com.test.salesportal.filesystem.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.StringUtil;
import com.test.salesportal.filesystem.api.IFileSystem;
import com.test.salesportal.filesystem.zip.util.InputStreamSupplier;
import com.test.salesportal.filesystem.zip.util.InputStreamZipInput;
import com.test.salesportal.filesystem.zip.util.ZipInput;

public class ZipFileSystem implements IFileSystem {

	@FunctionalInterface
	public interface BytesWriter {
		void writeBytes(byte [] bytes) throws IOException;
	}

	private final InputStreamSupplier readZipFile;
	private final BytesWriter writeZipFile;
	
	public ZipFileSystem(InputStreamSupplier readZipFile, BytesWriter writeZipFile) {

		this.readZipFile = readZipFile;
		this.writeZipFile = writeZipFile;
		
	}

	private static String pathToString(String [] path) {
		return StringUtil.join(path, '/');
	}
	
	
	private static long guessZipEntryCompressedSize(ZipEntry zipEntry) {
		
		final long size;
		
		if (zipEntry.getCompressedSize() > 0L) {
			size = zipEntry.getCompressedSize();
		}
		else if (zipEntry.getSize() > 0L) {
			size = zipEntry.getSize();
		}
		else {
			size = 0;
		}
		
		return size;
	}
	
	@FunctionalInterface
	interface PutAdditionalEntries {
		void onPutAdditionalEntries(ZipOutputStream outputStream) throws IOException;
	}
	
	private static byte [] replaceFile(
			ZipInput zipInput,
			Predicate<ZipEntry> putEntry,
			PutAdditionalEntries putAdditionalEntries) throws IOException {
		
		
		final List<ZipEntry> zipEntries = new ArrayList<>();
		
		zipInput.forEach(zipEntries::add);
		
		final long zipFileSize = zipEntries.stream()
				.map(ZipFileSystem::guessZipEntryCompressedSize)
				.reduce(Long::sum)
				.orElse(100000L);
				
		final ByteArrayOutputStream baos = new ByteArrayOutputStream((int)zipFileSize);

		try (ZipOutputStream zipOutput = new ZipOutputStream(baos)) {

			zipInput.forEachWithInputStream((zipEntry, inputStream) -> {
			
				final boolean put = putEntry.test(zipEntry);
				
				if (put) {
					final ZipEntry outputEntry = new ZipEntry(zipEntry.getName());
					
					zipOutput.putNextEntry(outputEntry);
					
					IOUtil.copyStreams(inputStream, zipOutput);
				}
				
				return null;
			});
			
			if (putAdditionalEntries != null) {
				putAdditionalEntries.onPutAdditionalEntries(zipOutput);
			}
		}

		return baos.toByteArray();
	}
	
	@Override
	public void storeFile(String[] path, InputStream toStore, Integer streamLength) throws IOException {

		final String pathString = pathToString(path);

		final byte [] replaced = replaceFile(
				new InputStreamZipInput(readZipFile),
				entry -> !entry.getName().equals(pathString),
				zipOutputStream -> {
					
					final ZipEntry replacedEntry = new ZipEntry(pathString);
					
					zipOutputStream.putNextEntry(replacedEntry);
					
					IOUtil.copyStreams(toStore, zipOutputStream);
					
					zipOutputStream.closeEntry();
				});

		writeZipFile.writeBytes(replaced);
	}

	@Override
	public FileInput readFileInput(String[] path) throws IOException {
		
		final String pathString = pathToString(path);
		
		return new InputStreamZipInput(readZipFile).forEachWithInputStream((zipEntry, inputStream) -> {
			
			FileInput result;
			
			if (zipEntry.getName().equals(pathString)) {
				
				final InputStream stream;
				final int length;
				
				if (zipEntry.getSize() >= 0L) {
					stream = inputStream;
					length = (int)zipEntry.getSize();
				}
				else {
					
					final ByteArrayOutputStream baos = new ByteArrayOutputStream();
					
					IOUtil.copyStreams(inputStream, baos);
					
					final byte [] bytes = baos.toByteArray();
					
					stream = new ByteArrayInputStream(bytes);
					length = bytes.length;
				}
				
				result = new FileInput(stream, length);
			}
			else {
				result = null;
			}
			
			return result;
		});
	}

	@Override
	public void deleteFile(String[] path) throws IOException {
		
		final String pathString = pathToString(path);

		final byte [] replaced = replaceFile(
				new InputStreamZipInput(readZipFile),
				entry -> !entry.getName().equals(pathString),
				null);
		
		writeZipFile.writeBytes(replaced);
	}

	@Override
	public boolean exists(String[] path) throws IOException {

		final String pathString = pathToString(path);

		final ZipInput zipInput = new InputStreamZipInput(readZipFile);

		return null != zipInput.forEach(entry -> {
			
			return entry.getName().equals(pathString) ? entry : null;
		});
	}

	private static final String [] NO_STRINGS = new String[0];
	
	@Override
	public String[] listFiles(String[] path) throws IOException {

		final List<String> result = new ArrayList<>();
		
		final ZipInput zipInput = new InputStreamZipInput(readZipFile);
		
		zipInput.forEach(entry -> {
		
			final String name = entry.getName();

			final String [] namePath = StringUtil.split(name, '/');
		
			if (namePath.length == 0) {
				throw new IllegalStateException();
			}
			
			final String [] nameDir = namePath.length == 1
					? NO_STRINGS
					: Arrays.copyOf(namePath, namePath.length - 1);
			
			if (Arrays.equals(path, nameDir)) {
				result.add(namePath[namePath.length - 1]);
			}
			
			return null;
		});
		
		return result.toArray(new String[result.size()]);
	}
}
