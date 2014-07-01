package com.github.odavid.maven.plugins.unpackedrepo;

import java.io.File;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "build-path", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class UnpackedRepoPathBuilderMojo extends AbstractUnpackRepoMojo{
	@Parameter(defaultValue=" ")
	private String separator;

	@Parameter
	private String prefix;

	@Parameter
	private String suffix;
	
	@Parameter(defaultValue="unpacked-repo-artifacts-path")
	private String property;
	
	
	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		String newSuffix = suffix;
		if(newSuffix != null){
			newSuffix = newSuffix.replace('\\', File.separatorChar);
			newSuffix = newSuffix.replace('/', File.separatorChar);
		}
		List<Artifact> artifacts = filterArtifacts();
		if(artifacts.size() > 0){
			StringBuilder buffer = new StringBuilder();
			boolean start = true;
			for(Artifact artifact: artifacts){
				String path = getUnpackedFilePath(artifact).getAbsolutePath();
				if(prefix != null){
					path = prefix + path;
				}
				if(newSuffix != null){
					path = path + newSuffix;
				}
				if(!start){
					buffer.append(separator);
				}else{
					start = false;
				}
				buffer.append(path);
			}
			mavenProject.getProperties().put(property, buffer.toString());
			getLog().debug("Property:" + property + " = " + buffer.toString());
		}else{
			getLog().debug("No artifacts matched the filter, skipping execution");
		}
	}

}
