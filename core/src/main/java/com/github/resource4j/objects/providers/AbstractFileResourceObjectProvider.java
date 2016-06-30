package com.github.resource4j.objects.providers;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.objects.exceptions.ResourceObjectAccessException;
import com.github.resource4j.objects.providers.resolvers.DefaultObjectNameResolver;
import com.github.resource4j.objects.providers.resolvers.ObjectNameResolver;
import com.github.resource4j.resources.context.ResourceResolutionContext;

public abstract class AbstractFileResourceObjectProvider implements ResourceObjectProvider {

    private ObjectNameResolver resolver = new DefaultObjectNameResolver();

    protected AbstractFileResourceObjectProvider() {
    }

    protected AbstractFileResourceObjectProvider(ObjectNameResolver resolver) {
        this.resolver = resolver;
    }

    public void setResolver(ObjectNameResolver resolver) {
        this.resolver = resolver;
    }

    protected ObjectNameResolver resolver() {
        return resolver;
    }

    @Override
    public ResourceObject get(String name, ResourceResolutionContext context) throws ResourceObjectAccessException {
        String actualName = resolver.resolve(name, context);
        return get(name, actualName);
    }

    protected abstract ResourceObject get(String name, String actualName) throws ResourceObjectAccessException;

}