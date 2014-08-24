package com.github.odavid.maven.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

public class Mixin {
	private String groupId;
	private String artifactId;
	private String version;

	private Boolean mergePluginManagement;
	private Boolean mergePlugins;
	private Boolean mergeProperties;
	private Mixins mixins;

	private Artifact artifact;

	public void setMixins(Mixins mixins){
		this.mixins = mixins;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public void setMergePluginManagement(Boolean mergePluginManagement) {
		this.mergePluginManagement = mergePluginManagement;
	}
	public void setMergePlugins(Boolean mergePlugins) {
		this.mergePlugins = mergePlugins;
	}
	public void setMergeProperties(Boolean mergeProperties) {
		this.mergeProperties = mergeProperties;
	}
	public String getGroupId() {
		return groupId;
	}
	public String getArtifactId() {
		return artifactId;
	}
	public String getVersion() {
		return version;
	}
	public boolean isMergePluginManagement() {
		return mergePluginManagement != null ? mergePluginManagement : mixins.isMergePluginManagement();
	}
	public boolean isMergePlugins() {
		return mergePlugins != null ? mergePlugins : mixins.isMergePlugins();
	}
	public boolean isMergeProperties() {
		return mergeProperties != null ? mergeProperties : mixins.isMergeProperties();
	}

	public void merge(MavenProject mavenProject, MavenSession mavenSession, Plugin plugin, MixinModelMerger mixinModelMerger, MavenXpp3Reader xpp3Reader, RepositorySystem repositorySystem) throws MavenExecutionException {
		Artifact artifact = getArtifact(mavenProject, plugin);
		File artifactFile;
		try {
			artifactFile = resolveArtifact(mavenProject, artifact, mavenSession.getRepositorySession(), repositorySystem);
		} catch (MojoExecutionException e) {
			throw new MavenExecutionException (String.format("Cannot resolve mixin artifact %s", artifact), e);
		}
		Model mixinModel;
		try {
			mixinModel = xpp3Reader.read(new FileInputStream(artifactFile));
			if(isMergeProperties()){
				mixinModelMerger.mergeProperties(mavenProject.getModel(), mixinModel);
			}
			if(isMergePluginManagement()){
				mixinModelMerger.mergePluginManagement(mavenProject.getModel(), mixinModel);
			}
			if(isMergePlugins()){
				mixinModelMerger.mergePlugins(mavenProject.getModel(), mixinModel);
			}
			
		} catch (FileNotFoundException e) {
			throw new MavenExecutionException (String.format("Cannot find mixin file %s for mixin artifact %s ", artifactFile, artifact) , e);
		} catch (IOException e) {
			throw new MavenExecutionException (String.format("Cannot read mixin file %s for mixin artifact %s ", artifactFile, artifact)  , e);
		} catch (XmlPullParserException e) {
			throw new MavenExecutionException (String.format("Cannot parse mixin file %s for mixin artifact %s ", artifactFile, artifact)  , e);
		}
	}

	private File resolveArtifact(MavenProject currentProject, Artifact artifact, RepositorySystemSession repositorySystemSession, RepositorySystem repositorySystem) throws MojoExecutionException {
		try {
			ArtifactRequest request = new ArtifactRequest();
			request.setArtifact(artifact);
			request.setRepositories(currentProject.getRemoteProjectRepositories());
			
			ArtifactResult result = repositorySystem.resolveArtifact(repositorySystemSession, request);
			return result.getArtifact().getFile();
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(String.format( "Error resolving artifact %s", artifact));
		}
	}
	private Artifact getArtifact(MavenProject currentProject, Plugin plugin) throws MavenExecutionException {
		if(this.artifact == null){
			String depConflictId = groupId + ":" + artifactId + ":pom";
			if (StringUtils.isEmpty(version)) {
				version = null;
				for (org.apache.maven.artifact.Artifact artifact : currentProject.getArtifacts()) {
					if (artifact.getDependencyConflictId().equals(depConflictId)) {
						version = artifact.getVersion();
						break;
					}
				}
				if (version == null) {
					for (Dependency dep : plugin.getDependencies()) {
						if (dep.getGroupId().equals(groupId) && dep.getArtifactId().equals(artifactId) && dep.getType().equals("pom")) {
							version = dep.getVersion();
							break;
						}
					}
				}
				if (version == null) {
					throw new MavenExecutionException( "Cannot find version for " + depConflictId, currentProject.getFile());
				}
			}
			artifact = new DefaultArtifact(groupId, artifactId, "pom", version);
		}
		return artifact;
	}
}
