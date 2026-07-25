---
title: Recipe for a config.
description: This page defines a recipe (pre-written code snippet) for a config, that can be copied by a developer instead of manually writing it.
navigation:
  title: Config
---

# Config

Below is an example for a YAML and a Properties config.  
YAML configs can be used to store more complex data, like your custom classes for other constructs.

Properties configs can only store simple data like strings, booleans, numbers, etc.

```java [MyYamlConfig.java]
@VitalConfig.Info(name = "myconfig.yaml", processor = VitalYAMLConfigProcessor.class)
public class MyYamlConfig extends VitalConfig {
	@Property(String.class)
	private String myString;

	// You always have to explicitly define the generic parameters here	
	@Property(String.class)
	private List<String> myStringList;

	@Property(MyClass.class)
	private MyClass myClass;

	@Property(String.class, MyClass.class)
	private Map<String, MyClass> myClassMap;

	// Getters and setters

	public class MyClass {
		@Property(String.class)
		private String myClassString;

		// Custom config objects must have a default constructor
		public MyClass() {}

		// Getters and setters
	}
}
```

```java [MyConfig.java]
@VitalConfig.Info(name = "myconfig.properties", processor = VitalPropertiesConfigProcessor.class)
public class MyConfig extends VitalConfig {
	@Property(String.class)
	private String myString;

	@Property(Boolean.class)
	private boolean myBoolean;

	@Property(Integer.class)
	private int myInt;

	// Getters and setters
}
```
