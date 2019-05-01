package com.test.salesportal.gallery.stubs;

import java.util.ArrayList;
import java.util.List;

public final class GalleryViewOperations {

	private final List<GalleryViewOperation> operations;
	
	public GalleryViewOperations() {
		this.operations = new ArrayList<>();
	}

	public void clear() {
		this.operations.clear();
	}

	public GalleryViewOperations createUpperPlaceHolder() {
		add(new CreateUpperPlaceHolderOperation());

		return this;
	}

	public GalleryViewOperations clearRenderContainer() {
		add(new ClearRenderContainerOperation());

		return this;
	}

	public GalleryViewOperations appendPlaceholderToRenderContainer() {
		add(new AppendPlaceHolderToRenderContainer());

		return this;
	}
	
	public GalleryViewOperations appendRowToRenderContainer(int rowNo) {
		add(new AppendRowToRenderContainer(rowNo));

		return this;
	}
	
	public GalleryViewOperations prependRowToRenderContainer(int rowNo) {
		add(new PrependRowToRenderContainer(rowNo));

		return this;
	}

	public GalleryViewOperations createRowContainer(int rowNo) {
		add(new CreateRowOperation(rowNo));
		
		return this;
	}
	
	public GalleryViewOperations appendItemToRowContainer(int rowNo, int itemIndexInRow, int itemIndex) {
		add(new AppendItemToRowContainerOperation(rowNo, itemIndexInRow, itemIndex));

		return this;
	}
	
	public GalleryViewOperations replaceProvisionalWithComplete(int rowNo, int itemIndexInRow, int itemIndex) {
		add(new ReplaceProvisionalWithCompleteOperation(rowNo, itemIndexInRow, itemIndex));

		return this;
	}
	
	public GalleryViewOperations removeRowFromContainer(int rowNo) {
		add(new RemoveRowFromContainerOperation(rowNo));

		return this;
	}

	public GalleryViewOperations setPlaceHolderHeight(int heightPx) {
		add(new SetPlaceHolderHeightOperation(heightPx));

		return this;
	}

	private void add(GalleryViewOperation operation) {
		if (operation == null) {
			throw new IllegalArgumentException("operation == null");
		}

		this.operations.add(operation);
	}
	
	@Override
	public String toString() {
		return operations.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GalleryViewOperations other = (GalleryViewOperations) obj;
		if (operations == null) {
			if (other.operations != null)
				return false;
		} else if (!operations.equals(other.operations))
			return false;
		return true;
	}



	private static abstract class GalleryViewOperation {

		@Override
		public boolean equals(Object obj) {
			
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;

			return true;
		}
	}
	
	private static class CreateUpperPlaceHolderOperation extends GalleryViewOperation {
		
		@Override
		public String toString() {
			return "CreateUpperPlaceHolder";
		}
	}
	
	private static class AppendPlaceHolderToRenderContainer extends GalleryViewOperation {

		@Override
		public String toString() {
			return "AppendPlaceHolder";
		}
	}
	
	private static class ClearRenderContainerOperation extends GalleryViewOperation {
		
		@Override
		public String toString() {
			return "ClearRenderContainer";
		}
	}

	private static class AppendRowToRenderContainer extends GalleryViewOperation {
		private final int rowNo;

		public AppendRowToRenderContainer(int rowNo) {
			this.rowNo = rowNo;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppendRowToRenderContainer other = (AppendRowToRenderContainer) obj;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "AppendRow [" + rowNo + "]";
		}
	}

	private static class PrependRowToRenderContainer extends GalleryViewOperation {
		private final int rowNo;

		public PrependRowToRenderContainer(int rowNo) {
			this.rowNo = rowNo;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			PrependRowToRenderContainer other = (PrependRowToRenderContainer) obj;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "PrependRow [" + rowNo + "]";
		}
	}

	private static class CreateRowOperation extends GalleryViewOperation {
		private final int rowNo;

		public CreateRowOperation(int rowNo) {
			this.rowNo = rowNo;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			CreateRowOperation other = (CreateRowOperation) obj;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}
		
		@Override
		public String toString() {
			return "CreateRow [" + rowNo +  "]";
		}
	}

	private static class AppendItemToRowContainerOperation extends GalleryViewOperation {
		private final int rowNo; // index into total number of rows (no matter hov many are rendered at any given moment)
		private final int itemIndexInRow; // Index into row
		private final int itemIndex; // Index into virtual array
		
		public AppendItemToRowContainerOperation(int rowNo, int itemIndexInRow, int itemIndex) {
			this.rowNo = rowNo;
			this.itemIndexInRow = itemIndexInRow;
			this.itemIndex = itemIndex;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			AppendItemToRowContainerOperation other = (AppendItemToRowContainerOperation) obj;
			if (itemIndex != other.itemIndex)
				return false;
			if (itemIndexInRow != other.itemIndexInRow)
				return false;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "AppendItemToRow [" + rowNo + ", " + itemIndexInRow
					+ ", " + itemIndex + "]";
		}
	}

	private static class ReplaceProvisionalWithCompleteOperation extends GalleryViewOperation {
		private final int rowNo; // index into total number of rows (no matter hov many are rendered at any given moment)
		private final int itemIndexInRow; // Index into row
		private final int itemIndex; // Index into virtual array

		public ReplaceProvisionalWithCompleteOperation(int rowNo, int itemIndexInRow, int itemIndex) {
			this.rowNo = rowNo;
			this.itemIndexInRow = itemIndexInRow;
			this.itemIndex = itemIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + itemIndex;
			result = prime * result + itemIndexInRow;
			result = prime * result + rowNo;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			ReplaceProvisionalWithCompleteOperation other = (ReplaceProvisionalWithCompleteOperation) obj;
			if (itemIndex != other.itemIndex)
				return false;
			if (itemIndexInRow != other.itemIndexInRow)
				return false;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "ProvisionalToComplete [" + rowNo + ", " + itemIndexInRow
					+ ", " + itemIndex + "]";
		}
	}

	private static class RemoveRowFromContainerOperation extends GalleryViewOperation {
		private final int rowNo; // index into total number of rows (no matter hov many are rendered at any given moment)
		
		RemoveRowFromContainerOperation(int rowNo) {
			this.rowNo = rowNo;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			RemoveRowFromContainerOperation other = (RemoveRowFromContainerOperation) obj;
			if (rowNo != other.rowNo)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "RemoveRow [" + rowNo +  "]";
		}
	}
	
	private static class SetPlaceHolderHeightOperation extends GalleryViewOperation {

		private final int heightPx;

		public SetPlaceHolderHeightOperation(int heightPx) {
			this.heightPx = heightPx;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!super.equals(obj))
				return false;
			if (getClass() != obj.getClass())
				return false;
			SetPlaceHolderHeightOperation other = (SetPlaceHolderHeightOperation) obj;
			if (heightPx != other.heightPx)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "SetPlaceHolderHeight [" + heightPx +  "]";
		}
	}
}
