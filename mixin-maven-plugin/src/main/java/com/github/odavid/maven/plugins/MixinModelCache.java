package com.github.odavid.maven.plugins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

@Component(role=MixinModelCache.class)
public class MixinModelCache {
	private Map<String,Model> cache = new HashMap<>();
	
	@Requirement
	private Logger logger;
	
	@Requirement
	private ArtifactFetcher artifactFetcher;
	
	private MavenSession mavenSession;
	
	private MavenXpp3Reader xpp3Reader;

	public void init(MavenSession mavenSession, MavenXpp3Reader xpp3Reader){
		this.mavenSession = mavenSession;
		this.xpp3Reader = xpp3Reader;
	}
	public Model getModel(Mixin mixin, MavenProject mavenProject) throws MavenExecutionException{
		Model model = cache.get(mixin.getKey());
		if(model == null){
			checkMixinVersion(mixin, mavenProject);
			File artifactFile = null;
			for(MavenProject project: mavenSession.getProjects()){
				logger.debug(String.format("Checking if mixin %s is in within the same reactor", mixin));
				if(project.getGroupId().equals(mixin.getGroupId()) && project.getArtifactId().equals(mixin.getArtifactId()) && mixin.getVersion().equals(project.getVersion())){
					File mixinFile = new File(project.getBasedir(), mixin.getType() + ".xml");
					if(mixinFile.exists()){
						artifactFile = mixinFile;
					}
				}
			}
			if(artifactFile == null){
				Artifact artifact = getArtifact(mixin, mavenProject);
				try {
					artifactFile = resolveArtifact(artifact);
				} catch (MojoExecutionException e) {
					throw new MavenExecutionException (String.format("Cannot resolve mixin artifact %s", artifact), e);
				}
				if(artifactFile == null || !artifactFile.exists()){
					throw new MavenExecutionException (String.format("Cannot resolve mixin artifact %s", artifact), new NullPointerException());
				}
			}			
			try {
				logger.debug(String.format("loading mixin %s locally from %s", mixin, artifactFile));
				model = xpp3Reader.read(new FileInputStream(artifactFile));
				model.setVersion(mixin.getVersion());
				model.setGroupId(mixin.getGroupId());
				model.setArtifactId(mixin.getArtifactId());
				
				cache.put(mixin.getKey(), model);
			} catch (FileNotFoundException e) {
				throw new MavenExecutionException (String.format("Cannot find mixin file %s for mixin artifact %s ", artifactFile, mixin) , e);
			} catch (IOException e) {
				throw new MavenExecutionException (String.format("Cannot read mixin file %s for mixin artifact %s ", artifactFile, mixin)  , e);
			} catch (XmlPullParserException e) {
				throw new MavenExecutionException (String.format("Cannot parse mixin file %s for mixin artifact %s ", artifactFile, mixin)  , e);
			}
			
		}
		return model;
	}
	private File resolveArtifact(Artifact artifact) throws MojoExecutionException {
		try {
			artifactFetcher.resolve(artifact, mavenSession);
			return artifact.getFile();
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(String.format( "Error resolving artifact %s", artifact, e));
		}
	}
	private void checkMixinVersion(Mixin mixin, MavenProject currentProject) throws MavenExecutionException {
		if(StringUtils.isEmpty(mixin.getVersion())){
			String groupId = mixin.getGroupId();
			String artifactId = mixin.getArtifactId();
			String depConflictId = mixin.getKey();
			String version = mixin.getVersion();
			String type = mixin.getType();
			
			if (StringUtils.isEmpty(version)) {
				version = null;
				for (org.apache.maven.artifact.Artifact artifact : currentProject.getArtifacts()) {
					if (artifact.getDependencyConflictId().equals(depConflictId)) {
						version = artifact.getVersion();
						mixin.setVersion(version);
						break;
					}
				}
				if(version == null){
					for (Dependency dependency : currentProject.getDependencyManagement().getDependencies()) {
						if (dependency.getArtifactId().equals(artifactId) && dependency.getGroupId().equals(groupId) && dependency.getType().equals(type)) {
							version = dependency.getVersion();
							mixin.setVersion(version);
							break;
						}
					}
				}
				if (version == null) {
					throw new MavenExecutionException( "Cannot find version for " + depConflictId, currentProject.getFile());
				}
			}
		}
	}
	private Artifact getArtifact(Mixin mixin, MavenProject currentProject) throws MavenExecutionException {
		String groupId = mixin.getGroupId();
		String artifactId = mixin.getArtifactId();
		String version = mixin.getVersion();
		String type = mixin.getType();
		return artifactFetcher.createArtifact(groupId, artifactId, type, null, version);
	}

}
