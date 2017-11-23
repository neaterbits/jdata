package com.test.cv.model;

public interface ItemVisitor<T, R> {

	R onJob(Job job, T param);
	
	R onSelfEmployed(SelfEmployed selfEmployed, T param);
	
	R onEducation(Education education, T param);
	
	R onCustom(Custom custom, T param);
}
