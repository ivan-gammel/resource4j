package com.github.resource4j.generic;

import com.github.resource4j.MandatoryString;
import com.github.resource4j.MandatoryValue;
import com.github.resource4j.MissingValueException;
import com.github.resource4j.ResourceKey;

/**
 *
 * @author Ivan Gammel
 * @since 1.0
 */
public class GenericMandatoryString extends GenericResourceString implements MandatoryString {

    protected GenericMandatoryString(String resolvedSource, ResourceKey key, String value) {
        this(resolvedSource, key, value, null);
    }

    protected GenericMandatoryString(String resolvedSource, ResourceKey key, String value, Throwable cause) {
        super(resolvedSource, key, value);
        if (value == null) {
            throw new MissingValueException(key, cause);
        }
    }

    @Override
    public <T> MandatoryValue<T> ofType(Class<T> type) {
        return new GenericMandatoryValue<>(resolvedSource, key, as(type));
    }

	@Override
	public String resolvedSource() {
		return resolvedSource;
	}

}