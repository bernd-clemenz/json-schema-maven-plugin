# JSON Schema file generator

This Maven plugin generates JSON-Schema files from POJOs using
[Jackson](https://github.com/FasterXML/jackson-module-jsonSchema).

## Prerequisites
* Java 8 or higher
* Maven 3.6.2 or higher
* [Jackson](https://github.com/FasterXML/jackson-module-jsonSchema) in dependencies
* Model classes annotated with [@HyperSchema](https://fasterxml.github.io/jackson-module-jsonSchema/javadoc/2.9/com/fasterxml/jackson/module/jsonSchema/annotation/JsonHyperSchema.html)

## Usage

```xml
<plugins>
  <!-- your other plugins -->
  <plugin>
    <groupId>de.isc.maven</groupId>
    <artifactId>json-schema-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
      <packagesToScan>
        <item>your.domain</item>
        <item>your.other.domain</item>
      </packagesToScan>
      <baseClassName>your.domain.base.Thing</baseClassName>
      <outputDirectory>META-INF/json-schema</outputDirectory>
    </configuration>
    <executions>
      <execution>
        <id>build-json-schema</id>
        <phase>process-classes</phase>
        <goals>
          <goal>json-schema</goal>
        </goals>
      </execution>
    </executions>
  </plugin>
</plugins>
```

## Goals and phase

* **json-schema** is the only goal
* **process-classes** is the mandatory lifecycle phase for this plugin.

# Configuration

| Name | Description |
|------|------------|
| **packagesToScan** | A list of packages, where your model classes exist |
| **baseClassName** | Fully qualified of the base class of your model classes |
| **outputDirectory** | Output directory name, relative to Mavens output path |

* [Lifecycle reference](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)
* [Reflections](https://github.com/ronmamo/reflections)
    * [more on reflections library](https://code.google.com/archive/p/reflections/)
    
## History

Just created for use in some RESTFull API projects, where it proofed helpful.
