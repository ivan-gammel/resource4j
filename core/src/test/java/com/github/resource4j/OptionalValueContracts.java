package com.github.resource4j;

import org.junit.Test;

import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public abstract class OptionalValueContracts extends ResourceValueContracts {
	
	@Override
	protected abstract OptionalValue<String> createValue(ResourceKey key, String content);

	@Test
	public void testOrDefaultReturnsOptionalValueContentWhenNotNull() throws Exception {
		String content = someContent();
		OptionalValue<String> value = createValue(anyKey(), content);
		assertSame(content, value.orDefault("Another content"));
	}
	
	@Test
	public void testOrReturnsMandatoryValueWithSameContentWhenNotNull() throws Exception {
		String content = someContent();
		OptionalValue<String> value = createValue(anyKey(), content);
		MandatoryValue<String> mandatoryValue = value.or("Another content");
		assertSame(value.asIs(), mandatoryValue.asIs());
		assertSame(value.key(), mandatoryValue.key());
	}

	@Test
	public void testOrReturnsMandatoryValueWithDefaultContentWhenNull() throws Exception {
		String content = someContent();
		OptionalValue<String> value = createValue(anyKey(), null);
		MandatoryValue<String> mandatoryValue = value.or(content);
		assertSame(content, mandatoryValue.asIs());
		assertSame(value.key(), mandatoryValue.key());
	}

	@Test(expected = NullPointerException.class)
	public void testOrThrowsNullPointerExceptionIfDefaultValueIsNull() throws Exception {
		OptionalValue<String> value = createValue(anyKey(), someContent());
		value.or((String) null);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test(expected = NullPointerException.class)
	public void testOrThrowsNullPointerExceptionIfSupplierIsNull() throws Exception {
		OptionalValue<String> value = createValue(anyKey(), someContent());
		value.orElseGet((Supplier) null);
	}
	
	@Test(expected = NullPointerException.class)
	public void testOrDefaultNullPointerExceptionIfDefaultValueIsNull() throws Exception {
		OptionalValue<String> value = createValue(anyKey(), someContent());
		value.orDefault(null);
	}

	@Test
	public void testNotNullReturnsMandatoryValueWithSameContentWhenNotNull() throws Exception {
		String content = someContent();
		OptionalValue<String> value = createValue(anyKey(), content);
		MandatoryValue<String> mandatoryValue = value.notNull();
		assertSame(value.asIs(), mandatoryValue.asIs());
		assertSame(value.key(), mandatoryValue.key());
	}

	@Test(expected = MissingValueException.class)
	public void testNotNullThrowsMissingValueExceptionWhenNull() throws Exception {
		OptionalValue<String> value = createValue(anyKey(), null);
		value.notNull();
	}

	@Test
	public void testStdReturnsJavaUtilOptional() {
		OptionalValue<String> value = createValue(anyKey(), someContent());
		Optional<String> s = value.std();
		assertEquals(s.get(), value.asIs());
	}
	
	@Test
	public void testStdReturnsJavaUtilOptionalOfNull() {
		OptionalValue<String> value = createValue(anyKey(), null);
		Optional<String> s = value.std();
		assertFalse(s.isPresent());
	}
	
}
