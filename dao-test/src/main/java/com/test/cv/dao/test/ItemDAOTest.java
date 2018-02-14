package com.test.cv.dao.test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import com.test.cv.dao.IFoundItem;
import com.test.cv.dao.IFoundItemPhotoThumbnail;
import com.test.cv.dao.IItemDAO;
import com.test.cv.model.ItemPhoto;
import com.test.cv.model.items.Snowboard;
import com.test.cv.model.items.SnowboardProfile;

import static org.assertj.core.api.Assertions.assertThat;

import junit.framework.TestCase;

// Inherited by implementation specific test
public abstract class ItemDAOTest extends TestCase {

	protected abstract IItemDAO getItemDAO();

	private static Snowboard makeSnowboard() {
		final Snowboard snowboard = new Snowboard();
		
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
}
