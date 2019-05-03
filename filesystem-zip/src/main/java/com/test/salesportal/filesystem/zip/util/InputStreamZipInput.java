package com.test.salesportal.filesystem.zip.util;

import java.io.IOException;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InputStreamZipInput extends ZipInput {

	private final InputStreamSupplier inputStreamSupplier;
	
	public InputStreamZipInput(InputStreamSupplier inputStreamSupplier) {
		
		if (inputStreamSupplier == null) {
			throw new IllegalArgumentException("inputStreamSupplier == null");
		}
		
		this.inputStreamSupplier = inputStreamSupplier;
	}

	@Override
	public <T> T forEach(Function<ZipEntry, T> input) throws IOException {
		
		T result = null;
		
		try (ZipInputStream zipInputStream = new ZipInputStream(inputStreamSupplier.getInputStream())) {
			
			ZipEntry zipEntry;
			
			while (null != (zipEntry = zipInputStream.getNextEntry())) {
				result = input.apply(zipEntry);
				
				if (result != null) {
					break;
				}
			}
		}
		
		return result;
	}

	@Override
	public <T> T forEachWithInputStream(OnZipInputStream<T> input) throws IOException {

		T result = null;
		
		try (ZipInputStream zipInputStream = new ZipInputStream(inputStreamSupplier.getInputStream())) {
			
			ZipEntry zipEntry;
			
			while (null != (zipEntry = zipInputStream.getNextEntry())) {
				
				result = input.onInputStream(zipEntry, zipInputStream);
				
				zipInputStream.closeEntry();
			}
		}
		
		return result;
	}
}
