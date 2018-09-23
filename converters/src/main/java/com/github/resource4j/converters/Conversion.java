package com.github.resource4j.converters;

import java.util.Set;

public interface Conversion<F,T> {

    Set<ConversionPair> acceptedTypes();

    T convert(F fromValue, Class<T> toType, Object format) throws TypeCastException;

}
