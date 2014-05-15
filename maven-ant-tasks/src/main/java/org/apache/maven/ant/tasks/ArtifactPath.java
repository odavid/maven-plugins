package org.apache.maven.ant.tasks;

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.util.StringUtils;

public class ArtifactPath extends AbstractMavenAntTask{
	private String groupId;
	private String artifactId;
	private String type = "jar";
	private String classifier;
	
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	@Override
	public void execute() throws BuildException {
		if(StringUtils.isEmpty(groupId)){
			throw new BuildException("groupId is required");
		}
		if(StringUtils.isEmpty(artifactId)){
			throw new BuildException("artifactId is required");
		}
		
		Set<Artifact> artifacts = getMavenProject().getArtifacts();
		Artifact a = null;
		for(Artifact artifact: artifacts){
			if(artifact.getGroupId().equals(groupId) && artifact.getArtifactId().equals(artifactId)){
				a = artifact;
				break;
			}
		}
		if(a != null){
			DefaultArtifact newArtifact = new DefaultArtifact(groupId, artifactId, a.getVersionRange(), a.getScope(), type, classifier, a.getArtifactHandler());
            try {
				ArtifactResolver artifactResolver = getArtifactResolver();
				artifactResolver.resolve(newArtifact, getMavenProject().getRemoteArtifactRepositories(), getLocalRepository());
			} catch (Exception e) {
				throw new BuildException("Cannot resolve artifact: " + newArtifact.getDependencyConflictId());
			}
		}
	}
}
