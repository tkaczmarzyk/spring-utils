package net.kaczmarzyk.springutils;

public class HelloBeanImpl implements HelloBean {

	@Override
	public String hello() {
		return "Hello, World!";
	}
}
