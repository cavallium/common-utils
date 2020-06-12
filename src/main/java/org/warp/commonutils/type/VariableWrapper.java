package org.warp.commonutils.type;

public class VariableWrapper<T> {

	public volatile T var;

	public VariableWrapper(T value) {
		this.var = value;
	}
}
