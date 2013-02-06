package net.kaczmarzyk.springutils;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.AbstractApplicationContext;

public class MockApplicationContet extends AbstractApplicationContext {

	private Map<String, Object> beans = new HashMap<String, Object>();
	
	
	public MockApplicationContet(String beanName, Object bean) {
		beans.put(beanName, bean);
	}
	
	@Override
	public Object getBean(String name) throws BeansException {
		Object bean = beans.get(name);
		if (bean != null) {
			return bean;
		} else {
			throw new NoSuchBeanDefinitionException(name);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T extends Object> T getBean(String name, java.lang.Class<T> requiredType) {
		Object bean = getBean(name);
		if (bean != null && requiredType.isAssignableFrom(bean.getClass())) {
			return (T) bean;
		} else {
			throw new NoSuchBeanDefinitionException(name);
		}
	}
	
	@Override
	protected void refreshBeanFactory() throws BeansException, IllegalStateException {
	}

	@Override
	protected void closeBeanFactory() {
	}

	@Override
	public ConfigurableListableBeanFactory getBeanFactory() throws IllegalStateException {
		throw new UnsupportedOperationException("not implemented");
	}

}
