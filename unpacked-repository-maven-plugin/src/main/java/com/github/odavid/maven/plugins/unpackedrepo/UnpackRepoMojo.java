package com.github.odavid.maven.plugins.unpackedrepo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

@Mojo(name = "unpack", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class UnpackRepoMojo extends AbstractUnpackRepoMojo{
	
	@Override
	public void execute() throws MojoExecutionException {
		File localUnpackedRepo  = new File(localRepository.getBasedir(), ".unpacked");
		File markersDir = new File(localUnpackedRepo, ".markers");
		List<Artifact> artifacts = filterArtifacts();
		if(artifacts.size() > 0){
			List<Element> artifactItems = new ArrayList<>();
			for(Artifact artifact: artifacts){
				artifactItems.add(buildArtifactItem(artifact));
			}
			Plugin depPlugin = MojoExecutor.plugin(
			        groupId("org.apache.maven.plugins"),
			        artifactId("maven-dependency-plugin"),
			        version("2.8")
			);
			ExecutionEnvironment env = MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager);
			MojoExecutor.executeMojo(depPlugin, "unpack", 
					MojoExecutor.configuration(
							element("markersDirectory", markersDir.getAbsolutePath()),
							element("artifactItems", 
									artifactItems.toArray(new Element[artifactItems.size()])))
									, env);
		}else{
			getLog().info("No artifacts matched the filter, skipping execution");
		}
	}

	private Element buildArtifactItem(Artifact dependency) {
		return element("artifactItem", 
				element("groupId", dependency.getGroupId()), 
				element("artifactId", dependency.getArtifactId()),
				element("artifactId", dependency.getArtifactId()),
				element("version", dependency.getVersion()),
				element("type", dependency.getType() != null ? dependency.getType() : ""),
				element("classifier", dependency.getClassifier() != null ? dependency.getClassifier() : null),
				element("outputDirectory", getUnpackedFilePath(dependency).getAbsolutePath()));
	}

}
