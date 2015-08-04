package com.github.odavid.maven.plugins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.configuration.BeanConfigurationException;
import org.apache.maven.configuration.BeanConfigurationRequest;
import org.apache.maven.configuration.BeanConfigurator;
import org.apache.maven.configuration.DefaultBeanConfigurationRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.DefaultModelBuildingRequest;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.plugin.PluginConfigurationExpander;
import org.apache.maven.model.plugin.ReportingConverter;
import org.apache.maven.model.profile.DefaultProfileActivationContext;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.plugin.lifecycle.Execution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class MixinsProjectLoader {
	public static final String PLUGIN_GROUPID = "com.github.odavid.maven.plugins";
	public static final String PLUGIN_ARTIFACTID = "mixin-maven-plugin";

	private MavenSession mavenSession;
	private MavenProject mavenProject;
	private ProfileSelector profileSelector;
    private ProfileInjector profileInjector;
    private MixinModelMerger mixinModelMerger;
    private ModelInterpolator modelInterpolator;
    private PluginConfigurationExpander pluginConfigurationExpander;
	private BeanConfigurator beanConfigurator;
	private ReportingConverter reportingConverter;
	private RepositorySystem repositorySystem;

    private DefaultModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest();
    private MixinModelCache mixinModelCache;
	private Logger logger;

	public MixinsProjectLoader(MavenSession mavenSession, MavenProject mavenProject, ModelInterpolator modelInterpolator, 
			PluginConfigurationExpander pluginConfigurationExpander, 
			BeanConfigurator beanConfigurator, Logger logger, 
			MixinModelCache mixinModelCache, ProfileSelector profileSelector, ProfileInjector profileInjector, 
			MixinModelMerger mixinModelMerger, ReportingConverter reportingConverter, RepositorySystem repositorySystem){
		this.mavenSession = mavenSession;
		this.mavenProject = mavenProject;
		this.modelInterpolator = modelInterpolator;
		this.pluginConfigurationExpander = pluginConfigurationExpander;
		this.beanConfigurator = beanConfigurator;
		this.logger = logger;
		this.mixinModelCache = mixinModelCache;
		this.profileSelector = profileSelector;
		this.profileInjector = profileInjector;
		this.mixinModelMerger = mixinModelMerger;
		this.reportingConverter = reportingConverter;
		this.repositorySystem = repositorySystem;
		
		ProjectBuildingRequest projectBuildingRequest = mavenSession.getProjectBuildingRequest();
		modelBuildingRequest.setActiveProfileIds(projectBuildingRequest.getActiveProfileIds());
		modelBuildingRequest.setInactiveProfileIds(projectBuildingRequest.getInactiveProfileIds());
		modelBuildingRequest.setBuildStartTime(projectBuildingRequest.getBuildStartTime());
	}
	
	public void mergeMixins() throws MavenExecutionException {
		List<Mixin> mixinList = new ArrayList<>();
		Map<String,Mixin> mixinMap = new HashMap<String, Mixin>();
		fillMixins(mixinList, mixinMap, mavenProject.getModel());
		MixinModelProblemCollector problems = new MixinModelProblemCollector();
		ModelBuildingRequest request = new DefaultModelBuildingRequest(modelBuildingRequest);
		request.setSystemProperties(mavenSession.getSystemProperties());
		request.setUserProperties(mavenSession.getUserProperties());

		Set<String> mixinProfiles = new HashSet<String>();
		for(Mixin mixin: mixinList){
			logger.debug(String.format("Merging mixin: %s into %s", mixin.getKey(), mavenProject.getFile()));
			Model mixinModel = mixinModelCache.getModel(mixin, mavenProject);
			if(mixin.isActivateProfiles()){
				logger.debug(String.format("Activating profiles in mixin: %s into %s", mixin.getKey(), mavenProject.getFile()));
				mixinModel = mixinModel.clone();
	            List<Profile> activePomProfiles =
	                    profileSelector.getActiveProfiles( mixinModel.getProfiles(), getProfileActivationContext(), problems );
				for(Profile profile: activePomProfiles){
					logger.debug(String.format("Activating profile %s in mixin: %s into %s", profile.getId(), mixin.getKey(), mavenProject.getFile()));
					profileInjector.injectProfile(mixinModel, profile, modelBuildingRequest, problems);
					mixinProfiles.add(profile.getId());
				}
			}
			// https://issues.apache.org/jira/browse/MSITE-484
			// The merging of reportPlugins is problematice today. The reportingConverter adds reportPlugins to the site configuration, but they are not
			// merged as plugins, and therefore create issues of mixture between reports.
			// The workaround for that is to remove all reportPlugins if the user defined reporting section in the original pom. After the mixin will be merged. 
			boolean hasReporting = mavenProject.getModel().getReporting() != null; 
			if(hasReporting){
				removeSitePluginReportPlugins(mavenProject.getModel());
			}
			mixin.merge(mixinModel, mavenProject, mavenSession, mixinModelMerger);
			if(hasReporting){
				//Need to convert old style reporting before merging the mixin, so the site plugin will be merged correctly
				reportingConverter.convertReporting(mavenProject.getModel(), request, problems);
			}
		}
		if(mixinList.size() > 0){
			//Apply the pluginManagement section on the plugins section
			mixinModelMerger.applyPluginManagementOnPlugins(mavenProject.getModel());

			modelInterpolator.interpolateModel(mavenProject.getModel(), mavenProject.getBasedir(), request, problems);
			pluginConfigurationExpander.expandPluginConfiguration(mavenProject.getModel(), request, problems);
			if(mavenProject.getInjectedProfileIds().containsKey(Profile.SOURCE_POM)){
				mavenProject.getInjectedProfileIds().get(Profile.SOURCE_POM).addAll(mixinProfiles);
			}else{
				mavenProject.getInjectedProfileIds().put(Profile.SOURCE_POM, new ArrayList<String>(mixinProfiles));
			}
			problems.checkErrors(mavenProject.getFile());

			setupMaven33DistributionManagement(mavenProject);
		}
	}
	
	private void removeSitePluginReportPlugins(Model model) {
		cleanSitePluginFromReportPlugins(model.getBuild().getPluginManagement());
		cleanSitePluginFromReportPlugins(model.getBuild());
	}

	private Plugin cleanSitePluginFromReportPlugins(PluginContainer pluginContainer) {
		if(pluginContainer == null){
			return null;
		}
		Plugin sitePlugin = null;
		for(Plugin plugin: pluginContainer.getPlugins()){
			if("maven-site-plugin".equals( plugin.getArtifactId() ) && "org.apache.maven.plugins".equals( plugin.getGroupId() )){
				sitePlugin = plugin;
				break;
			}
		}
		cleanReportPluginsFromConfiguration(sitePlugin);
		if(sitePlugin != null){
			for(PluginExecution pluginExecution: sitePlugin.getExecutions()){
				cleanReportPluginsFromConfiguration(pluginExecution);
			}
		}
		return sitePlugin;
	}
	void cleanReportPluginsFromConfiguration(ConfigurationContainer configurationContainer){
		if(configurationContainer == null) return;
		if(configurationContainer.getConfiguration() != null){
			Xpp3Dom dom = (Xpp3Dom)configurationContainer.getConfiguration();
            for ( int i = dom.getChildCount() - 1; i >= 0; i-- ){
                Xpp3Dom child = dom.getChild( i );
                if ( "reportPlugins".equals( child.getName() ) ){
                    dom.removeChild( i );
                }
            }
		}
	}

	/** 
	 * Maven &gt; 3.3 changed the way distributionManagement is being built. It is now being initialized during the projectbuilder phase, 
	 * and therefore if a mixin is adding distributionManagement, we need to setup again
	 */
	private void setupMaven33DistributionManagement(MavenProject mavenProject) {
        if ( mavenProject.getDistributionManagementArtifactRepository() == null && mavenProject.getDistributionManagement() != null){
        	if(mavenProject.getDistributionManagement().getSnapshotRepository() != null){
        		mavenProject.setSnapshotArtifactRepository(createRepo(mavenProject.getDistributionManagement().getSnapshotRepository()));
        	}
        	if(mavenProject.getDistributionManagement().getRepository() != null){
        		mavenProject.setReleaseArtifactRepository(createRepo(mavenProject.getDistributionManagement().getRepository()));
        	}
        }
	}
	
	private ArtifactRepository createRepo(DeploymentRepository deploymentRepo){
        try{
            ArtifactRepository repo = repositorySystem.buildArtifactRepository( deploymentRepo );
            repositorySystem.injectProxy( mavenSession.getRepositorySession(), Arrays.asList( repo ) );
            repositorySystem.injectAuthentication( mavenSession.getRepositorySession(), Arrays.asList( repo ) );
            return repo;
        } catch ( InvalidRepositoryException e ) {
            throw new IllegalStateException( "Failed to create distribution repository " + deploymentRepo.getId() + " for " + mavenProject.getId(), e );
        }
		
	}

	private void fillMixins(List<Mixin> mixinList, Map<String,Mixin> mixinMap, Model model) throws MavenExecutionException {
		//Merge properties of current Project with mixin for interpolateModel to work correctly 
		model = model.clone();
		Properties origProperties = model.getProperties() != null ? model.getProperties() : new Properties();
		origProperties.putAll(mavenProject.getProperties());
		model.setProperties(origProperties);
		MixinModelProblemCollector problems = new MixinModelProblemCollector();

		ModelBuildingRequest request = new DefaultModelBuildingRequest(modelBuildingRequest);
		request.setSystemProperties(mavenSession.getSystemProperties());
		request.setUserProperties(mavenSession.getUserProperties());
		
		modelInterpolator.interpolateModel(model, mavenProject.getBasedir(), request, problems);
		if(model.getBuild() == null){
			model.setBuild(new Build());
		}
		List<Plugin> plugins = model.getBuild().getPlugins();
		for (Plugin plugin : plugins) {
			if (plugin.getGroupId().equals(PLUGIN_GROUPID) && plugin.getArtifactId().equals(PLUGIN_ARTIFACTID)) {
				Mixins mixins = loadConfiguration(plugin.getConfiguration());
				//First start with the base level and then add the inherited mixins
				for(Mixin mixin: mixins.getMixins()){
					if(!mixinMap.containsKey(mixin.getKey())){
						logger.debug(String.format("Adding mixin: %s to cache", mixin.getKey()));

						mixinModelCache.getModel(mixin, mavenProject);
						mixinMap.put(mixin.getKey(), mixin);
						mixinList.add(mixin);
					}
				}
				for(Mixin mixin: mixins.getMixins()){
					if(mixin.isRecurse()){
						Model mixinModel = mixinModelCache.getModel(mixin, mavenProject);
						fillMixins(mixinList, mixinMap, mixinModel);
					}
				}
			}
		}
	}

    private DefaultProfileActivationContext getProfileActivationContext() {
        DefaultProfileActivationContext context = new DefaultProfileActivationContext();
        List<String> activeProfileIds = new ArrayList<>();
        List<String> inactiveProfileIds = new ArrayList<>();
        for(Profile profile: mavenProject.getActiveProfiles()){
        	activeProfileIds.add(profile.getId());
        }
        activeProfileIds.addAll(modelBuildingRequest.getActiveProfileIds());
        for(Profile profile: mavenProject.getModel().getProfiles()){
        	if(profile.getActivation() != null && !activeProfileIds.contains(profile.getId())){
        		inactiveProfileIds.add(profile.getId());
        	}
        }
        inactiveProfileIds.addAll(modelBuildingRequest.getInactiveProfileIds());
        context.setActiveProfileIds( activeProfileIds);
        context.setInactiveProfileIds( inactiveProfileIds );
        context.setSystemProperties( mavenSession.getSystemProperties() );
        context.setUserProperties( mavenSession.getUserProperties() );
        context.setProjectDirectory( mavenProject.getBasedir() );
        return context;
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
