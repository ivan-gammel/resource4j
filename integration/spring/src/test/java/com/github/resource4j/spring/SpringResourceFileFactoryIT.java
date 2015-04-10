package com.github.resource4j.spring;

import static com.github.resource4j.resources.resolution.ResourceResolutionContext.in;
import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.github.resource4j.files.ResourceFile;
import com.github.resource4j.resources.Resources;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/META-INF/spring/testContext.xml")
public class SpringResourceFileFactoryIT {

	@Autowired
	private Resources resources;
	
	@Test
	public void testResourceFileLoadedBySpringFactory() {
		ResourceFile file = resources.contentOf("test.txt", in(Locale.US));
		assertTrue(file instanceof SpringResourceFile);
	}
	
}