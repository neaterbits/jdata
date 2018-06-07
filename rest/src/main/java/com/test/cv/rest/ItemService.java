package com.test.cv.rest;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.test.cv.common.IOUtil;
import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;
import com.test.cv.model.items.ItemTypes;

@Path("/")
public class ItemService extends BaseService {

	
	@POST
	@Path("items")
	@Consumes("application/json")
	@Produces("application/text")
	public String storeItem(@QueryParam("userId") String userId, Item item, HttpServletRequest request) throws ItemStorageException {
		// Received an item as JSon, store it
		final String itemId = getItemDAO(request).addItem(userId, item);
		
		return itemId;
	}
	
	@POST
	@Path("items/{itemId}/image")
	@Consumes({ "image/jpeg", "image/png" })
	public void storeImage(@QueryParam("userId") String userId, @PathParam("itemId") String itemId, @QueryParam("itemType") String itemType, @QueryParam("index") int index, byte [] imageData, HttpServletRequest request) throws IOException, ItemStorageException {
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("No userId");
		}
		
		if (itemType == null || itemType.trim().isEmpty()) {
			throw new IllegalArgumentException("No item type");
		}
		
		final ByteArrayInputStream photoInputStream1 = new ByteArrayInputStream(imageData);
		final ByteArrayInputStream photoInputStream2 = new ByteArrayInputStream(imageData);
		final String photoMimeType = request.getHeader("Content-Type").trim();
		
		final BufferedImage image = ImageIO.read(photoInputStream2);

		// Scale image, keeping aspect ratio
		final int bb = THUMBNAIL_MAX_SIZE;
		
		final int thumbWidth;
		final int thumbHeight;
		
		final ByteArrayInputStream thumbnailInputStream;
		final String thumbnailMimeType = photoMimeType;
		
		final int width = image.getWidth();
		final int height = image.getHeight();
		
		final int thumbDataLength;
		
		if (width <= bb && height <= bb) {
			thumbnailInputStream = new ByteArrayInputStream(imageData);
			
			thumbWidth = width;
			thumbHeight = height;
			
			thumbDataLength = imageData.length;
		}
		else {
			
			if (width >= height) {
				// landscape
				final double scaleDown = bb / (double)width;
				thumbWidth = bb;
				thumbHeight = (int)(height * scaleDown);
			}
			else {
				final double scaleDown = bb / (double)height;
				thumbWidth = (int)(width * scaleDown);
				thumbHeight = bb;
			}
			
			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			final Image thumb = image.getScaledInstance(thumbWidth, thumbHeight, BufferedImage.SCALE_SMOOTH);

			final String formatName;

			switch (thumbnailMimeType) {
			case "image/jpeg": formatName = "jpeg"; break;
			case "image/png": formatName = "png"; break;
			
			default:
				throw new UnsupportedOperationException("Unknown mimetype " + thumbnailMimeType);
			}
			
			ImageIO.write(imageToRenderedImage(thumb), formatName, baos);
			
			final byte [] thumbData = baos.toByteArray();
			
			thumbnailInputStream = new ByteArrayInputStream(thumbData);
			
			thumbDataLength = thumbData.length;
		}
				
		// Must create a thumbnail from photo
		// Some JPEGs may have thumbnails already
		
		getItemDAO(request).addPhotoAndThumbnailForItem(
				userId, itemId, ItemTypes.getTypeByName(itemType).getType(),
				thumbnailInputStream, thumbnailMimeType, thumbDataLength, thumbWidth, thumbHeight,
				photoInputStream1, photoMimeType, imageData.length);
	}

	@POST
	@Path("items/{itemId}/imageThumbAndUrl")
	@Consumes({ "image/jpeg", "image/png" })
	public void storeThumbAndImageUrl(
			@QueryParam("userId") String userId,
			@PathParam("itemId") String itemId,
			@QueryParam("itemType") String itemType,
			@QueryParam("index") int index,
			@QueryParam("thumbWidth") int thumbWidth,
			@QueryParam("thumbHeight") int thumbHeight,
			@QueryParam("imageUrl") String imageUrl,
			byte [] thumbData,
			HttpServletRequest request) throws IOException, ItemStorageException {

		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("No userId");
		}
		
		if (itemType == null || itemType.trim().isEmpty()) {
			throw new IllegalArgumentException("No item type");
		}
		
		final String thumbnailMimeType = request.getContentType();
		
		final int thumbDataLength = thumbData.length;
		final InputStream thumbnailInputStream = new ByteArrayInputStream(thumbData);
		
		getItemDAO(request).addPhotoUrlAndThumbnailForItem(
				userId, itemId, ItemTypes.getTypeByName(itemType).getType(),
				thumbnailInputStream, thumbnailMimeType, thumbDataLength, thumbWidth, thumbHeight,
				imageUrl);
		
	}

	
	private static RenderedImage imageToRenderedImage(Image image) {
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D g = bufferedImage.createGraphics();

		g.drawImage(image, 0, 0, null);
		
		return bufferedImage;
	}

}
