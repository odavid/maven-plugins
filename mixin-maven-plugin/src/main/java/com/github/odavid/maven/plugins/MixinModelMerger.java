package com.github.odavid.maven.plugins;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Build;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.model.merge.MavenModelMerger;
import org.codehaus.plexus.component.annotations.Component;

/**
 * MixinModelMerger 
 * Enables pluginManagement and properties import from different pom files. Uses protected methods of MavenModelMerger, and therefore inherits from it
 */
@Component(role=MixinModelMerger.class)
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
	
	public void mergeReporting(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if(source.getReporting() != null){
			if(target.getReporting() == null){
				target.setReporting(new Reporting());
			}
			Reporting sourceContainer = source.getReporting();
			Reporting targetContainer = target.getReporting();
			if(targetContainer.getOutputDirectory() == null){
				targetContainer.setOutputDirectory(sourceContainer.getOutputDirectory());
			}
			if(targetContainer.getExcludeDefaults() == null){
				targetContainer.setExcludeDefaults(sourceContainer.getExcludeDefaults());
			}
			for(ReportPlugin sourcePlugin: sourceContainer.getPlugins()){
				String key = sourcePlugin.getKey();
				ReportPlugin targetPlugin = null;
				for(ReportPlugin targetPluginElement: targetContainer.getPlugins()){
					if(targetPluginElement.getKey() != null && targetPluginElement.getKey().equals(key)){
						targetPlugin = targetPluginElement;
						break;
					}
				}
				if(targetPlugin == null){
					targetContainer.getPlugins().add(sourcePlugin.clone());
				}else{
					for(ReportSet sourceReportSet : sourcePlugin.getReportSets()){
						String executionId = sourceReportSet.getId();
						ReportSet targetReportSet = null;
						for(ReportSet targetReportSetIt: targetPlugin.getReportSets()){
							if(targetReportSetIt.getId() != null && targetReportSetIt.getId().equals(executionId)){
								targetReportSet = targetReportSetIt;
								break;
							}
						}
						if(targetReportSet == null){
							targetPlugin.addReportSet(sourceReportSet.clone());
						}else{
							super.mergeReportSet(targetReportSet, sourceReportSet, false, context);
						}
					}
					super.mergeConfigurationContainer( targetPlugin, sourcePlugin, false, context);
					super.mergeReportPlugin_GroupId( targetPlugin, sourcePlugin, false, context);
					super.mergeReportPlugin_ArtifactId( targetPlugin, sourcePlugin, false, context);
					super.mergeReportPlugin_Version( targetPlugin, sourcePlugin, false, context);
				}
			}
		}
	}
	
	public void applyPluginManagementOnPlugins(Model model){
		Map<Object, Object> context = new HashMap<Object, Object>();
		mergePluginContainers(model.getBuild(), model.getBuild().getPluginManagement(), context, false);
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
		}
	}
	
	public void mergeProperties(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if(source.getProperties() != null){
			super.mergeModelBase_Properties(target, source, false, context);
		}
	}
	
	public void mergeDistributionManagement(Model target, Model source){
		Map<Object, Object> context = new HashMap<Object, Object>();
		if (source.getDistributionManagement() != null){
			if(target.getDistributionManagement() == null){
				target.setDistributionManagement(new DistributionManagement());
			}
			super.mergeDistributionManagement(target.getDistributionManagement(), source.getDistributionManagement(), false, context);
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
			String key = sourcePlugin.getKey();
			Plugin targetPlugin = null;
			for(Plugin targetPluginElement: targetContainer.getPlugins()){
				if(targetPluginElement.getKey() != null && targetPluginElement.getKey().equals(key)){
					targetPlugin = targetPluginElement;
					break;
				}
			}
			if(targetPlugin == null){
				if(addTargetPlugin){
					targetContainer.getPlugins().add(sourcePlugin.clone());
				}
			}else{
				for(PluginExecution sourceExecution : sourcePlugin.getExecutions()){
					String executionId = sourceExecution.getId();
					PluginExecution targetPluginExecution = null;
					for(PluginExecution targetExecution: targetPlugin.getExecutions()){
						if(targetExecution.getId() != null && targetExecution.getId().equals(executionId)){
							targetPluginExecution = targetExecution;
							break;
						}
					}
					if(targetPluginExecution == null){
						targetPlugin.addExecution(sourceExecution.clone());
					}else{
						super.mergePluginExecution(targetPluginExecution, sourceExecution, false, context);
					}
				}
				super.mergeConfigurationContainer( targetPlugin, sourcePlugin, false, context);
				super.mergePlugin_GroupId( targetPlugin, sourcePlugin, false, context);
				super.mergePlugin_ArtifactId( targetPlugin, sourcePlugin, false, context);
				super.mergePlugin_Version( targetPlugin, sourcePlugin, false, context);
				super.mergePlugin_Extensions( targetPlugin, sourcePlugin, false, context );
				super.mergePlugin_Dependencies( targetPlugin, sourcePlugin, false, context);
			}
		}
	}
}
