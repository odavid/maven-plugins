package com.github.odavid.maven.plugins.dependencyhandler

import static org.twdata.maven.mojoexecutor.MojoExecutor.*
import groovy.text.SimpleTemplateEngine

import java.nio.file.Files
import java.nio.file.Path

import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.BuildPluginManager
import org.apache.maven.plugins.annotations.Component
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugins.annotations.ResolutionScope
import org.apache.maven.project.MavenProject
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter
import com.github.odavid.maven.plugins.groovy.GroovyMojoWithAntLogger

abstract class AbstractDependencyHandlerMojo extends GroovyMojoWithAntLogger{
	
	@Parameter(defaultValue='${project}', readonly=true)
	MavenProject mavenProject
	
	@Parameter(defaultValue='${session}', readonly=true)
	MavenSession mavenSession
	
	@Component
	BuildPluginManager pluginManager
	
	@Parameter(property='dependencyhandler.outputDirectory', defaultValue='${project.build.directory}')
	File outputDirectory
	
	@Parameter(property='dependencyhandler.includeTransitive', defaultValue='true')
	boolean includeTransitive

	@Parameter(property='dependencyhandler.stripVersion', defaultValue='false')
	boolean stripVersion

	@Parameter(property='dependencyhandler.stripClassifier', defaultValue='false')
	boolean stripClassifier

	@Parameter(property='dependencyhandler.includes')
	String includes;

	@Parameter(property='dependencyhandler.excludes')
	String excludes;
	
	@Parameter(property='dependencyhandler.overrideExisting', defaultValue='true')
	boolean overrideExisting;

	@Parameter(property='dependencyhandler.markersDirectory', defaultValue='${project.build.directory}/dependency-maven-plugin-markers')
	File markersDirectory;
	
	@Parameter(property='dependencyhandler.dependencyPluginVersion', defaultValue='2.8')
	String dependencyPluginVersion

	List<Artifact> filterDependencies(){
		AndArtifactFilter filter = new AndArtifactFilter()
		if(includes) {
			List<String> includeList = includes.split(',\\s*')
			log.info("Using include filter: ${includeList}" )
			PatternIncludesArtifactFilter includeFilter = new PatternIncludesArtifactFilter(includeList, false)
			filter.add(includeFilter)
		}
		if(excludes){
			List<String> excludeList = excludes.split(',\\s*')
			log.info("Using exclude filter: ${excludeList}" )
			PatternExcludesArtifactFilter excludeFilter = new PatternExcludesArtifactFilter(excludeList, false)
			filter.add(excludeFilter)
		}
		
		Set<Artifact> artifacts = includeTransitive? mavenProject.getArtifacts() : mavenProject.getDependencyArtifacts()
		def filtered = []
		artifacts.each {
			if(filter.include(it)){
				filtered += it
			}
		}
		filtered
	}
	
	void createSymlinks(List<Artifact> filtered){
		SimpleTemplateEngine templateEngine = new SimpleTemplateEngine()
		String symlinkPattern = '${artifactId}'
		if(!stripVersion){
			symlinkPattern+='-${version}'
		}
		if(!stripClassifier){
			symlinkPattern+= '${dashClassifier}'
		}
		symlinkPattern+='.${type}'
		log.info("Using symlinkPattern: ${symlinkPattern}")
		outputDirectory.mkdirs()
		filtered.each { Artifact it ->
			def artifactMap = [
				'artifactId': it.artifactId,
				'groupId': it.groupId,
				'version': it.version,
				'type': it.type,
				'classifier': it.classifier ?: "",
				'dashClassifier': it.classifier? "-${it.classifier}" : ""]
			def name = templateEngine.createTemplate(symlinkPattern).make(artifactMap)
			File f = new File(outputDirectory, name.toString())
			Path t = f.toPath()
			Path s = it.getFile().toPath()
			log.info("Configured Artifact: ${it} $s -> $t")
			if(overrideExisting && f.exists()){
				f.delete()
			}
			Files.createSymbolicLink(t, s)
		}
	}

	Element[] artifactItems(List<Artifact> filtered){
		def items = filtered.collect { a ->
			def coordinates = [
				element('groupId', a.groupId),
				element('artifactId', a.artifactId),
				element('type', a.type)
			]
			if (a.classifier){
				coordinates += element('classifier', a.classifier)
			}

			Element artifactItem = element('artifactItem', coordinates as Element[])
		} as Element[]
	}
	
	void dependencyMavenPluginCopy(List<Artifact> filtered){
		dependencyMavenPlugin(filtered, 'copy', {
				configuration(
					element(name('stripVersion'), "${stripVersion}"),
					element(name('stripClassifier'), "${stripClassifier}"),
					element(name('outputDirectory'), "${outputDirectory}"),
					element(name('artifactItems'), artifactItems(filtered))
				)
			}
		)
	}
	void dependencyMavenPluginUnpack(List<Artifact> filtered){
		dependencyMavenPlugin(filtered, 'unpack', {
				configuration(
					element(name('markersDirectory'), "${markersDirectory}"),
					element(name('outputDirectory'), "${outputDirectory}"),
					element(name('artifactItems'), artifactItems(filtered))
				)
			}
		)
	}
	
	private void dependencyMavenPlugin(List<Artifact> filtered, String goal, Closure configuration){
		executeMojo(
			plugin(
				groupId('org.apache.maven.plugins'),
				artifactId('maven-dependency-plugin'),
				version(dependencyPluginVersion)
			),
			goal,
			configuration(filtered),
			executionEnvironment(
				mavenProject,
				mavenSession,
				pluginManager
			)
		);
	}
}