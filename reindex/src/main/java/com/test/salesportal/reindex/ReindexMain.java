package com.test.salesportal.reindex;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.test.salesportal.common.ItemId;
import com.test.salesportal.dao.IFoundItem;
import com.test.salesportal.dao.IFoundItemPhotoThumbnail;
import com.test.salesportal.dao.IItemDAO;
import com.test.salesportal.dao.xml.XMLItemDAO;
import com.test.salesportal.index.IndexSearchCursor;
import com.test.salesportal.index.lucene.LuceneItemIndex;
import com.test.salesportal.model.Item;
import com.test.salesportal.model.ItemPhoto;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.xmlstorage.api.LockProvider;
import com.test.salesportal.xmlstorage.local.LocalXmlStorage;

public class ReindexMain {

	public static void main(String [] args) throws Exception {
		
		if (args.length != 2) {
			throw new IllegalArgumentException("Expected <srcdir> <dstdir>");
		}
		
		final File srcDir = new File(args[0]);
		
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			throw new IllegalArgumentException("no directory at " + srcDir.getAbsolutePath());
		}
		
		final File dstDir = new File(args[1]);
		
		if (dstDir.exists()) {
			if (!dstDir.isDirectory()) {
				throw new IllegalArgumentException("dst dir exists but is not a directory");
			}
			
			if (dstDir.list().length > 0) {
				throw new IllegalArgumentException("dst dir is not empty");
			}
		}
		else {
			if (!dstDir.mkdirs()) {
				throw new IllegalArgumentException("Failed to make dstdir");
			}
		}
		
		final LocalXmlStorage srcStorage = new LocalXmlStorage(srcDir);
		final LuceneItemIndex srcIndex = new LuceneItemIndex(indexDir(srcDir));
		final LockProvider srcLockProvider = srcStorage; // File based locking
		
		try (IItemDAO srcDao = new XMLItemDAO(srcStorage, srcIndex, srcLockProvider)) {
		
			final LocalXmlStorage dstStorage = new LocalXmlStorage(dstDir);
			final LuceneItemIndex dstIndex = new LuceneItemIndex(indexDir(dstDir));
			final LockProvider dstLockProvider = srcStorage; // File based locking

			try (IItemDAO dstDao = new XMLItemDAO(dstStorage, dstIndex, dstLockProvider)) {
				// Just call on source index to get all items
				final List<Class<? extends Item>> allTypes = ItemTypes.getAllTypesList();
		
				final IndexSearchCursor cursor = srcIndex.search(allTypes, null, null, null, null, null);
				
				final List<String> itemIds = cursor.getItemIDs(0, Integer.MAX_VALUE);
		
				final ItemId [] itemAndUserIds = srcIndex.expandToItemIdUserId(itemIds.toArray(new String[itemIds.size()]));
				
				int itemNo = 1;
				
				for (ItemId itemAndUserId : itemAndUserIds) {

					final String userId = itemAndUserId.getUserId();
					final String itemId = itemAndUserId.getItemId();

					System.out.println("Indexing item " + itemNo + " out of " + itemAndUserIds.length + ": " + itemId);

					++ itemNo;

					final IFoundItem foundItem = srcDao.getItem(userId, itemId);
					
					final Item item = foundItem.getItem();
					
					// Reindex into destination DAO
					final String newItemId = dstDao.addItem(userId, item);

					// Also reindex photos
					final List<IFoundItemPhotoThumbnail> thumbnails = srcDao.getPhotoThumbnails(userId, itemId);
					
					for (IFoundItemPhotoThumbnail thumbnail : thumbnails) {
						final ItemPhoto photo = srcDao.getItemPhoto(userId, thumbnail);

						final InputStream thumbnailInputStream = new ByteArrayInputStream(thumbnail.getData());
						final InputStream photoInputStream = new ByteArrayInputStream(photo.getData());
						
						dstDao.addPhotoAndThumbnailForItem(
								userId, newItemId,
								item.getClass(),
								thumbnailInputStream, thumbnail.getMimeType(), thumbnail.getData().length, thumbnail.getWidth(), thumbnail.getHeight(),
								photoInputStream, photo.getMimeType(), photo.getData().length);
					}
				}
			}
		}
	}
	
	private static String indexDir(File baseDir) {
		return new File(baseDir, "index").getAbsolutePath();
	}
}
