package com.github.resource4j.resources;

import static com.github.resource4j.ResourceKey.bundle;
import static com.github.resource4j.resources.cache.CachedValue.cached;
import static com.github.resource4j.resources.cache.CachedValue.missingValue;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.github.resource4j.OptionalString;
import com.github.resource4j.ResourceKey;
import com.github.resource4j.files.MissingResourceFileException;
import com.github.resource4j.files.ResourceFile;
import com.github.resource4j.generic.GenericOptionalString;
import com.github.resource4j.resources.cache.CachedValue;
import com.github.resource4j.resources.cache.ResourceCache;
import com.github.resource4j.resources.cache.SimpleResourceCache;
import com.github.resource4j.resources.resolution.ResourceResolutionContext;

/**
 *
 * @author Ivan Gammel
 * @since 1.0
 */
public class DefaultResources extends CustomizableResources {

    /**
     *
     */
    public final static ResourceKey DEFAULT_APPLICATION_RESOURCES = bundle("i18n.resources");

    private ResourceCache<OptionalString> valueCache = new SimpleResourceCache<>();

    private ResourceCache<ResourceFile> fileCache = new SimpleResourceCache<>();

    public DefaultResources() {
        this.defaultResourceBundle = DEFAULT_APPLICATION_RESOURCES;
    }

    public DefaultResources(String defaultBundle) {
        this.defaultResourceBundle = ResourceKey.bundle(defaultBundle);
    }

    /**
     *
     * @param key
     * @param locale
     * @return
     */
    @Override
	public OptionalString get(ResourceKey key, ResourceResolutionContext context) {
        // first, try to get the value from cache
        CachedValue<OptionalString> cachedValue = valueCache.get(key, context);
        OptionalString result = null;
        if (cachedValue != null) {
        	result = cachedValue.get();
        }

        Throwable suppressedException = null;
        synchronized (valueCache) {
            // value not in cache, load related bundles to cache
            suppressedException = tryLoadToCache(key, context);
	        // finally, try again
	        cachedValue = valueCache.get(key, context);
	        if (cachedValue != null) {
	            result = cachedValue.get();
	        }
	        // value not found: record this search result to cache
	        valueCache.put(key, context, missingValue(OptionalString.class));
        }
        if (result == null) {
        	result = new GenericOptionalString(null, key, null, suppressedException);
        } 
        return result;
    }

	private Throwable tryLoadToCache(ResourceKey key, ResourceResolutionContext context) {
		String bundleName = getBundleParser().getResourceFileName(key);
		String defaultBundleName = getBundleParser().getResourceFileName(defaultResourceBundle);
		String[] bundleOptions = bundleName != null
		        ? new String[] { bundleName, defaultBundleName }
		        : new String[] { defaultBundleName };
		List<String> options = getFileEnumerationStrategy().enumerateFileNameOptions(bundleOptions, context);
		Throwable suppressedException = null;
		boolean found = false;
		for (String option : options) {
		    try {
		        ResourceFile file = getFileFactory().getFile(key, option);
		        Map<String, String> properties = getBundleParser().parse(file);
		        for (Map.Entry<String, String> property : properties.entrySet()) {
		            String bundle = key.getBundle();
		            String id = property.getKey();
		            ResourceKey propertyKey = bundle(bundle).child(id);
		            String propertyValue = property.getValue();
		            OptionalString string = new GenericOptionalString(file.resolvedName(), key, propertyValue);
		            valueCache.putIfAbsent(propertyKey, context, cached(string));
		            if (key.equals(propertyKey)) {
		            	found = true;
		            }
		        }
		    } catch (MissingResourceFileException e) {
		    	suppressedException = e;
		    }
		}
		return found ? null : suppressedException;
	}

    @Override
    public ResourceFile contentOf(String name, ResourceResolutionContext context) {
        ResourceKey key = bundle(name);
        // first, try to find the file in cache
        CachedValue<ResourceFile> cachedFile = fileCache.get(key, context);
        if (cachedFile != null) {
            if (cachedFile.isMissing()) {
                throw new MissingResourceFileException(key);
            } else {
                return cachedFile.get();
            }
        }
        // not cached, try to find it
        List<String> options = getFileEnumerationStrategy().enumerateFileNameOptions(new String[] { name }, context);
        for (String option : options) {
            try {
                ResourceFile file = getFileFactory().getFile(key, option);
                file.asStream().close();
                fileCache.put(key, context, cached(file));
                return file;
            } catch (MissingResourceFileException e) {
            } catch (IOException e) {
            }
        }
        // file not found: record this search result in cache
        fileCache.put(key, context, missingValue(ResourceFile.class));
        throw new MissingResourceFileException(key);
    }
}
