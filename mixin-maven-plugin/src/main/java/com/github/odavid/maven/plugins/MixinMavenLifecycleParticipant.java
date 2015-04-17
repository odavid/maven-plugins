package com.github.odavid.maven.plugins;

import java.util.List;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.configuration.BeanConfigurator;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.interpolation.ModelInterpolator;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.plugin.PluginConfigurationExpander;
import org.apache.maven.model.plugin.ReportingConverter;
import org.apache.maven.model.profile.ProfileInjector;
import org.apache.maven.model.profile.ProfileSelector;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

@Component(role = AbstractMavenLifecycleParticipant.class, hint = "MixinMavenLifecycleParticipant")
public class MixinMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

	private final MavenXpp3Reader mavenXpp3reader = new MavenXpp3Reader();
	
	@Requirement
	private MixinModelMerger mixinModelMerger;

	@Requirement
	private Logger logger;
	
	@Requirement
	private BeanConfigurator beanConfigurator;
	
    @Requirement
    private ModelInterpolator modelInterpolator;
	
    @Requirement
    private PluginConfigurationExpander pluginConfigurationExpander;
    
    @Requirement
    private ProfileInjector profileInjector;

    @Requirement
    private ProfileSelector profileSelector;

    @Requirement
    private ArtifactFetcher artifactFetcher;

    @Requirement
    private MixinModelCache mixinModelCache;
    
    @Requirement
    private ReportingConverter reportingConverter;
    
    @Requirement
    private RepositorySystem repositorySystem;
    
	@Override
	public void afterSessionStart(MavenSession session) throws MavenExecutionException {
	}

	@Override
	public void afterProjectsRead(MavenSession mavenSession) throws MavenExecutionException {
		logger.info(String.format("%s: Merging Mixins", MixinsProjectLoader.PLUGIN_ARTIFACTID));
		mixinModelCache.init(mavenSession, mavenXpp3reader); 
	
		List<MavenProject> projects = mavenSession.getProjects();
		for (MavenProject module : projects ) {
			MixinsProjectLoader loader = new MixinsProjectLoader(mavenSession, module, 
					modelInterpolator, pluginConfigurationExpander, beanConfigurator, logger, mixinModelCache, profileSelector, profileInjector, mixinModelMerger, reportingConverter, repositorySystem);
			loader.mergeMixins();
		}
		mavenSession.setProjects(projects);
		logger.info(String.format("%s: Mixins were merged", MixinsProjectLoader.PLUGIN_ARTIFACTID));
	}

}