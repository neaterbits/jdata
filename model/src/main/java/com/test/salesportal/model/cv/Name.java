package com.test.salesportal.model.cv;

import java.util.List;

import com.test.salesportal.model.text.Text;
import com.test.salesportal.model.text.Translation;

public class Name extends Text {

	public Name() {
		super();
	}

	public Name(List<Translation> texts) {
		super(texts);
	}
}
