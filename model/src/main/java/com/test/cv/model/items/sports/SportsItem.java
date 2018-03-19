package com.test.cv.model.items.sports;

import javax.persistence.MappedSuperclass;

import com.test.cv.model.items.PhysicalItem;

// TODO makes sense to split in types likes this?

//@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@MappedSuperclass
public abstract class SportsItem extends PhysicalItem {

}
