package com.test.cv.dao.test;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import com.test.cv.common.ItemId;
import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.ItemPhotoCategory;
import com.test.cv.model.items.Snowboard;
import com.test.cv.model.items.SnowboardProfile;

import static org.assertj.core.api.Assertions.assertThat;

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
	}
	
	public void testStoreThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
		
		final String userId = "theUser";
		final String itemId;

		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);

			 final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream("thumbnail".getBytes());
			 final ByteArrayInputStream photoInputStream = new ByteArrayInputStream("photo".getBytes());

			 itemDAO.addPhotoAndThumbnailForItem(userId, itemId, thumbnailInputStream, "image/png", photoInputStream, "image/jpeg");

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
	}
	
	public void testMoveThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
			
		final String userId = "user1";
		final String itemId;
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail1".getBytes(), "photo1".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail2".getBytes(), "photo2".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail3".getBytes(), "photo3".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail4".getBytes(), "photo4".getBytes());
			 
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
			 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, 0, 3);
			 
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
			 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, 1, 2);

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
			 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, 2, 1);

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
			 itemDAO.movePhotoAndThumbnailForItem(userId, itemId, 3, 0);

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
	}
	

	public void testRetrieveMultipleThumbnails() throws Exception {
		final Snowboard snowboard1 = makeSnowboard();
		final Snowboard snowboard2 = makeSnowboard();
		final Snowboard snowboard3 = makeSnowboard();
			
		final String user1Id = "user1";
		final String user2Id = "user2";
		final String itemId1;
		final String itemId2;
		final String itemId3;

		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId1 = itemDAO.addItem(user1Id, snowboard1);
			 itemId2 = itemDAO.addItem(user2Id, snowboard2);
			 itemId3 = itemDAO.addItem(user2Id, snowboard3);
			 
			 addPhotoAndThumbnail(itemDAO, user1Id, itemId1, "thumbnail1_1".getBytes(), "photo1_1".getBytes());
			 addPhotoAndThumbnail(itemDAO, user1Id, itemId1, "thumbnail1_2".getBytes(), "photo1_2".getBytes());

			 // item 2 has no photo nor thumbnail
			 
			 addPhotoAndThumbnail(itemDAO, user2Id, itemId3, "thumbnail3_1".getBytes(), "photo3_1".getBytes());

			 final ItemId [] itemIds = new ItemId [] {
				new ItemId(user1Id, itemId1),
				new ItemId(user2Id, itemId2),
				new ItemId(user2Id, itemId3)
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

	private void addPhotoAndThumbnail(IItemDAO itemDAO, String userId, String itemId, byte [] thumbnail, byte [] photo) throws ItemStorageException {
		 final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnail);
		 final ByteArrayInputStream photoInputStream = new ByteArrayInputStream(photo);

		 itemDAO.addPhotoAndThumbnailForItem(userId, itemId, thumbnailInputStream, "image/png", photoInputStream, "image/jpeg");
	}

	public void testDeleteThumbnailAndPhoto() throws Exception {
		final Snowboard snowboard = makeSnowboard();
			
		final String userId = "user1";
		final String itemId;
		try (IItemDAO itemDAO = getItemDAO()) {
			 itemId = itemDAO.addItem(userId, snowboard);
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail1".getBytes(), "photo1".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail2".getBytes(), "photo2".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail3".getBytes(), "photo3".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail4".getBytes(), "photo4".getBytes());
			 
			 itemDAO.deletePhotoAndThumbnailForItem(userId, itemId, 1);
			 
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
			 itemDAO.deletePhotoAndThumbnailForItem(userId, itemId, 2);

			 	
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
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail1".getBytes(), "photo1".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail2".getBytes(), "photo2".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail3".getBytes(), "photo3".getBytes());
			 addPhotoAndThumbnail(itemDAO, userId, itemId, "thumbnail4".getBytes(), "photo4".getBytes());

			 itemDAO.deleteItem(userId, itemId);
			 
			 assertThat(itemDAO.getPhotoThumbnails(userId, itemId).isEmpty()).isTrue();
			 assertThat(itemDAO.getNumThumbnails(userId, itemId)).isEqualTo(0);
			 assertThat(itemDAO.getNumPhotos(userId, itemId)).isEqualTo(0);
			 assertThat(itemDAO.getItem(userId, itemId)).isEqualTo(null);
		}
	}
}
