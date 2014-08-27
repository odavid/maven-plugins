package com.github.odavid.maven.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.configuration.BeanConfigurationException;
import org.apache.maven.configuration.BeanConfigurationRequest;
import org.apache.maven.configuration.BeanConfigurator;
import org.apache.maven.configuration.DefaultBeanConfigurationRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.plugin.PluginConfigurationExpander;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.RepositorySystem;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "MixinMavenLifecycleParticipant")
public class MixinMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	private static final String PLUGIN_GROUPID = "com.github.odavid.maven.plugins";
	private static final String PLUGIN_ARTIFACTID = "mixin-maven-plugin";
	
	private final MavenXpp3Reader mavenXpp3reader = new MavenXpp3Reader();
	private final MixinModelMerger mixinModelMerger = new MixinModelMerger();

	@Requirement
	protected Logger logger;

	@Requirement
	protected RepositorySystem repositorySystem;
	
	@Requirement
	private BeanConfigurator beanConfigurator;
	
    @Requirement
    private ModelInterpolator modelInterpolator;
	
    @Requirement
    private PluginConfigurationExpander pluginConfigurationExpander;
    
    private MixinModelCache mixinModelCache = new MixinModelCache();
    
    private DefaultModelBuildingRequest modelBuildingRequest;
    
	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
	}

	@Override
	public void afterProjectsRead(MavenSession mavenSession) throws MavenExecutionException {
		logger.info(String.format("%s: Merging Mixins", PLUGIN_ARTIFACTID));
		
		ProjectBuildingRequest projectBuildingRequest = mavenSession.getProjectBuildingRequest();
		modelBuildingRequest = new DefaultModelBuildingRequest();
		modelBuildingRequest.setActiveProfileIds(projectBuildingRequest.getActiveProfileIds());
		modelBuildingRequest.setInactiveProfileIds(projectBuildingRequest.getInactiveProfileIds());
		modelBuildingRequest.setBuildStartTime(projectBuildingRequest.getBuildStartTime());
		
		List<MavenProject> projects = mavenSession.getProjects();
		List<Mixin> mixinList = new ArrayList<>();
		Map<String,Mixin> mixinMap = new HashMap<String, Mixin>();
		for (MavenProject module : projects ) {
			mixinList.clear();
			mixinMap.clear();
			mergeMixins(mixinList, mixinMap, module, mavenSession);
		}
		mavenSession.setProjects(projects);
		logger.info(String.format("%s: Mixins were merged", PLUGIN_ARTIFACTID));
	}

	private void fillMixins(List<Mixin> mixinList, Map<String,Mixin> mixinMap, Model model, MavenProject currentProject, MavenSession mavenSession) throws MavenExecutionException {
		model = model.clone();
		MixinModelProblemCollector problems = new MixinModelProblemCollector();
		modelInterpolator.interpolateModel(model, currentProject.getBasedir(), modelBuildingRequest, problems);
		List<Plugin> plugins = model.getBuild().getPlugins();
		for (Plugin plugin : plugins) {
			if (plugin.getGroupId().equals(PLUGIN_GROUPID) && plugin.getArtifactId().equals(PLUGIN_ARTIFACTID)) {
				Mixins mixins = loadConfiguration(plugin.getConfiguration());
				//First start with the base level and then add the inherited mixins
				for(Mixin mixin: mixins.getMixins()){
					if(!mixinMap.containsKey(mixin.getKey())){
						logger.debug(String.format("Adding mixin: %s to cache", mixin.getKey()));

						mixinModelCache.getModel(mixin, currentProject, mavenSession, mavenXpp3reader, repositorySystem);
						mixinMap.put(mixin.getKey(), mixin);
						mixinList.add(mixin);
					}
				}
				for(Mixin mixin: mixins.getMixins()){
					if(mixin.isRecurse()){
						Model mixinModel = mixinModelCache.getModel(mixin, currentProject, mavenSession, mavenXpp3reader, repositorySystem);
						fillMixins(mixinList, mixinMap, mixinModel, currentProject, mavenSession);
					}
				}
			}
		}
	}

	private void mergeMixins(List<Mixin> mixinList, Map<String,Mixin> mixinMap, MavenProject currentProject, MavenSession mavenSession) throws MavenExecutionException {
		fillMixins(mixinList, mixinMap, currentProject.getModel(), currentProject, mavenSession);
		for(Mixin mixin: mixinList){
			logger.debug(String.format("Merging mixin: %s into %s", mixin.getKey(), currentProject.getFile()));
			Model mixinModel = mixinModelCache.getModel(mixin, currentProject, mavenSession, mavenXpp3reader, repositorySystem);
			mixin.merge(mixinModel, currentProject, mavenSession, mixinModelMerger, mavenXpp3reader, repositorySystem);
		}
		if(mixinList.size() > 0){
			//Apply the pluginManagement section on the plugins section
			mixinModelMerger.applyPluginManagementOnPlugins(currentProject.getModel());

			MixinModelProblemCollector problems = new MixinModelProblemCollector();
			modelInterpolator.interpolateModel(currentProject.getModel(), currentProject.getBasedir(), modelBuildingRequest, problems);
			pluginConfigurationExpander.expandPluginConfiguration(currentProject.getModel(), modelBuildingRequest, problems);
			problems.checkErrors(currentProject.getFile());
		}
	}

	private Mixins loadConfiguration(Object configuration) throws MavenExecutionException {
		Mixins mixins = new Mixins();
		BeanConfigurationRequest request = new DefaultBeanConfigurationRequest();
		request.setBean(mixins);
		request.setConfiguration(configuration, "mixins");
		try {
			beanConfigurator.configureBean(request);
			return mixins;
		} catch (BeanConfigurationException e) {
			throw new MavenExecutionException("Cannot load mixins configuration: " + e.getMessage(), e); 
		}
	}
}