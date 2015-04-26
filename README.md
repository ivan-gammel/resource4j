resource4j - resource loader for Java
=====================================
Overview
--------
Resource4j library is a replacement for Java ResourceBundle mechanism that supports complex i18n scenarios of large and legacy applications and provides safe access to key/value application configuration and arbitrary resource files. 
With well-designed API based on fluent DSL it is a solution for i18n just like SLF4J is a solution for logging 
abstraction.

Key features of this library:

 * Support of **Java SE resource bundles**, **custom resource file formats** and **data sources**
 * Fluent **DSL** for type conversions and enforcing not-null constraints
 * Locale-based and custom resolution of values
 * **Spring Framework** and **Thymeleaf** integration
 * Full support of **Test-Driven Development**
 * **Modular architecture** allows to use only the part of implementation you really need 

**resource4j-example-server** web application is a good example of how this library can be used to localize resources 
and page templates in web applications.

Quick Start
-----------
1. Add <code>resource4j-core.jar</code> to your classpath.
2. Create new instance of DefaultResources:
```Java
Resources resources = new DefaultResources();
```
3. Add resource bundles and content files to your classpath:
    <pre><code>/com/mycompany/data/Country-en_US.properties:
	discount=0.1</code></pre>
	<pre><code>/docs/EULA.txt:
	Lorem ipsum dolorem sit amet.</code></pre>         	
4. Get some value:

```Java 
BigDecimal localDiscount = resources.get(key(Country.class, "discount"), in(Locale.US))
			.notNull()
			.as(BigDecimal.class);
```			
5. Load content:

```Java
String eulaText = resources.contentOf("/docs/EULA.txt", in(Locale.US))
			.parsedTo(string())
			.asIs();
```			

Dependency management
---------------------
If you are using Maven, please, add following lines to your POM file:

	<dependency>
		<groupId>com.github.resource4j</groupId>
		<artifactId>resource4j-core</artifactId>
		<version>2.0.2</version>
	</dependency>
For integration with Spring and (optionally) Thymeleaf, add following:

	<dependency>
		<groupId>com.github.resource4j</groupId>
		<artifactId>resource4j-spring</artifactId>
		<version>2.0.2</version>
	</dependency>
	
Resources
---------
As you can see from the example above, the key component for loading resources is some implementation of Resources interface. This library provides two implementations of it:

1. **DefaultResources** - customizable implementation that discovers resource bundles and loads their 
	content to cache. This implementation is good enough to replace Java SE resource bundle 
	functionality and provides some additional capabilities which will be described below. 
2. **InMemoryResources** - the component that can store resource values and resource file content 
	in memory and is mostly useful for unit testing as a mock for more sophisticated implementation.

DefaultResources class allows to customize following settings:

1. Name of default resource bundle - a file containing default values for keys 
   that do not exist in other bundles. 
2. Naming convention for resource files via ResourceFileEnumerationStrategy implementation. 
   Default implementation uses the convention for Java SE resource bundles. See Javadoc for details.
3. Resource file factory implementation - to support loading of resources from database, 
   network or classpath (default).
4. Resource bundle parser implementation - to support loading resource values in different formats.
   Default implementation supports loading from .properties files.   

Resource keys
-------------
Each resource value is represented by a ResourceKey. Resource key represents both name of the resource bundle and name of the value in that bundle as a single immutable instance (compared to Java SE, where you have to work with them as separate strings). Compared to Cal10n, you don't need to create an enumeration for every set of resource keys (in large projects there can be thousands of them) and you may even generate resource keys dynamically using <code>child()</code> method.

Resource key can be created for arbitrary bundle, any class and enumeration constant. Below are some examples:
	
	// bundle=example.bundle id=someKey
	ResourceKey key1 = ResourceKey.key("example.bundle", "someKey"); 
	
	// bundle=example.MyService id=someKey
	ResourceKey key2 = ResourceKey.key(MyService.class, "someKey"); 
	
	// bundle=example.MyEnum id=SOME_VALUE
	ResourceKey key3 = ResourceKey.key(MyEnum.SOME_VALUE); 
	
	// bundle=example.MyService id=null
	ResourceKey bundle = ResourceKey.bundle(MyService.class); 
	
	// bundle=example.MyService id=someKey
	ResourceKey child = bundle.child("someKey"); 
	
	// bundle=example.MyService id=someKey.invalid
	ResourceKey invalid = child.child("invalid"); 

Resource values
---------------
Initially resource value is returned as OptionalString wrapper: using methods from fluent DSL you can convert it to MandatoryString (<code>notNull()</code> and <code>orDefault()</code> methods), to value of another type (method <code>as()</code>) or get the wrapped string. Here are some examples:

	OptionalString value = resources.get(key, in(Locale.US));
	String string = string.asIs(); // can be null here
	MandatoryString mandatory = value.notNull(); // may throw MissingValueException here 
	MandatoryString m2 = value.or("example"); 
	OptionalValue<Integer> integer = value.ofType(Integer.class);
	OptionalValue<Integer> intUS = value.ofType(Integer.class, DecimalFormat.getNumberInstance(Locale.US));
	int intValue = integer.asIs(); // autoboxing
	int intValue = integer.orDefault(5); // autoboxing
	
	long fluentValue = resources.get(key, in(currentLocale)).ofType(Long.class).orDefault();

Resource files and parsers
--------------------------
Sometimes you need to load value as binary content of a large file. You can use ResourceFile and ResourceParser API for it:

	ResourceFile document = resources.contentOf("document.xml", in(Locale.US));
	OptionalString string = document.parsedTo(ResourceParsers.string("utf-8"));
	
Or shorter:

    String s = resources
    	.contentOf("document.xml", in(Locale.US))
    	.parsedTo(string("utf-8"))
    	.notNull().asIs();

You can even implement your own parser (below is the example that uses XStream for XML parsing):

	@XStreamAlias("config")
	public class MyConfig {
	}

	public class ConfigParser extends AbstractValueParser<MyConfig> {
		private final static ConfigParser INSTANCE = new ConfigParser(); 
		private XStream xstream = new XStream();
		{
			xstream.processAnnotations(MyConfig.class);
		}
		public static ConfigParser configuration() {
			return INSTANCE;
		}
		protected MyConfig parse(ResourceFile file) throws IOException {
			return (MyConfig) xstream.fromXML(file.asStream());	
		}	
	}
	...
	import static MyConfig.configuration;
	...
	MyConfig config = resources
		.contentOf("config.xml")
		.parsedTo(configuration())
		.notNull().asIs();

Resolution contexts
-------------------
Resolution context is the way to specify which version of resource bundle to use for loading value in runtime. Resolution context in its simplest form consists of Locale object, defining the language and the country for which the value must be loaded. Resource4J library allows to define more complex resolution contexts, containing multiple components.
The **ResourceResolutionContext** class contains two useful factory methods for defining resolution context:
	
	ResourceResolutionContext in(Object... components);
	ResourceResolutionContext withoutContext();
	
The first one creates context defined by given components, the second one creates empty context.
Example of composite resolution context usage that allows different presentation of some resource values on the web:

	resources.get(key("bundle", id"), in(Locale.US, "WEB"));
	
When passed to Resources instance that uses basic naming strategy, this context defines following search order for resource bundles:

	
	bundle-en_US-WEB.properties
	bundle-en-WEB.properties
	bundle-WEB.properties
	bundle-en_US.properties	
	bundle-en.properties
	bundle.properties

You may even not use locale-based resolution:

	resources.get(key("database", "password"), in("localhost", "mywebapp", "debug"))

This query will look up the value in resource bundles in following sequence:
	
	database-localhost-mywebapp-debug.xml
	database-mywebapp-debug.xml
	database-localhost-debug.xml
	database-debug.xml
	database-localhost-mywebapp.xml
	database-mywebapp.xml
	database-localhost.xml
	database.xml	

Resource providers
------------------
Resource providers are the convenient way to work with a single bundle, addressing the values by plain String keys. If you have a class that references a lot of localization resources or configuration values,
using a ResourceProvider instead of Resources can be a good option:

	class MyClass {
		...
		ResourceProvider provider = resources.forKey(bundle(MyClass.class));
		OptionalString string = provider.get("someKey", in(Locale.US));
		...
	}

Testing
-------
Compared to Cal10n, Resource4J does not provide compile-time verification of resources - in large projects it may not always be possible to have all translations by the beginning of development, so developers need more flexible approach. Thus, this library prefers post-compile checks, allowing to write simple unit tests for verification of resource keys. Next versions of this library will provide support for post-deployment verification to support plugin architectures. 

Spring and Thymeleaf integration
================================
Overview
--------------------
You can easily configure an implementation of Resources interface in DI container. To use the advanced features of Resource4j Spring and Thymeleaf integration add resource4j-spring library dependency to your project. These features include:

1. Support of annotation-driven injection using **ResourceValueBeanPostProcessor** and **@AutowiredResource** annotation.
2. Support for Spring-based discovery of resource bundles.
3. Resource4j as message and resource resolver for Thymeleaf.
4. Default configuration for Spring and Thymeleaf (**ThymeleafResourceConfiguration** class).
 
Annotation-driven injection
---------------------------
With ResourceValueBeanPostProcessor configured, it is possible to inject resource providers,
resource value references and even the values of any type that can be instantiated from a string.
Below is an example:

```Java
	public class MyService {

		/*
		 * injected value is a provider for bundle(MyService.class)
		 */
		@AutowiredResource
		private ResourceProvider resources;

		/* Value is injected for key(MyService.class, "date") 
		 * and resolution context "WEB"
		 */
		@AutowiredResource(value="date", context="WEB")
		private MandatoryString dateFormat; 

		/* 
		 * Value is injected for key(MyService.class, "applicationName")
		 */
		@AutowiredResource
		private String applicationName;

	} 
```

Class **ResourceValueReference** is particularly useful in this scenario:

```Java		
	/* 
	 * value is injected for key(MyService.class, "population") 
	 */
	@AutowiredResource
	private ResourceValueReference population;
	...	
	System.out.println(population.fetch(in(Locale.US)).notNull().asIs());

	// or simply...
	System.out.println(population.in(Locale.US).notNull().asIs());
```

You may even inject file content:

```Java
package com.mycompany.legal;

class EULA {
    /* 
     * Value is injected from /logo.jpg
     */
    @AutowiredResource(source = "/logo.jpg")
    private byte[] logo;
	
	/* 
     * Content will be loaded on demand from /com/mycompany/legal/EULA.txt
     */
    @AutowiredResource(source = "*.txt")
    private ResourceFileReference content;
}
```