package com.github.odavid.maven.plugins.unpackedrepo;

import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

@Mojo(name = "export-properties", defaultPhase = LifecyclePhase.INITIALIZE, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class UnpackRepoPropertiesExportMojo extends AbstractUnpackRepoMojo{
	@Parameter(defaultValue="unpack.repo.")
	private String prefix;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		List<Artifact> artifacts = this.filterArtifacts();
		mavenProject.getProperties().setProperty(prefix + "basedir", localRepoBaseDir().getAbsolutePath());
		mavenProject.getProperties().setProperty(prefix + "markersdir", localRepoMarkersDir().getAbsolutePath());
		for(Artifact artifact: artifacts){
			mavenProject.getProperties().setProperty(prefix + artifact.getDependencyConflictId(), this.getUnpackedFilePath(artifact).getAbsolutePath());
		}
	}

}
