package org.apache.maven.ant.tasks;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import org.apache.tools.ant.BuildException;

public class UnpackDependenciesAntTask extends AbstractMavenAntTask{
	private String includeGroupIds;
	private String includeArtifactIds;
	private String outputDirectory;
	private String includeTypes;
	private String includeScope;
	private String includes;
	private String excludes;
	public void setIncludes(String includes) {
		this.includes = includes;
	}
	public void setExcludes(String excludes) {
		this.excludes = excludes;
	}
	private boolean failOnMissingClassifierArtifact;
	
	public void setIncludeGroupIds(String includeGroupIds) {
		this.includeGroupIds = includeGroupIds;
	}
	public void setIncludeArtifactIds(String includeArtifactIds) {
		this.includeArtifactIds = includeArtifactIds;
	}
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	public void setIncludeTypes(String includeTypes) {
		this.includeTypes = includeTypes;
	}
	public void setIncludeScope(String includeScope) {
		this.includeScope = includeScope;
	}
	public void setFailOnMissingClassifierArtifact(
			boolean failOnMissingClassifierArtifact) {
		this.failOnMissingClassifierArtifact = failOnMissingClassifierArtifact;
	}
	@Override
	public void execute() throws BuildException {
		try {
			executeMojo(
				    plugin(
				        groupId("org.apache.maven.plugins"),
				        artifactId("maven-dependency-plugin"),
				        version("2.8")
				    ),
				    goal("unpack-dependencies"),
				    configuration(
					        element(name("outputDirectory"), outputDirectory),
					        element(name("includeArtifactIds"), includeArtifactIds),
					        element(name("includeGroupIds"), includeGroupIds),
					        element(name("includeTypes"), includeTypes),
					        element(name("includeScope"), includeScope),
					        element(name("includes"), includes),
					        element(name("excludes"), excludes),
					        element(name("failOnMissingClassifierArtifact"), String.valueOf(failOnMissingClassifierArtifact))
				    ),
				    executionEnvironment(
				        getMavenProject(),
				        getMavenSession(),
				        getPluginManager()
				    )
			);
			
		} catch (Throwable e) {
			throw new BuildException("Cannot unpack-dependencies: " + e.getMessage(), e);
		}
	}
}
