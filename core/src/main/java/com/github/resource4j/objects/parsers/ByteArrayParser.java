package com.github.resource4j.objects.parsers;

import com.github.resource4j.OptionalValue;
import com.github.resource4j.ResourceKey;
import com.github.resource4j.ResourceObject;
import com.github.resource4j.ResourceObjectException;
import com.github.resource4j.values.GenericOptionalValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteArrayParser extends AbstractParser<byte[], OptionalValue<byte[]>> {

	private static final ByteArrayParser INSTANCE = new ByteArrayParser();

	@Override
	protected OptionalValue<byte[]> createValue(ResourceObject object,
			ResourceKey key, byte[] value, Throwable suppressedException) {
		return new GenericOptionalValue<byte[]>(object.actualName(), key, value, suppressedException);
	}

	@Override
	protected byte[] parse(ResourceObject object) throws IOException, ResourceObjectException {
		InputStream is = object.asStream();
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[0xFFFF];
			for (int len; (len = is.read(buffer)) != -1;) {
				os.write(buffer, 0, len);
			}
			os.flush();
			return os.toByteArray();
		} 
	}

	public static ByteArrayParser getInstance() {
		return INSTANCE;
	}
}
