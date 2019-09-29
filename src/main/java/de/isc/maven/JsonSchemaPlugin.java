/*
 * (c) 2019 ISC Clemenz & Weinbrecht GmbH
 * Licensed under Apache 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */
package de.isc.maven;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

/**
 * Plugin for Maven to create JSON Schema files.
 *
 * @author Bernd Clemenz
 * @version 1.0.0
 * @since 1.0.0
 */
@Mojo(
  name = "json-schema",
  defaultPhase = LifecyclePhase.PROCESS_CLASSES,
  requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
  requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
@SuppressWarnings("unused")
public class JsonSchemaPlugin extends AbstractMojo {

  @Parameter(property = "packagesToScan", required = true)
  private String[] packagesToScan;

  @Parameter(
    property = "baseClassName",
    required = true,
    defaultValue = "java.lang.Object")
  private String baseClassName;

  @Parameter(property = "outputDirectory")
  private String outputDirectory;

  @Parameter(defaultValue = "${session}", required = true)
  private MavenSession m_session;

  @Parameter(defaultValue = "${project}", required = true)
  private MavenProject m_project;

  @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
  private String m_mavenOutputDirectory;

  /**
   * Constructor.
   */
  public JsonSchemaPlugin() {
      // empty
  }

  private void makeOutputDirectory()
  throws IOException {
    outputDirectory = m_mavenOutputDirectory + File.separator + outputDirectory;
    File dir = new File(outputDirectory);
    if(!dir.exists()) {
      Files.createDirectories(dir.toPath());
      getLog().debug("Create output directory: " + dir.getAbsolutePath());
    }
  }

  /**
   * Iterates compiled classes of given type an creates
   * JSON Schema file in configured output directory
   *
   * @throws MojoExecutionException if a class could not be loaded
   */
  public void execute()
  throws MojoExecutionException {
    getLog().info("Generate JSON schema files");

    Objects.requireNonNull(packagesToScan, "Need a package to scan");
    getLog().info("Package(s) to scan is: " + Arrays.deepToString(packagesToScan));
    if(packagesToScan.length == 0) {
      getLog().info("No packages to scan, nothing to do");
      return;
    }

    Objects.requireNonNull(baseClassName, "Need a baseClassName");
    Objects.requireNonNull(m_session, "Maven session not injected");
    Objects.requireNonNull(m_project, "Maven project not injected");
    getLog().info("Base class is: " + baseClassName);

    try {
      makeOutputDirectory();
      ClassLoader ldr = Thread.currentThread().getContextClassLoader();
      try {
        /*
         * Ensure classes can be loaded and processed via reflection
         */
        SchemaClassLoader schemaClassLoader = new SchemaClassLoader(m_project,
                                                                    getLog(),
                                                                    ldr);
        Thread.currentThread().setContextClassLoader(schemaClassLoader);

        /*
         * process classes and write Schema files.
         */
        ObjectMapper mapper = new ObjectMapper();
        JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(mapper);
        Class<?> baseClass = schemaClassLoader.findClass(baseClassName);
        Reflections reflections = new Reflections(packagesToScan);
        @SuppressWarnings("unchecked")
        Set<Class<?>> subTypes = reflections.getSubTypesOf((Class<Object>) baseClass);
        subTypes.forEach(typ -> {
          getLog().info(typ.getCanonicalName());
          try {
            JsonSchema schema = schemaGen.generateSchema(typ);
            Path tgt = Paths.get(outputDirectory,typ.getName() + "-schema.json");
            Files.write(tgt,mapper.writerWithDefaultPrettyPrinter()
                                  .writeValueAsString(schema)
                                  .getBytes(StandardCharsets.UTF_8));
            getLog().info("Written: " + tgt.toFile().getName());
          } catch(IOException x) {
            getLog().error(x.getMessage());
          }
        });
      } finally {
        Thread.currentThread().setContextClassLoader(ldr);
      }
    } catch(Exception x) {
      throw new MojoExecutionException(x.getMessage());
    }
  }
}
