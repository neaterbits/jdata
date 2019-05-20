package com.test.salesportal.rest.search.paged;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.test.salesportal.common.IOUtil;
import com.test.salesportal.dao.ItemStorageException;
import com.test.salesportal.model.items.base.ItemTypes;
import com.test.salesportal.rest.search.BaseSearchService;
import com.test.salesportal.rest.search.SearchItemResult;
import com.test.salesportal.rest.search.model.criteria.SearchCriterium;


@Path("/searchpaged")
public class PagedSearchService extends BaseSearchService<SearchItemResult, PagedSearchResult> {
	
	private final ItemTypes itemTypes;
	private final PagedSearchLogic searchLogic;
	
	public PagedSearchService(String localFileDir, ItemTypes itemTypes) {
		super(localFileDir);
		
		this.itemTypes = itemTypes;
		
		this.searchLogic = new PagedSearchLogic(itemTypes);
	}

	@GET
	@Path("search")
	// TODO check that we adhere to best practices for pageNo and itemsPerPage
	public PagedSearchResult search(String [] types, String freeText, SearchCriterium [] criteria, String [] sortOrder, String [] fields, Integer pageNo, Integer itemsPerPage, Boolean testdata, HttpServletRequest request) {
		return searchLogic.search(
				types,
				freeText,
				criteria,
				sortOrder,
				fields,
				pageNo,
				itemsPerPage,
				testdata,
				getSearchDAO(request));
	}


	public byte[] searchReturnCompressed(String freeText, String [] types, SearchCriterium [] criteria, String[] sortOrder, String [] fields, Integer pageNo, Integer itemsPerPage, HttpServletRequest request) {

		// Return result as a compressed array (non JSON) of
		// - IDs, in order
		// titles, in order
		// thumbnail sizes (byte width, byte height), in order
		// fields, in orders

		final PagedSearchResult searchResult = this.search(
				types,
				freeText, criteria, sortOrder,
				fields,
				pageNo, itemsPerPage,
				false,
				request);

		// Add information to compression stream
		
		// TODO compress
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		final OutputStream outputStream = baos; // TODO compression
		
		try (DataOutputStream dataOut = new DataOutputStream(outputStream)) {
		
			try {
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeUTF(item.getId());
				}
				
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeUTF(item.getTitle());
				}
				
				for (SearchItemResult item : searchResult.getItems()) {
					dataOut.writeByte(item.getThumbWidth());
					dataOut.writeByte(item.getThumbHeight());
				}
			}
			catch (IOException ex) {
				throw new IllegalStateException("Failed to write to output stream");
			}
		} catch (IOException ex) {
			throw new IllegalStateException("Exception while closing output stream", ex);
		}

		return baos.toByteArray();
	}
		

	@Path("/thumbnails")
	// Get item thumbnails as one big compressed JPEG? Or as a stream of JPEGs?
	public byte [] getThumbnails(String [] itemIds, HttpServletRequest request) {

		// Compact item IDs
		final List<String> filtered = Arrays.stream(itemIds)
			.filter(id -> id != null)
			.map(id -> id.trim())
			.filter(id -> !id.isEmpty())
			.collect(Collectors.toList());
		
		itemIds = filtered.toArray(new String[filtered.size()]);

		// Return thumbnails as concatenated JPEGs
		InputStream inputStream = null;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream(50000);

		try {
			inputStream = getItemRetrievalDAO(request, itemTypes).retrieveAndConcatenateThumbnails(itemIds);

			IOUtil.copyStreams(inputStream, baos);
		} catch (ItemStorageException ex) {
			throw new IllegalStateException("Could not get thumbnails");
		} catch (IOException ex) {
			throw new IllegalStateException("Could not read thumbnails");
		}
		finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}

		return baos.toByteArray();
	}
}
