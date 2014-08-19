package com.github.odavid.maven.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.merge.MavenModelMerger;

/**
 * MixinModelMerger 
 * Enables pluginManagement and properties import from different pom files. Uses protected methods of MavenModelMerger, and therefore inherits from it
 */
public class MixinModelMerger extends MavenModelMerger {
	
	public void mergePluginManagement(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if(source.getBuild()!= null && source.getBuild().getPluginManagement() != null){
			if(target.getBuild() == null){
				target.setBuild(new Build());
			}
			if(target.getBuild().getPluginManagement() == null){
				target.getBuild().setPluginManagement(new PluginManagement());
			}
			PluginContainer sourceContainer = source.getBuild().getPluginManagement();
			PluginContainer targetContainer = target.getBuild().getPluginManagement();
			mergePluginContainers(targetContainer, sourceContainer, context, true);
		}
	}

	public void mergePlugins(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if(source.getBuild()!= null){
			if(target.getBuild() == null){
				target.setBuild(new Build());
			}
			PluginContainer sourceContainer = source.getBuild();
			PluginContainer targetContainer = target.getBuild();
			mergePluginContainers(targetContainer, sourceContainer, context, true);
			mergePluginContainers(targetContainer, target.getBuild().getPluginManagement(), context, false);
		}
	}
	
	public void mergeProperties(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if(source.getProperties() != null){
			super.mergeModelBase_Properties(target, source, false, context);
		}
	}

	/**
	 * Fully merges pluginContainers with their plugins, their executions and their configuration 
	 * @param targetPlugin
	 * @param sourcePlugin
	 * @param context
	 */
	private void mergePluginContainers(PluginContainer targetContainer, PluginContainer sourceContainer, Map<Object, Object> context, boolean addTargetPlugin){
		List<Plugin> plugins = sourceContainer.getPlugins();
		for (Plugin sourcePlugin : plugins) {
			Plugin targetPlugin = targetContainer.getPluginsAsMap().get(sourcePlugin.getKey());
			if(targetPlugin == null){
				if(addTargetPlugin){
					targetContainer.getPlugins().add(sourcePlugin);
				}
			}else{
				for(PluginExecution sourceExecution : sourcePlugin.getExecutions()){
					PluginExecution targetPluginExecution = targetPlugin.getExecutionsAsMap().get(sourceExecution.getId());
					if(targetPluginExecution == null){
						targetPlugin.addExecution(sourceExecution);
					}else{
						super.mergePluginExecution(targetPluginExecution, sourceExecution, false, context);
					}
				}
				if(targetPlugin.getConfiguration() == null){
					targetPlugin.setConfiguration(sourcePlugin.getConfiguration());
				}else{
					super.mergePlugin(targetPlugin, sourcePlugin, false, context);
				}
			}
		}
	}
}
