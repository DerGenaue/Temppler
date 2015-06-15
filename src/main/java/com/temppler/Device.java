package com.temppler;

/**
 * A Device with a lifecycle of init -> start <-> stop -> destroy
 * @author konrad
 *
 */
public abstract class Device {
		
	protected boolean active = false;
	
	public abstract void init();
	
	public abstract void start();
	
	public abstract void stop();
	
	public abstract void destroy();
}
