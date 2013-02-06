package net.kaczmarzyk.springutils;

import org.junit.Before;
import org.junit.Rule;

import static org.mockito.Mockito.*;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.context.ApplicationContext;

public class LazyBeanTest {

	private static final String BEAN_NAME = "testBean";
	private static final HelloBean wrappedBean = mock(HelloBean.class);
	
	private ApplicationContext mockCtx = new MockApplicationContet(BEAN_NAME, wrappedBean);
	
	private HelloBean lazy;
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Before
	public void init() {
		LazyBean factory = new LazyBean();
		factory.setBeanName(BEAN_NAME);
		factory.setApplicationContext(mockCtx);
		factory.setBeanType(HelloBean.class);
		lazy = (HelloBean) factory.getObject();
	}
	
	@Test
	public void shouldRethrowTheOriginalRuntimeException() {
		when(wrappedBean.hello()).thenThrow(new TestException());
		expected.expect(TestException.class);
		
		lazy.hello();
	}
	
	private class TestException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
