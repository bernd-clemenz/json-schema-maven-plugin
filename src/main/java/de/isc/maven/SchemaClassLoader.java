/*
 * (c) 2019 ISC Clemenz & Weinbrecht GmbH
 * Licensed under Apache 2.0 (http://www.apache.org/licenses/LICENSE-2.0)
 */
package de.isc.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * SchemaClassLoader adding compile project class-path to this plugins
 * class-path.
 *
 * @author Bernd Clemenz
 * @version 1.0.0
 * @since 1.0.0
 */
public class SchemaClassLoader extends URLClassLoader {
  private final Log m_log;
  private final Map<String,Class<?>> m_classMap = Collections.synchronizedMap(new HashMap<>());

  /**
   * Constructor.
   *
   * @param mavenProject the Maven project
   * @param mavenLog the maven logger
   * @param parent the parent class-loader
   * @throws DependencyResolutionRequiredException forwarded from API
   */
  SchemaClassLoader(final MavenProject mavenProject,
                    final Log mavenLog,
                    final ClassLoader parent)
  throws DependencyResolutionRequiredException {
    super(new URL[] {},parent);
    m_log = mavenLog;

    mavenProject.getCompileClasspathElements().forEach(s -> {
      try {
        addURL(new File(s).toURI().toURL());
        m_log.debug("Element: " + s);
      } catch(MalformedURLException x) {
        m_log.error("Ignored:" + x.getMessage());
      }
    });
  }

  /**
   *
   * @param name class name to search for
   * @return the class
   * @throws ClassNotFoundException if not found.
   */
  @Override
  protected Class<?> findClass(final String name)
  throws ClassNotFoundException {
    m_log.debug("findClass: " + name);

    Class<?> foundClzz = m_classMap.get(name);
    m_log.debug("Got: " + foundClzz);
    if(null != foundClzz) {
      m_log.debug("Class already there: " + name);
      return foundClzz;
    }
    Class<?> clzz = super.findClass(name);
    m_classMap.put(name,clzz);
    return clzz;
  }
}
