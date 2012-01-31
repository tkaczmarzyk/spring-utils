/**
 * Copyright (c) 2013, Tomasz Kaczmarzyk.
 *
 * This file is part of SpringUtils
 *
 * SpringUtils is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * SpringUtils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with SpringUtils; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package net.kaczmarzyk.springutils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class LazyBean implements ApplicationContextAware, FactoryBean<Object> {

	private ApplicationContext appCtx;
	private String beanName;
	private Class<?> beanType;
	private Object wrappedBean;
	private Object wrapper;
	
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		appCtx = applicationContext;
	}
	
	private Object getBean() {
		assertContext();
		if (wrappedBean == null) {
			if (beanName != null) {
				wrappedBean = appCtx.getBean(beanName, beanType);
			} else {
				Map<String, ?> beans = new HashMap<String, Object>(appCtx.getBeansOfType(beanType));
				String wrapperName = null;
				for (Map.Entry<String, ?> entry : beans.entrySet()) {
					if (entry.getValue() instanceof Lazy) {
						wrapperName = entry.getKey();
						break;
					}
				}
				beans.remove(wrapperName);
				if (beans.size() != 1) {
					throw new IllegalStateException("No unique bean of type " + beanType + ". Found: " + beans.keySet());
				} else {
					wrappedBean = beans.values().iterator().next();
				}
			}
		}
		return wrappedBean;
	}
	
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}
	
	public void setBeanType(Class<?> beanType) {
		this.beanType = beanType;
	}
	
	private void assertContext() {
		if (appCtx == null) {
			throw new IllegalStateException("application context not set");
		}
	}
	
	@Override
	public Class<?> getObjectType() {
		return beanType;
	}

	@Override
	public boolean isSingleton() {
		return true; // TODO make it configurable
	}
	
	@Override
	public Object getObject() throws BeansException {
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(new Class<?>[] { beanType, Lazy.class }); // TODO support base classes, multiple interfaces
		
		MethodHandler handler = new MethodHandler() {
			@Override
			public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Exception {
				if (Arrays.asList("toString", "equals", "hashCode").contains(thisMethod.getName())) {
					return proceed.invoke(self, args);
				}
				if (thisMethod.getDeclaringClass().isAssignableFrom(beanType)) {
					return thisMethod.invoke(getBean(), args);
				} else {
					return proceed.invoke(self, args);
				}
			}
		};
		
		try {
			wrapper = factory.create(new Class<?>[0], new Object[0], handler);
			return wrapper;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public static interface Lazy {
	}
}
