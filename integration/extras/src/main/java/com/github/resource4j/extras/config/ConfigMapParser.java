package com.github.resource4j.extras.config;

import com.github.resource4j.ResourceObject;
import com.github.resource4j.ResourceObjectException;
import com.github.resource4j.objects.parsers.AbstractValueParser;
import com.github.resource4j.objects.parsers.BundleParser;
import com.github.resource4j.resources.discovery.ContentType;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigParseOptions;
import com.typesafe.config.ConfigSyntax;
import com.typesafe.config.ConfigValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.github.resource4j.extras.config.ConfigParser.config;
import static com.typesafe.config.ConfigParseOptions.defaults;

@SuppressWarnings("WeakerAccess")
@ContentType(extension = ".config", mimeType = "text/x-java-properties")
public class ConfigMapParser extends AbstractValueParser<Map<String, String>> implements BundleParser {

	private ConfigParser parser;
	
	public static ConfigMapParser configMap() {
		return new ConfigMapParser();
	}
	
	public static ConfigMapParser configMap(ConfigParseOptions options) {
		return new ConfigMapParser(options);
	}
	

	public ConfigMapParser() {
		parser = config(defaults()
				.setSyntax(ConfigSyntax.CONF)
				.setAllowMissing(false));
	}
	
	public ConfigMapParser(ConfigParseOptions options) {
		parser = config(options);
	}


	@Override
	protected Map<String, String> parse(ResourceObject file) throws IOException,
			ResourceObjectException {
		Config config = file.parsedTo(parser).asIs();

		Map<String, String> result = new HashMap<String, String>();
		Set<Entry<String, ConfigValue>> entrySet = config.entrySet();
		for (Entry<String, ConfigValue> entry : entrySet) {
			ConfigValue value = entry.getValue();
			switch (value.valueType()) {
			case BOOLEAN:
			case NUMBER:
			case STRING:
				result.put(entry.getKey(), String.valueOf(value.unwrapped()));
				break;
			default:
				result.put(entry.getKey(), value.render());
			}

		}
		return result;
	}

}
