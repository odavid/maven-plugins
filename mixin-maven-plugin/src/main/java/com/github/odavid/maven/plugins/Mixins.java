package com.github.odavid.maven.plugins;

import java.util.ArrayList;
import java.util.List;

public class Mixins {
	private List<Mixin> mixins = new ArrayList<Mixin>();
	private boolean mergePlugins = true;
	private boolean mergePluginManagement = true;
	private boolean mergeProperties = true;
	private boolean recurse = true;

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
	
	public void setRecurse(boolean recurse){
		this.recurse = recurse;
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
	public boolean isRecurse() {
		return recurse;
	}
}
