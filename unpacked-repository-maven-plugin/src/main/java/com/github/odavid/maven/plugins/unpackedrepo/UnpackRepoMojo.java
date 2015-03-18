package com.github.odavid.maven.plugins.unpackedrepo;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import org.twdata.maven.mojoexecutor.MojoExecutor.Element;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

@Mojo(name = "unpack", defaultPhase = LifecyclePhase.PROCESS_SOURCES, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class UnpackRepoMojo extends AbstractUnpackRepoMojo{
	@Parameter(defaultValue="true", property="unpack.repo.globalUpackSync")
	private boolean globalUpackSync;
	
	@Parameter(defaultValue="2.10", property="unpack.repo.dependencyPluginVersion")
	private String dependencyPluginVersion;

	@Override
	public void execute() throws MojoExecutionException {
		List<Artifact> artifacts = filterArtifacts();
		if(artifacts.size() > 0){
			List<Element> artifactItems = new ArrayList<>();
			for(Artifact artifact: artifacts){
				artifactItems.add(buildArtifactItem(artifact));
			}
			Plugin depPlugin = MojoExecutor.plugin(
			        groupId("org.apache.maven.plugins"),
			        artifactId("maven-dependency-plugin"),
			        version(dependencyPluginVersion)
			);
			if(globalUpackSync){
				//Synchronize all modules on the same class, 
				//so unpacking to a global location won't happen at the same time
				//I chose the MavenSession.class since it is loaded in the parent class loader
				getLog().info(mavenProject.getArtifactId() + ": globalUpackSync = true, globally Synchronizing on MavenSession class id: " + System.identityHashCode(MavenSession.class));
				synchronized (MavenSession.class) {
					unpackUsingDependencyPlugin(depPlugin, artifactItems);
				}
			}else{
				getLog().info("globalUpackSync = false");
				unpackUsingDependencyPlugin(depPlugin, artifactItems);
			}
			
            if(createSymlinks){
                symlinkRootDir.mkdirs();
                for(Artifact artifact: artifacts){
                    File targetFile = getUnpackedFilePath(artifact);
                    File linkFile = getSymlinkDir(artifact);
                    Path target = Paths.get(targetFile.getAbsolutePath());
                    Path link = Paths.get(linkFile.getAbsolutePath());
                    try {
                        if(linkFile.exists() && Files.isSymbolicLink(link)) {
                            getLog().info("Link: " + linkFile + " exists, rebuilding...");
                            Files.delete(link);
                        }
                        Files.createSymbolicLink(link, target);
                    } catch (IOException e) {
                        throw new MojoExecutionException("Could not create symlink " + getSymlinkDir(artifact).getAbsolutePath()+ " for " + getUnpackedFilePath(artifact) + " " + e.getMessage(), e);
                    }
                }
            }
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

	private void unpackUsingDependencyPlugin(Plugin depPlugin, List<Element> artifactItems) throws MojoExecutionException{
		ExecutionEnvironment env = MojoExecutor.executionEnvironment(mavenProject, mavenSession, pluginManager);
		MojoExecutor.executeMojo(depPlugin, "unpack", 
				MojoExecutor.configuration(
						element("markersDirectory", localRepoMarkersDir().getAbsolutePath()),
						element("artifactItems", 
								artifactItems.toArray(new Element[artifactItems.size()])))
								, env);
		
	}
	
}
