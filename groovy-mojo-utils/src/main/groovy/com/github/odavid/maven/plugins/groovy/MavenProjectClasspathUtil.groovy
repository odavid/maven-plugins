package com.github.odavid.maven.plugins.groovy;

import org.apache.maven.artifact.Artifact
import org.apache.maven.project.MavenProject

public class MavenProjectClasspathUtil {

	static List<String> buildCombinedClasspathElements(MavenProject mavenProject, List<Artifact> pluginArtifacts){
		def compileClasspath = mavenProject.getCompileClasspathElements()
		pluginArtifacts.each { a ->
			if(a.type == 'jar'){
				compileClasspath += a.file.path
			}
		}
		compileClasspath
	}
	
	static void createMetaClasspathJar(File classpathMetaJar, List<String> cpElements, AntBuilder ant){
		File basedir = classpathMetaJar.parentFile
		def relativePaths = []
		cpElements.each{ cpElement ->
			File f = new File(cpElement)
			if(f.name.endsWith('.jar')){
				relativePaths += basedir.toPath().relativize(f.toPath()).toString().replace('\\', '/')
			}
		}
		ant.jar(jarfile: classpathMetaJar){
			manifest{
				attribute(name: 'Class-Path', value: relativePaths.join(File.pathSeparator))
			}
		}
	}
}
