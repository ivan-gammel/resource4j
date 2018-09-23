package com.github.resource4j.spring;

import com.github.resource4j.spring.test.TestConfiguration;
import example.document.DocumentTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class ResourceObjectAutowiringIT implements ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	@Test
	public void testByteContentInjectedFromResourceFileWhenAutowiring() {
		AutowireCapableBeanFactory beanFactory = applicationContext.getAutowireCapableBeanFactory();
		DocumentTemplate bean = new DocumentTemplate();
		beanFactory.initializeBean(bean, "template");
		assertEquals(1612, bean.getLogo().length);
	}
}
