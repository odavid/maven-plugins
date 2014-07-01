package com.github.odavid.maven.plugins.unpackedrepo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter;
import org.apache.maven.artifact.resolver.filter.TypeArtifactFilter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

public abstract class AbstractUnpackRepoMojo extends AbstractMojo{
	@Component
	MavenProject mavenProject;

	@Component
	MavenProjectHelper projectHelper;

	@Parameter(defaultValue = "${localRepository}")
	ArtifactRepository localRepository;

	@Component
	BuildPluginManager pluginManager;
	
	@Component
	MavenSession mavenSession;
	
	@Parameter(property="unpack.dep.type")
	String type;
	
	@Parameter(property="unpack.dep.classifier")
	String classifier;
	
	@Parameter
	List<String> includeArtifacts;
	
	@Parameter
	List<String> excludeArtifacts;

	List<Artifact> filterArtifacts(){
		AndArtifactFilter filter = new AndArtifactFilter();
		if(type != null){
			filter.add(new TypeArtifactFilter(type));
		}
		if(classifier != null){
			filter.add(new ArtifactFilter(){
				@Override
				public boolean include(Artifact artifact) {
					return artifact.getClassifier().equals(classifier);
				}
			});
		}
		if(includeArtifacts != null && includeArtifacts.size() > 0){
			filter.add(new IncludesArtifactFilter(includeArtifacts));
		}
		if(excludeArtifacts != null && excludeArtifacts.size() > 0){
			filter.add(new ExcludesArtifactFilter(excludeArtifacts));
		}
		
		@SuppressWarnings("unchecked")
		Set<Artifact> deps = mavenProject.getArtifacts();
		
		List<Artifact> artifactItems = new ArrayList<>(); 
		for(Artifact dependency: deps){
			if(filter.include(dependency)){
				artifactItems.add(dependency);
			}
		}
		return artifactItems;
	}
	
	File localRepoBaseDir(){
		return new File(localRepository.getBasedir(), ".unpacked");
	}
	File localRepoMarkersDir(){
		return new File(localRepoBaseDir(), ".markers");
	}

	File getUnpackedFilePath(Artifact dependency) {
		String gid = dependency.getGroupId();
		String artifactId = dependency.getArtifactId();
		String version = dependency.getVersion();
		String classifier = dependency.getClassifier();
		String type = dependency.getType();
		
		gid = gid.replace('.', File.separatorChar);
		File localUnpackedRepo = localRepoBaseDir();
		File folder = new File(localUnpackedRepo, gid);
		folder = new File(folder, version);
		StringBuilder lastname = new StringBuilder(artifactId).append('-').append(version);
		if(classifier != null){
			lastname.append('-').append(classifier);
		}
		lastname.append('.').append(type);
		folder = new File(folder, lastname.toString());
		return folder;
	}
}
