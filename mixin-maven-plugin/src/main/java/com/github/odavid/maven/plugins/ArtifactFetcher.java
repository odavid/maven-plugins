package com.github.odavid.maven.plugins;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;

public interface ArtifactFetcher {
	public Artifact createArtifact(String groupId, String artifactId, String type, String classifier, String version);
	
	public void resolve(Artifact artifact, MavenSession mavenSession) throws ArtifactResolutionException;
}
