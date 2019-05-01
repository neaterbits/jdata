package com.test.salesportal.dao;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.test.salesportal.common.IOUtil;

// Stream that reads thumbnails on demand, keeping as little as possible in memory
public abstract class RetrieveThumbnailsInputStream extends InputStream {

	public static class Thumbnail {
		public final String mimeType;
		public final int thumbnailSize;
		public final InputStream thumbnail;

		public Thumbnail(String mimeType, int thumbnailSize, InputStream thumbnail) {
			
			this.mimeType = mimeType;
			this.thumbnailSize = thumbnailSize;
			this.thumbnail = thumbnail;
		}

		@Override
		public String toString() {
			return mimeType + "/" + thumbnailSize + "/" + String.format("%08x", System.identityHashCode(thumbnail));
		}
	}

	enum State {
		INITIAL,
		METADATA,
		DATA,
		DONE
	}
	
	private byte [] metadata;
	private int metaDataPos;
	
	private State state;
	
	private Thumbnail cur;
	
	protected abstract Thumbnail getNext();
	
	protected RetrieveThumbnailsInputStream() {
		setState(State.INITIAL);
	}
	
	private void setState(State newState) {
		this.state = newState;
	}
	
	@Override
	public final int read() throws IOException {
		int nextByte;
		
		boolean ok = false;
		
		try {
			switch (state) {
			case INITIAL:
				// Nothing read yet, read initial
				nextByte = getNextThumbnail();
				break;
				
			case METADATA:
				if (metaDataPos >= metadata.length) { // has returned all metadata
					// Continue returning data
					if (cur.thumbnailSize == 0) {
						// no thumbnail, skip to next
						nextByte = getNextThumbnail();
					}
					else {
						setState(State.DATA);
						
						nextByte = getNextDataByte();
					}
				}
				else {
					nextByte = metadata[metaDataPos ++];
				}
				break;
				
			case DATA:
				nextByte = getNextDataByte();
				break;
				
			case DONE:
				nextByte = -1;
				break;
				
			default:
				throw new IllegalStateException("Unknown state " + state);
				
			}
			
			ok = true;
		}
		finally {
			if (cur != null && !ok) {
				// close inputstream in case of exception
				try {
					cur.thumbnail.close();
				}
				catch (Exception ex) {
					
				}
			}
		}

		return nextByte;
	}
	
	private int getNextThumbnail() throws IOException {
		int nextByte;
		
		this.cur = getNext();
		if (cur == null) {
			setState(State.DONE);
			
			nextByte = -1;
		}
		else {
			cur = updateMetaDataBuffer(cur);
			
			setState(State.METADATA);
			
			nextByte = metadata[metaDataPos ++];
		}
		return nextByte;
	}

	private Thumbnail updateMetaDataBuffer(Thumbnail thumbnail) throws IOException {
		final Thumbnail result;
		
		if (thumbnail.thumbnailSize == -1) {
			// Unknown size, just read all into byte buffer in order to figure out size
			final byte [] data = IOUtil.readAll(thumbnail.thumbnail);
		
			result = new Thumbnail(thumbnail.mimeType, data.length, new ByteArrayInputStream(data));
			
			try {
				thumbnail.thumbnail.close();
			}
			catch (Exception ex) {
				
			}
		}
		else {
			result = thumbnail;
		}
		
		if (result.thumbnailSize == 0) {
			// just add empty data to metadata
			this.metadata = new byte[5];
			Arrays.fill(metadata, (byte)0);
		}
		else {
			// must send in size and characters
			final ByteArrayOutputStream baos = new ByteArrayOutputStream(4 + result.mimeType.length() + 1);
			
			final DataOutputStream dataOut = new DataOutputStream(baos);
			
			dataOut.writeInt(result.thumbnailSize);
			
			dataOut.writeBytes(result.mimeType);
			dataOut.writeByte(0);
			
			this.metadata = baos.toByteArray();
		}
		this.metaDataPos = 0;
		
		return result;
	}
	
	private int getNextDataByte() throws IOException {

		int nextByte = cur.thumbnail.read();
		if (nextByte == -1) {
			// EOF on this
			try {
				cur.thumbnail.close();
			} catch (IOException e) {
			}
			
			// Try get next in stream
			nextByte = getNextThumbnail();
		}
		
		return nextByte;
	}

}
