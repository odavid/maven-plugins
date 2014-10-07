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
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

public class MixinModelCache {
	private Map<String,Model> cache = new HashMap<>();
	@SuppressWarnings("unused")
	private Logger logger;
	
	public MixinModelCache(Logger logger){
		this.logger = logger;
	}
	public Model getModel(Mixin mixin, MavenProject mavenProject, MavenSession mavenSession, MavenXpp3Reader xpp3Reader, ArtifactFetcher artifactFetcher) throws MavenExecutionException{
		Model model = cache.get(mixin.getKey());
		if(model == null){
			File artifactFile = null;
			for(MavenProject project: mavenSession.getProjects()){
				if(project.getGroupId().equals(mixin.getGroupId()) && project.getArtifactId().equals(mixin.getArtifactId())){
					File mixinFile = new File(project.getBasedir(), mixin.getType() + ".xml");
					if(mixinFile.exists()){
						artifactFile = mixinFile;
					}
				}
			}
			if(artifactFile == null){
				Artifact artifact = getArtifact(mixin, mavenProject, artifactFetcher);
				try {
					artifactFile = resolveArtifact(mavenSession, artifact, artifactFetcher);
				} catch (MojoExecutionException e) {
					throw new MavenExecutionException (String.format("Cannot resolve mixin artifact %s", artifact), e);
				}
				if(artifactFile == null || !artifactFile.exists()){
					throw new MavenExecutionException (String.format("Cannot resolve mixin artifact %s", artifact), new NullPointerException());
				}
			}			
			try {
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
	private File resolveArtifact(MavenSession mavenSession, Artifact artifact, ArtifactFetcher artifactFetcher) throws MojoExecutionException {
		try {
			artifactFetcher.resolve(artifact, mavenSession);
			return artifact.getFile();
		} catch (ArtifactResolutionException e) {
			throw new MojoExecutionException(String.format( "Error resolving artifact %s", artifact, e));
		}
	}
	private Artifact getArtifact(Mixin mixin, MavenProject currentProject, ArtifactFetcher artifactFetcher) throws MavenExecutionException {
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
					break;
				}
			}
			if (version == null) {
				throw new MavenExecutionException( "Cannot find version for " + depConflictId, currentProject.getFile());
			}
		}
		return artifactFetcher.createArtifact(groupId, artifactId, type, null, version);
	}

}
