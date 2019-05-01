package com.test.salesportal.model.cv;

public interface ItemVisitor<T, R> {

	R onJob(Job job, T param);
	
	R onSelfEmployed(SelfEmployed selfEmployed, T param);
	
	R onEducation(Education education, T param);
	
	R onProject(Project project, T param);
	
	R onCustom(Custom custom, T param);
}
