package com.test.salesportal.rest.search.all.cache;

import com.test.salesportal.model.ItemAttribute;
import com.test.salesportal.model.items.ItemTypes;
import com.test.salesportal.model.items.sports.Snowboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class SearchRangeUtilTest extends TestCase {

	public void testSearchRangeMatchesDecimal() {
		
		final ItemAttribute widthAttribute = ItemTypes.getTypeInfo(Snowboard.class).getAttributes().getByName("width");
		assertThat(widthAttribute).isNotNull();
		
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, null, true, decimal(165), false)).isFalse();
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, null, true, decimal(165), true)).isTrue();

		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(160), true, decimal(165), false)).isFalse();
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(160), true, decimal(165), true)).isTrue();

		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(160), false, decimal(170), true)).isTrue();
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(160), true,  decimal(170), false)).isTrue();

		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(165), false, decimal(170), true)).isFalse();
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(165), true,  decimal(170), true)).isTrue();
		
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(165), false, null, true)).isFalse();
		assertThat(SearchRangeUtil.matches(decimal(165), widthAttribute, decimal(165), true,  null, true)).isTrue();
	}

	public void testSearchRangeMatchesInteger() {
		
		final ItemAttribute yearAttribute = ItemTypes.getTypeInfo(Snowboard.class).getAttributes().getByName("productionYear");
		assertThat(yearAttribute).isNotNull();
		
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, null, true, 2005, false)).isFalse();
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, null, true, 2005, true)).isTrue();

		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2000, true, 2005, false)).isFalse();
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2000, true, 2005, true)).isTrue();

		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2000, false, 2010, true)).isTrue();
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2000, true,  2010, false)).isTrue();

		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2005, false, 2010, true)).isFalse();
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2005, true,  2010, true)).isTrue();
		
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2005, false, null, true)).isFalse();
		assertThat(SearchRangeUtil.matches(2005, yearAttribute, 2005, true,  null, true)).isTrue();
	}

	private static BigDecimal decimal(int value) {
		return BigDecimal.valueOf(value);
	}
}
