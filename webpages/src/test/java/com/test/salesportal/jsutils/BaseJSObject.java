package com.test.salesportal.jsutils;

import java.util.Collection;
import java.util.Set;

import jdk.nashorn.api.scripting.JSObject;

@SuppressWarnings("restriction")
public abstract class BaseJSObject implements JSObject {

	private final JSObjectType type;

	BaseJSObject(JSObjectType type) {
		
		if (type == null) {
			throw new IllegalArgumentException("type == null");
		}
		
		this.type = type;
	}

	@Override
	public Object call(Object arg0, Object... arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object eval(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getClassName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getMember(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object getSlot(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasMember(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasSlot(int arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isArray() {
		return type == JSObjectType.ARRAY;
	}

	@Override
	public boolean isFunction() {
		return type == JSObjectType.FUNCTION;
	}

	@Override
	public boolean isInstance(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isInstanceOf(Object arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStrictFunction() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> keySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object newObject(Object... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeMember(String arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMember(String arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setSlot(int arg0, Object arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public double toNumber() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<Object> values() {
		throw new UnsupportedOperationException();
	}
}
