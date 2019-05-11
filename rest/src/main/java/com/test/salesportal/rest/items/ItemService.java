package com.test.salesportal.rest.items;

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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.common.images.ThumbAndImageUrls;
import com.test.salesportal.dao.ItemStorageException;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.rest.BaseService;
import com.test.salesportal.rest.BaseServiceLogic;

@Path("/")
public class ItemService extends BaseService {
	
	public ItemService(String localFileDir) {
		super(localFileDir);
	}

	@POST
	@Path("items")
	@Consumes("application/json")
	@Produces("application/text")
	public String storeItem(@QueryParam("userId") String userId, Item item, HttpServletRequest request) throws ItemStorageException {
		// Received an item as JSon, store it
		final String itemId = getItemUpdateDAO(request).addItem(userId, item);
		
		return itemId;
	}

	@POST
	@Path("items/{itemId}/thumbAndImageUrls")
	@Consumes("application/json")
	public void storeThumbAndImageUrls(@QueryParam("userId") String userId, @QueryParam("itemId") String itemId, @QueryParam("itemType") String itemType, ThumbAndImageUrls urls, HttpServletRequest request) throws ItemStorageException {
		if (userId == null || userId.trim().isEmpty()) {
			throw new IllegalArgumentException("No userId");
		}

		if (itemType == null || itemType.trim().isEmpty()) {
			throw new IllegalArgumentException("No item type");
		}

		final Class<? extends Item> type = ItemTypes.getTypeByName(itemType).getType();

		// Received an item as JSon, store it
		getItemUpdateDAO(request).addThumbAndPhotoUrlsForItem(userId, itemId, type, urls);
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
		final int bb = BaseServiceLogic.THUMBNAIL_MAX_SIZE;
		
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
		
		getItemUpdateDAO(request).addPhotoAndThumbnailForItem(
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
		
		getItemUpdateDAO(request).addPhotoUrlAndThumbnailForItem(
				userId, itemId, ItemTypes.getTypeByName(itemType).getType(),
				thumbnailInputStream, thumbnailMimeType, thumbDataLength, thumbWidth, thumbHeight,
				imageUrl);
		
	}

	// Get total number of images for item
	@GET
	@Path("items/{itemId}/photoCount")
	public int getPhotoCount(
			@PathParam("itemId") String itemId,
			HttpServletRequest request) throws IOException, ItemStorageException {
		
		return getItemRetrievalDAO(request).getPhotoCount(itemId);
	}
	
	@GET
	@Path("items/{itemId}/thumbs") 
	@Produces({"image/jpeg"})
	public byte [] getThumb(
			@PathParam("itemId") String itemId,
			@QueryParam("thumbNo") int photoNo,
			HttpServletRequest request) throws IOException, ItemStorageException {
	
		final InputStream photoStream = getItemRetrievalDAO(request).getItemThumb(itemId, photoNo);

		final byte[] data;
		try {
			data = IOUtil.readAll(photoStream);
		}
		finally {
			photoStream.close();
		}

		return data;
	}

	
	@GET
	@Path("items/{itemId}/photos") 
	@Produces({"image/jpeg"})
	public byte [] getPhoto(
			@PathParam("itemId") String itemId,
			@QueryParam("photoNo") int photoNo,
			HttpServletRequest request) throws IOException, ItemStorageException {
	
		final InputStream photoStream = getItemRetrievalDAO(request).getItemPhoto(itemId, photoNo);

		final byte[] data;
		try {
			data = IOUtil.readAll(photoStream);
		}
		finally {
			photoStream.close();
		}

		return data;
	}
	
	private static RenderedImage imageToRenderedImage(Image image) {
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D g = bufferedImage.createGraphics();

		g.drawImage(image, 0, 0, null);
		
		return bufferedImage;
	}
}
