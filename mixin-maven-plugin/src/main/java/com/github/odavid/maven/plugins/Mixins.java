package com.github.odavid.maven.plugins;

import java.util.ArrayList;
import java.util.List;

public class Mixins {
	private List<Mixin> mixins = new ArrayList<Mixin>();
	private boolean mergePlugins = true;
	private boolean mergePluginManagement = true;
	private boolean mergeProperties = true;
	private boolean mergeReporting = true;
	private boolean mergeDistributionManagement = true;
	private boolean recurse = true;
	private boolean activateProfiles = true;
	private String defaultMixinType = "pom";

	public String getDefaultMixinType() {
		return defaultMixinType;
	}
	public void setDefaultMixinType(String defaultMixinType) {
		this.defaultMixinType = defaultMixinType;
	}
	public void addMixin(Mixin mixin){
		mixins.add(mixin);
		mixin.setMixins(this);
	}
	public List<Mixin> getMixins(){
		return mixins;
	}
	
	public void setMergePlugins(boolean mergePlugins) {
		this.mergePlugins = mergePlugins;
	}

	public void setMergePluginManagement(boolean mergePluginManagement) {
		this.mergePluginManagement = mergePluginManagement;
	}

	public void setMergeProperties(boolean mergeProperties) {
		this.mergeProperties = mergeProperties;
	}

	public void setMergeDistributionManagement(boolean mergeDistributionManagement) {
		this.mergeDistributionManagement = mergeDistributionManagement;
	}
	
	public void setRecurse(boolean recurse){
		this.recurse = recurse;
	}

	public void setActivateProfiles(boolean activateProfiles){
		this.activateProfiles = activateProfiles;
	}
	
	public boolean isMergePlugins() {
		return mergePlugins;
	}

	public boolean isMergePluginManagement() {
		return mergePluginManagement;
	}

	public boolean isMergeProperties() {
		return mergeProperties;
	}

	public boolean isMergeDistributionManagement() {
		return mergeDistributionManagement;
	}
	public boolean isRecurse() {
		return recurse;
	}
	public boolean isActivateProfiles() {
		return activateProfiles;
	}
	public boolean isMergeReporting() {
		return mergeReporting;
	}
	public void setMergeReporting(boolean mergeReporting) {
		this.mergeReporting = mergeReporting;
	}
	
	
}
