package com.test.cv.model.items;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;

// TODO makes sense to split in types likes this?

//@Entity
//@Inheritance(strategy=InheritanceType.SINGLE_TABLE)
@MappedSuperclass
public abstract class SportsItem extends PhysicalItem {

}
