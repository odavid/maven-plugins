package com.github.odavid.maven.plugins;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.configuration.BeanConfigurationException;
import org.apache.maven.configuration.BeanConfigurationRequest;
import org.apache.maven.configuration.BeanConfigurator;
import org.apache.maven.configuration.DefaultBeanConfigurationRequest;
import org.apache.maven.execution.MavenSession;
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
    
	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
	}

	@Override
	public void afterProjectsRead(MavenSession mavenSession) throws MavenExecutionException {
		logger.info(String.format("%s: Merging Mixins", PLUGIN_ARTIFACTID));
		List<MavenProject> projects = mavenSession.getProjects();
		for (MavenProject module : projects ) {
			mergeMixins(module, mavenSession);
		}
		mavenSession.setProjects(projects);
		logger.info(String.format("%s: Mixins were merged", PLUGIN_ARTIFACTID));
	}

	private void mergeMixins(MavenProject currentProject, MavenSession mavenSession) throws MavenExecutionException {
		List<Plugin> plugins = currentProject.getBuildPlugins();
		for (Plugin plugin : plugins) {
			if (plugin.getGroupId().equals(PLUGIN_GROUPID) && plugin.getArtifactId().equals(PLUGIN_ARTIFACTID)) {
				logger.info(String.format("%s: Processing Mixins for %s", PLUGIN_ARTIFACTID, currentProject.getFile()));
				Mixins mixins = loadConfiguration(plugin.getConfiguration());
				for(Mixin mixin: mixins.getMixins()){
					mixin.merge(mixinModelCache, currentProject, mavenSession, plugin, mixinModelMerger, mavenXpp3reader, repositorySystem);
				}
				if(mixins.getMixins().size() > 0){
					//Apply the pluginManagement section on the plugins section
					mixinModelMerger.applyPluginManagementOnPlugins(currentProject.getModel());

					ProjectBuildingRequest projectBuildingRequest = mavenSession.getProjectBuildingRequest();
					DefaultModelBuildingRequest request = new DefaultModelBuildingRequest();
					request.setActiveProfileIds(projectBuildingRequest.getActiveProfileIds());
					request.setInactiveProfileIds(projectBuildingRequest.getInactiveProfileIds());
					request.setBuildStartTime(projectBuildingRequest.getBuildStartTime());
	
					MixinModelProblemCollector problems = new MixinModelProblemCollector();
					modelInterpolator.interpolateModel(currentProject.getModel(), currentProject.getBasedir(), request, problems);
					pluginConfigurationExpander.expandPluginConfiguration(currentProject.getModel(), request, problems);
					problems.checkErrors(currentProject.getFile());
				}
				logger.info(String.format("%s: Mixins merged successfuly for %s", PLUGIN_ARTIFACTID, currentProject.getFile()));
				break;
			}
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