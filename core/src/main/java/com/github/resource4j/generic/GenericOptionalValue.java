package com.github.resource4j.generic;

import com.github.resource4j.MandatoryValue;
import com.github.resource4j.MissingValueException;
import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceKey;

/**
 * Generic implementation of {@link OptionalValue}.
 * @param <V> type of managed value
 * @author Ivan Gammel
 * @since 1.0
 */
public class GenericOptionalValue<V> extends GenericResourceValue<V> implements OptionalValue<V> {

    private Throwable suppressedException;

    public GenericOptionalValue(String resolvedSource, ResourceKey key, V value) {
        super(resolvedSource, key, value);
    }
    
    public GenericOptionalValue(String resolvedSource, ResourceKey key, V value, Throwable suppressedException) {
        super(resolvedSource, key, value);
        this.suppressedException = suppressedException;
    }
    
    public GenericOptionalValue(String resolvedSource, ResourceKey key, Throwable suppressedException) {
        super(resolvedSource, key, null);
        this.suppressedException = suppressedException;
    }

    @Override
    public V orDefault(V defaultValue) throws IllegalArgumentException {
        if (defaultValue == null) throw new IllegalArgumentException("defaultValue");
        if (value == null) return defaultValue;
        return value;
    }

    @Override
    public MandatoryValue<V> or(V defaultValue) throws IllegalArgumentException {
        if (defaultValue == null) throw new IllegalArgumentException("defaultValue");
        return new GenericMandatoryValue<>(resolvedSource, key, value == null ? defaultValue : value);
    }

    @Override
    public MandatoryValue<V> notNull() throws MissingValueException {
        return new GenericMandatoryValue<>(resolvedSource, key, value, suppressedException);
    }
}
