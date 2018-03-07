package com.test.cv.rest;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.test.cv.dao.ItemStorageException;
import com.test.cv.model.Item;

@Path("/")
public class ItemService extends BaseService {

	
	@POST
	@Path("items")
	@Consumes("application/json")
	@Produces("application/json")
	public String storeItem(@QueryParam("userId") String userId, Item item, HttpServletRequest request) throws ItemStorageException {
		// Received an item as JSon, store it
		final String itemId = getItemDAO(request).addItem(userId, item);
		
		return "{ 'itemId : '" + itemId + "' }";
	}
	
	@POST
	@Path("items/{itemId}/image")
	@Consumes({ "image/jpeg", "image/png" })
	public void storeImage(@QueryParam("userId") String userId, @PathParam("itemId") String itemId, byte [] imageData, HttpServletRequest request) throws IOException, ItemStorageException {
		// Received an item as JSon, store it
		
		final ByteArrayInputStream photoInputStream1 = new ByteArrayInputStream(imageData);
		final ByteArrayInputStream photoInputStream2 = new ByteArrayInputStream(imageData);
		final String photoMimeType = request.getHeader("Content-Type").trim();
		
		final BufferedImage image = ImageIO.read(photoInputStream2);

		// Scale image, keeping aspect ratio
		final int bb = 240;
		
		final int thumbWidth;
		final int thumbHeight;
		
		final ByteArrayInputStream thumbnailInputStream;
		final String thumbnailMimeType = photoMimeType;
		
		if (image.getWidth() <= bb && image.getHeight() <= bb) {
			thumbnailInputStream = new ByteArrayInputStream(imageData);
		}
		else {
			
			if (image.getWidth() >= image.getHeight()) {
				// landscape
				final double scaleDown = bb / image.getWidth();
				thumbWidth = bb;
				thumbHeight = (int)(image.getHeight() * scaleDown);
			}
			else {
				final double scaleDown = bb / image.getHeight();
				thumbWidth = (int)(image.getWidth() * scaleDown);
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
			
			thumbnailInputStream = new ByteArrayInputStream(baos.toByteArray());
		}
				
		// Must create a thumbnail from photo
		// Some JPEGs may have thumbnails already
		
		getItemDAO(request).addPhotoAndThumbnailForItem(userId, itemId, thumbnailInputStream, thumbnailMimeType, photoInputStream1, photoMimeType);
	}
	
	
	private static RenderedImage imageToRenderedImage(Image image) {
		final BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D g = bufferedImage.createGraphics();

		g.drawImage(image, 0, 0, null);
		
		return bufferedImage;
	}

}
