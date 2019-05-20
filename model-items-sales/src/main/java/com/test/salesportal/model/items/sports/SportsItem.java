package com.test.salesportal.model.items.sports;

import javax.persistence.MappedSuperclass;

import com.test.salesportal.model.items.sales.PhysicalItem;

// TODO makes sense to split in types likes this?

//@Entity
@MappedSuperclass
public abstract class SportsItem extends PhysicalItem {

}
