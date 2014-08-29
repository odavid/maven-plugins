package com.github.odavid.maven.plugins;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component(role=ArtifactFetcher.class, hint="DefaultArtifactFetcher")
public class DefaultArtifactFetcher implements ArtifactFetcher{
	@Requirement
	private RepositorySystem repositorySystem;
	@Requirement
	private ResolutionErrorHandler resolutionErrorHandler;
	

	@Override
	public Artifact createArtifact(String groupId, String artifactId, String type, String classifier, String version) {
		return repositorySystem.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
	}

	@Override
	public void resolve(Artifact artifact, MavenSession session) throws ArtifactResolutionException{
		ArtifactResolutionRequest request = new ArtifactResolutionRequest();
		request.setArtifact(artifact);
		request.setResolveRoot( true ).setResolveTransitively( true ).setLocalRepository(
                    session.getLocalRepository() ).setOffline( session.isOffline() ).setForceUpdate(
                    session.getRequest().isUpdateSnapshots() );
        request.setServers( session.getRequest().getServers() );
        request.setMirrors( session.getRequest().getMirrors() );
        request.setProxies( session.getRequest().getProxies() );
        request.setRemoteRepositories(session.getCurrentProject().getRemoteArtifactRepositories());
        request.setLocalRepository(session.getLocalRepository());
	    ArtifactResolutionResult result = repositorySystem.resolve(request);
        resolutionErrorHandler.throwErrors(request, result);
	}

}
