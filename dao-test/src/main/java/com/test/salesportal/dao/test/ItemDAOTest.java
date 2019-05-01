package com.test.salesportal.dao.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.dao.IFoundItemPhotoThumbnail;
import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.ItemStorageException;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemPhoto;
import com.test.salesportal.model.items.sports.Snowboard;
import com.test.salesportal.model.items.sports.SnowboardProfile;

import junit.framework.TestCase;

// Inherited by implementation specific test
public abstract class ItemDAOTest extends TestCase {

	protected abstract IItemDAO getItemDAO();

	private static Snowboard makeSnowboard() {
		final Snowboard snowboard = new Snowboard();
		
		snowboard.setTitle("Snowboard for sale");
		snowboard.setMake("Burton");
		snowboard.setModel("1234");
		snowboard.setProfile(SnowboardProfile.CAMBER);
		snowboard.setHeight(new BigDecimal("2.5"));
		snowboard.setWidth(new BigDecimal("30.4"));
		snowboard.setLength(new BigDecimal("164.5"));

		return snowboard;
	}
	
	public void testStoreAndRetrieveItem() throws Exception {
		
		final String userId = "theUser";
		final String itemId;
		
		final Snowboard snowboard = makeSnowboard();
		
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
		}
		
		// Obtain DAO anew to make sure we skip any caching
			
		try (IItemDAO itemDAO = getItemDAO()) {
			try {
				final IFoundItem retrieved = itemDAO.getItem(userId, itemId);
				
				final Snowboard s = (Snowboard)retrieved.getItem();
				
				assertThat(s).isNotSameAs(snowboard);
				
				assertThat(s.getMake()).isEqualTo("Burton");
				assertThat(s.getModel()).isEqualTo("1234");
				assertThat(s.getProfile()).isEqualTo(SnowboardProfile.CAMBER);
				assertThat(s.getHeight().compareTo(new BigDecimal("2.5"))).isEqualTo(0);
				assertThat(s.getWidth().compareTo(new BigDecimal("30.4"))).isEqualTo(0);
				assertThat(s.getLength().compareTo(new BigDecimal("164.5"))).isEqualTo(0);
			}
			finally {
				itemDAO.deleteItem(userId, itemId, Snowboard.class);
			}
		}
	}
	
	public void testStoreThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
		
		final String userId = "theUser";
		final String itemId;

		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 
			 try {
	
				 final byte [] thumbnailBytes = "thumbnail".getBytes();
				 final byte [] photoBytes = "photo".getBytes();
				 final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailBytes);
				 final ByteArrayInputStream photoInputStream = new ByteArrayInputStream(photoBytes);
	
				 itemDAO.addPhotoAndThumbnailForItem(userId, itemId, Snowboard.class,
						 thumbnailInputStream, "image/png", thumbnailBytes.length, 320, 240,
						 photoInputStream, "image/jpeg", photoBytes.length);
	
				 // Retrieve back
				 final List<IFoundItemPhotoThumbnail> thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
	
				 assertThat(thumbnails).isNotNull();
				 assertThat(thumbnails.size()).isEqualTo(1);
	
				 final IFoundItemPhotoThumbnail thumbnail = thumbnails.get(0);
	
				 assertThat(thumbnail.getMimeType()).isEqualTo("image/png");
				 assertThat(thumbnail.getData()).containsExactly("thumbnail".getBytes());
	
				 final ItemPhoto photo = itemDAO.getItemPhoto(userId, thumbnail);
	
				 assertThat(photo).isNotNull();
				 assertThat(photo.getMimeType()).isEqualTo("image/jpeg");
				 assertThat(photo.getData()).containsExactly("photo".getBytes());
			 }
			 finally {
				 itemDAO.deleteItem(userId, itemId, Snowboard.class);
			 }
		}
	}
	
	public void testMoveThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
			
		final String userId = "user1";
		final String itemId;
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 
			 try {
				 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail1".getBytes(), "photo1".getBytes());
				 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail2".getBytes(), "photo2".getBytes());
				 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail3".getBytes(), "photo3".getBytes());
				 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail4".getBytes(), "photo4".getBytes());
				 
				 List<IFoundItemPhotoThumbnail> thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
				 
				 assertThat(thumbnails.size()).isEqualTo(4);
				 
				 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
				 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail1".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo1".getBytes());		 
	
				 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
				 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail2".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo2".getBytes());		 
	
				 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
				 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail3".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo3".getBytes());		 
	
				 assertThat(thumbnails.get(3).getIndex()).isEqualTo(3);
				 assertThat(thumbnails.get(3).getData()).isEqualTo("thumbnail4".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(3)).getData()).isEqualTo("photo4".getBytes());		 
				 
				 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(4);
				 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(4);
				 
				 // Move first to last
				 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 0, 3);
				 
				 thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
				 
				 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
				 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail2".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo2".getBytes());		 
	
				 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
				 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail3".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo3".getBytes());		 
	
				 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
				 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail4".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo4".getBytes());		 
	
				 assertThat(thumbnails.get(3).getIndex()).isEqualTo(3);
				 assertThat(thumbnails.get(3).getData()).isEqualTo("thumbnail1".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(3)).getData()).isEqualTo("photo1".getBytes());		 
				 
				 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(4);
				 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(4);
				 
				 // Move position 1 to 2
				 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 1, 2);
	
				 thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
				 
				 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
				 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail2".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo2".getBytes());		 
	
				 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
				 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail4".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo4".getBytes());		 
	
				 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
				 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail3".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo3".getBytes());
	
				 assertThat(thumbnails.get(3).getIndex()).isEqualTo(3);
				 assertThat(thumbnails.get(3).getData()).isEqualTo("thumbnail1".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(3)).getData()).isEqualTo("photo1".getBytes());		 
				 
				 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(4);
				 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(4);
	
				 // Move position 2 to 1
				 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 2, 1);
	
				 thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
				 
				 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
				 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail2".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo2".getBytes());		 
	
				 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
				 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail3".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo3".getBytes());		 
	
				 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
				 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail4".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo4".getBytes());
	
				 assertThat(thumbnails.get(3).getIndex()).isEqualTo(3);
				 assertThat(thumbnails.get(3).getData()).isEqualTo("thumbnail1".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(3)).getData()).isEqualTo("photo1".getBytes());		 
				 
				 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(4);
				 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(4);
	
				 // Move position 3 to 0
				 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 3, 0);
	
				 thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
				 
				 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
				 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail1".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo1".getBytes());		 
				 
				 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
				 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail2".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo2".getBytes());		 
	
				 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
				 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail3".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo3".getBytes());		 
	
				 assertThat(thumbnails.get(3).getIndex()).isEqualTo(3);
				 assertThat(thumbnails.get(3).getData()).isEqualTo("thumbnail4".getBytes());
				 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(3)).getData()).isEqualTo("photo4".getBytes());
	
	
				 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(4);
				 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(4);
			 }
			 finally {
				 itemDAO.deleteItem(userId, itemId, Snowboard.class);
			 }
		}
	}
	

	public void testRetrieveMultipleThumbnails() throws Exception {
		final Snowboard snowboard1 = makeSnowboard();
		final Snowboard snowboard2 = makeSnowboard();
		final Snowboard snowboard3 = makeSnowboard();
			
		final String user1Id = "user1";
		final String user2Id = "user2";
		String itemId1 = null;
		String itemId2 = null;
		String itemId3 = null;

		try (IItemDAO itemDAO = getItemDAO()) {
			
			try {
				 itemId1 = itemDAO.addItem(user1Id, snowboard1);
				 itemId2 = itemDAO.addItem(user2Id, snowboard2);
				 itemId3 = itemDAO.addItem(user2Id, snowboard3);
				 
				 addPhotoAndThumbnail(itemDAO, user1Id, itemId1, Snowboard.class, "thumbnail1_1".getBytes(), "photo1_1".getBytes());
				 addPhotoAndThumbnail(itemDAO, user1Id, itemId1, Snowboard.class, "thumbnail1_2".getBytes(), "photo1_2".getBytes());
	
				 // item 2 has no photo nor thumbnail
				 
				 addPhotoAndThumbnail(itemDAO, user2Id, itemId3, Snowboard.class, "thumbnail3_1".getBytes(), "photo3_1".getBytes());
	
				 final String [] itemIds = new String[] {
					itemId1,
					itemId2,
					itemId3
				 };
				 
				 final InputStream thumbnailStream = itemDAO.retrieveAndConcatenateThumbnails(itemIds);
				 
				 try {
					 final DataInputStream dataInput = new DataInputStream(thumbnailStream);
					 
					 int thumbnailSize = dataInput.readInt();
					 assertThat(thumbnailSize).isEqualTo("thumbnail1_1".length());
					 assertThat(readAscii(dataInput)).isEqualTo("image/png");
					 assertThat(readBytes(dataInput, thumbnailSize)).isEqualTo("thumbnail1_1".getBytes());
					 
					 // item 2 has no thumbnail so expect empty entry
					 thumbnailSize = dataInput.readInt();
					 assertThat(thumbnailSize).isEqualTo(0);
					 assertThat(readAscii(dataInput)).isEqualTo("");
					 assertThat(readBytes(dataInput, thumbnailSize)).isEqualTo(new byte[0]);
					 
					 thumbnailSize = dataInput.readInt();
					 assertThat(thumbnailSize).isEqualTo("thumbnail3_1".length());
					 assertThat(readAscii(dataInput)).isEqualTo("image/png");
					 assertThat(readBytes(dataInput, thumbnailSize)).isEqualTo("thumbnail3_1".getBytes());
					 
					 // Should be EOF
					 assertThat(dataInput.read()).isEqualTo(-1);
				 }
				 finally {
					 thumbnailStream.close();
				 }
			}
			finally {
				if (itemId1 != null) {
				 itemDAO.deleteItem(user1Id, itemId1, Snowboard.class);
				}
				
				if (itemId2 != null) {
				 itemDAO.deleteItem(user2Id, itemId2, Snowboard.class);
				}
				
				if (itemId3 != null) {
				 itemDAO.deleteItem(user2Id, itemId3, Snowboard.class);
				}
			}
		}
	}
	
	private static String readAscii(DataInput dataInput) throws IOException {
		final StringBuilder sb = new StringBuilder();
		
		for (;;) {
			int c = dataInput.readUnsignedByte();
			
			if (c == -1) {
				throw new EOFException();
			}
			
			if (c == 0) {
				break;
			}
			
			sb.append((char)c);
		}
		
		return sb.toString();
	}
	
	private static byte [] readBytes(DataInput dataInput, int size) throws IOException {
		final byte [] bytes = new byte[size];
		
		dataInput.readFully(bytes);
		
		return bytes;
	}

	private void addPhotoAndThumbnail(IItemDAO itemDAO, String userId, String itemId, Class<? extends Item> type, byte [] thumbnail, byte [] photo) throws ItemStorageException {
		 final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnail);
		 final ByteArrayInputStream photoInputStream = new ByteArrayInputStream(photo);

		 itemDAO.addPhotoAndThumbnailForItem(userId, itemId, type, thumbnailInputStream, "image/png", thumbnail.length, 320, 240, photoInputStream, "image/jpeg", photo.length);
	}

	public void testDeleteThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
			
		final String userId = "user1";
		final String itemId;
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail1".getBytes(), "photo1".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail2".getBytes(), "photo2".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail3".getBytes(), "photo3".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail4".getBytes(), "photo4".getBytes());
			 
			 itemDAO.deletePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 1);
			 
			 List<IFoundItemPhotoThumbnail> thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
			 
			 assertThat(thumbnails.size()).isEqualTo(3);
			 
			 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
			 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail1".getBytes());
			 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo1".getBytes());		 
	
			 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
			 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail3".getBytes());
			 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo3".getBytes());		 

			 assertThat(thumbnails.get(2).getIndex()).isEqualTo(2);
			 assertThat(thumbnails.get(2).getData()).isEqualTo("thumbnail4".getBytes());
			 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(2)).getData()).isEqualTo("photo4".getBytes());		 
			 
			 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(3);
			 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(3);
			 
			 // Try delete last enry as well
			 itemDAO.deletePhotoAndThumbnailForItem(userId, itemId, Snowboard.class, 2);

			 	
			 thumbnails = itemDAO.getPhotoThumbnails(userId, itemId);
			 
			 assertThat(thumbnails.size()).isEqualTo(2);
			 
			 assertThat(thumbnails.get(0).getIndex()).isEqualTo(0);
			 assertThat(thumbnails.get(0).getData()).isEqualTo("thumbnail1".getBytes());
			 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(0)).getData()).isEqualTo("photo1".getBytes());		 
	
			 assertThat(thumbnails.get(1).getIndex()).isEqualTo(1);
			 assertThat(thumbnails.get(1).getData()).isEqualTo("thumbnail3".getBytes());
			 assertThat(itemDAO.getItemPhoto(userId, thumbnails.get(1)).getData()).isEqualTo("photo3".getBytes());		 

			 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(2);
			 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(2);

		}
	}
	
	public void testDeleteItem() throws Exception {
		final Snowboard snowboard = makeSnowboard();
		
		final String userId = "user1";
		final String itemId;
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail1".getBytes(), "photo1".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail2".getBytes(), "photo2".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail3".getBytes(), "photo3".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, Snowboard.class, "thumbnail4".getBytes(), "photo4".getBytes());

			 itemDAO.deleteItem(userId, itemId, Snowboard.class);
			 
			 assertThat(itemDAO.getPhotoThumbnails(userId, itemId).isEmpty()).isTrue();
			 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(0);
			 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(0);
			 assertThat(itemDAO.getItem(userId, itemId)).isEqualTo(null);
		}
	}
}
