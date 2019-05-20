package com.test.salesportal.model.items.sports;

import javax.persistence.MappedSuperclass;

import com.test.salesportal.model.items.sales.PhysicalItem;

// TODO makes sense to split in types likes this?

//@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@MappedSuperclass
public abstract class SportsItem extends PhysicalItem {

}
