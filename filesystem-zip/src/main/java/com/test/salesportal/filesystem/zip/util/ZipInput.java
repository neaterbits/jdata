package com.test.salesportal.filesystem.zip.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;
import java.util.zip.ZipEntry;

public abstract class ZipInput {
	
	@FunctionalInterface
	public interface OnZipInputStream<T> {
		
		T onInputStream(ZipEntry zipEntry, InputStream inputStream) throws IOException;
	}
	
	/**
	 * Iterates until non-null is returned
	 * @param input callback
	 * @return return a value here to exit iteration
	 * @throws IOException
	 */
	
	public abstract <T> T forEach(Function<ZipEntry, T> input) throws IOException;

	
	/**
	 * Iterates until non-null is returned
	 * @param input callback
	 * @return return a value here to exit iteration
	 * @throws IOException
	 */
	public abstract <T> T forEachWithInputStream(OnZipInputStream<T> input) throws IOException;

}
