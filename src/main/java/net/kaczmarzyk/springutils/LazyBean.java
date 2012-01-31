package net.kaczmarzyk.springutils;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public abstract class LazyBean implements ApplicationContextAware {

	private ApplicationContext appCtx;
	private String beanName;
	private Object bean;
	
	public void setApplicationContext(ApplicationContext applicationContext) {
		appCtx = applicationContext;
	}
	
	public Object getBean() {
		assertContext();
		if (bean == null) {
			bean = appCtx.getBean(beanName);
		}
		return bean;
	}
	
	public Class<?> getBeanType() {
		assertContext();
		return appCtx.getType(beanName);
	}

	private void assertContext() {
		if (appCtx == null) {
			throw new IllegalStateException("application context not set");
		}
	}
	
	public static LazyBean createProxy(String beanName) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(LazyBean.class);
		
		MethodHandler handler = new MethodHandler() {
			@Override
			public Object invoke(Object selfObj, Method thisMethod, Method proceed, Object[] args) throws Exception {
				LazyBean self = (LazyBean) selfObj;
				if (thisMethod.getDeclaringClass().isAssignableFrom(self.getBeanType())) {
					return thisMethod.invoke(self.getBean(), args);
				} else {
					return proceed.invoke(self, args);
				}
			}
		};
		
		try {
			LazyBean lazyBean = (LazyBean) factory.create(new Class<?>[0], new Object[0], handler);
			lazyBean.beanName = beanName;
			return lazyBean;
		} catch (RuntimeException re) {
			throw re;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
