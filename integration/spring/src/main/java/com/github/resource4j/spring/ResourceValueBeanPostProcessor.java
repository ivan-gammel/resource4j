package com.github.resource4j.spring;

import com.github.resource4j.ResourceKey;
import com.github.resource4j.resources.ResourceProvider;
import com.github.resource4j.resources.Resources;
import com.github.resource4j.spring.annotations.InjectBundle;
import com.github.resource4j.spring.annotations.InjectResource;
import com.github.resource4j.spring.annotations.InjectValue;
import com.github.resource4j.spring.annotations.support.InjectResourceCallback;
import com.github.resource4j.spring.annotations.support.InjectValueCallback;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.github.resource4j.ResourceKey.key;
import static org.springframework.util.ReflectionUtils.doWithFields;

@SuppressWarnings("deprecation")
public class ResourceValueBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

	private BeanFactory beanFactory;
	
	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	private Supplier<Resources> resources() {
		return () -> beanFactory.getBean(Resources.class);
	}

	@Override
	public Object postProcessAfterInitialization(Object bean, String beanName)
			throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessBeforeInitialization(final Object bean,
			final String beanName) throws BeansException {
		if (bean.getClass().isSynthetic() || Proxy.isProxyClass(bean.getClass())) {
            return bean;
        }
		Supplier<ResourceProvider> provider = getResourceProvider(bean);

		doWithFields(bean.getClass(), 
				new InjectValueCallback(bean, beanFactory, resources(), provider),
				field -> field.isAnnotationPresent(InjectValue.class));
		doWithFields(bean.getClass(), 
				new InjectResourceCallback(bean, beanName, beanFactory, resources()),
				field -> field.isAnnotationPresent(InjectResource.class));
		return bean;
	}

	private Supplier<ResourceProvider> getResourceProvider(Object bean) {
		Class<? extends Object> beanClass = bean.getClass();
		Supplier<ResourceProvider> provider = getProvider(beanClass.getName(), beanClass);
		if (provider == null) {
			for (Package pckg : packagesOf(beanClass)) {
				provider = getProvider(pckg.getName(), pckg);
				if (provider != null) {
					return provider;
				}
			}
		}
		return provider;
	}

	private Iterable<Package> packagesOf(Class<?> clazz) {
		List<Package> packages = new ArrayList<>();

		String name = clazz.getPackage().getName();
		String[] components = name.split("\\.");
		
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < components.length; i++) {
			if (builder.length() > 0) {
				builder.append('.');
			}
			builder.append(components[i]);
			Package pckg = Package.getPackage(builder.toString());
			if (pckg != null) {
				packages.add(pckg);
			}
		}
		return packages;
	}

	private Supplier<ResourceProvider> getProvider(String name, AnnotatedElement element) {
		InjectBundle annotation = element.getAnnotation(InjectBundle.class);
		if (annotation == null) {
			return null;
		}
		String id = annotation.id().isEmpty() ? null : annotation.id();
		ResourceKey key;
		if (annotation.value().isEmpty()) {
			key = key(name, id);
		} else {
			key = key(annotation.value(), id);
		}
		return () -> resources().get().forKey(key);
	}
	


}
