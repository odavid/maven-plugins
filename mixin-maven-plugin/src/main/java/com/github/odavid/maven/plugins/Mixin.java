package com.github.odavid.maven.plugins;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

public class Mixin {
	private String groupId;
	private String artifactId;
	private String version;
	private String type;

	private Boolean mergePluginManagement;
	private Boolean mergePlugins;
	private Boolean mergeReporting;
	private Boolean mergeProperties;
	private Boolean mergeDistributionManagement;
	private Boolean recurse;
	private Boolean activateProfiles;
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

	public void setRecurse(Boolean recurse){
		this.recurse = recurse;
	}
	
	public void setActivateProfiles(Boolean activateProfiles) {
		this.activateProfiles = activateProfiles;
	}
	public void setMergePluginManagement(Boolean mergePluginManagement) {
		this.mergePluginManagement = mergePluginManagement;
	}
	public void setMergePlugins(Boolean mergePlugins) {
		this.mergePlugins = mergePlugins;
	}
	public void setMergeReporting(Boolean mergeReporting) {
		this.mergeReporting = mergeReporting;
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
	public boolean isMergeReporting() {
		return mergeReporting != null ? mergeReporting : mixins.isMergeReporting();
	}
	public boolean isMergeProperties() {
		return mergeProperties != null ? mergeProperties : mixins.isMergeProperties();
	}
	public boolean isMergeDistributionManagement() {
		return mergeDistributionManagement != null ? mergeDistributionManagement : mixins.isMergeDistributionManagement();
	}
	public boolean isRecurse(){
		return recurse != null ? recurse : mixins.isRecurse();
	}
	
	public Boolean isActivateProfiles() {
		return activateProfiles != null ? activateProfiles : mixins.isActivateProfiles();
	}
	public String getType(){
		if(type == null){
			type = mixins.getDefaultMixinType(); 
		}
		return type;
	}

	public String getKey(){
		if(key == null){
			key = groupId + ":" + artifactId + ":" + getType();
		}
		return key;
	}

	public void merge(Model mixinModel, MavenProject mavenProject, MavenSession mavenSession, MixinModelMerger mixinModelMerger) throws MavenExecutionException {
		if(isMergeProperties()){
			mixinModelMerger.mergeProperties(mavenProject.getModel(), mixinModel);
		}
		if(isMergePluginManagement()){
			mixinModelMerger.mergePluginManagement(mavenProject.getModel(), mixinModel);
		}
		if(isMergePlugins()){
			mixinModelMerger.mergePlugins(mavenProject.getModel(), mixinModel);
		}
		if(isMergeReporting()){
			mixinModelMerger.mergeReporting(mavenProject.getModel(), mixinModel);
		}
		if(isMergeDistributionManagement()){
			mixinModelMerger.mergeDistributionManagement(mavenProject.getModel(), mixinModel);
		}
	}
	
	@Override
	public String toString() {
		return getKey();
	}

}
