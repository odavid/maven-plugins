package com.github.odavid.maven.plugins;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;

public class Mixin {
	private String groupId;
	private String artifactId;
	private String version;

	private Boolean mergePluginManagement;
	private Boolean mergePlugins;
	private Boolean mergeProperties;
	private Mixins mixins;

	private String key;

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
	
	public String getKey(){
		if(key == null){
			key = groupId + ":" + artifactId + ":pom";
		}
		return key;
	}

	public void merge(MixinModelCache cache, MavenProject mavenProject, MavenSession mavenSession, Plugin plugin, MixinModelMerger mixinModelMerger, MavenXpp3Reader xpp3Reader, RepositorySystem repositorySystem) throws MavenExecutionException {
		Model mixinModel = cache.getModel(this, mavenProject, mavenSession, plugin, xpp3Reader, repositorySystem);
		if(isMergeProperties()){
			mixinModelMerger.mergeProperties(mavenProject.getModel(), mixinModel);
		}
		if(isMergePluginManagement()){
			mixinModelMerger.mergePluginManagement(mavenProject.getModel(), mixinModel);
		}
		if(isMergePlugins()){
			mixinModelMerger.mergePlugins(mavenProject.getModel(), mixinModel);
		}
	}

}
