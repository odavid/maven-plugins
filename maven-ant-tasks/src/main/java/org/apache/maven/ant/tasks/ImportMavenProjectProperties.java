package org.apache.maven.ant.tasks;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;

public class ImportMavenProjectProperties extends AbstractMavenAntTask {

	
	private String propertyPrefix = "";

	private String versionsPropertyName = MavenAntTasksConstants.DEFAULT_VERSION_PROPERTY_NAME;

	public String getVersionsPropertyName() {
		return versionsPropertyName;
	}


	public void setVersionsPropertyName(String versionsPropertyName) {
		this.versionsPropertyName = versionsPropertyName;
	}


	public String getPropertyPrefix() {
		return propertyPrefix;
	}


	public void setPropertyPrefix(String propertyPrefix) {
		this.propertyPrefix = propertyPrefix;
	}


	public void execute() throws BuildException {
		Project antProject = getProject();
		MavenProject mavenProject = getMavenProject();
		ArtifactRepository localRepository = getLocalRepository();
		
        // Add some of the common maven properties
        log("Setting properties with prefix: " + propertyPrefix, Project.MSG_DEBUG);
        
        antProject.setProperty( ( propertyPrefix + "project.basedir" ), mavenProject.getBasedir().getAbsolutePath());
        antProject.setProperty( ( propertyPrefix + "project.groupId" ), mavenProject.getGroupId() );
        antProject.setProperty( ( propertyPrefix + "project.artifactId" ), mavenProject.getArtifactId() );
        antProject.setProperty( ( propertyPrefix + "project.name" ), mavenProject.getName() );
        if ( mavenProject.getDescription() != null){
            antProject.setProperty( ( propertyPrefix + "project.description" ), mavenProject.getDescription() );            
        }
        antProject.setProperty( ( propertyPrefix + "project.version" ), mavenProject.getVersion() );
        antProject.setProperty( ( propertyPrefix + "project.packaging" ), mavenProject.getPackaging() );
        antProject.setProperty( ( propertyPrefix + "project.build.directory" ), mavenProject.getBuild().getDirectory() );
        antProject.setProperty( ( propertyPrefix + "project.build.outputDirectory" ),
                                mavenProject.getBuild().getOutputDirectory() );
        antProject.setProperty( ( propertyPrefix + "project.build.testOutputDirectory" ),
                                mavenProject.getBuild().getTestOutputDirectory() );
        antProject.setProperty( ( propertyPrefix + "project.build.sourceDirectory" ),
                                mavenProject.getBuild().getSourceDirectory() );
        antProject.setProperty( ( propertyPrefix + "project.build.testSourceDirectory" ),
                                mavenProject.getBuild().getTestSourceDirectory() );
        
        antProject.setProperty( ( propertyPrefix + "project.build.scriptSourceDirectory" ),
                mavenProject.getBuild().getScriptSourceDirectory());
        
        antProject.setProperty( ( propertyPrefix + "project.build.finalName" ),
                mavenProject.getBuild().getFinalName());
        
        if(localRepository != null){
	        antProject.setProperty( ( propertyPrefix + "localRepository" ), localRepository.toString() );
	        antProject.setProperty( ( propertyPrefix + "settings.localRepository" ), localRepository.getBasedir() );
        }
        
        
        // Add properties for depenedency artifacts
        Set<Artifact> depArtifacts = mavenProject.getArtifacts();
        for ( Iterator<Artifact> it = depArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();

            String propName = artifact.getDependencyConflictId();

            antProject.setProperty( propertyPrefix + propName, artifact.getFile().getPath() );
        }
        Collection<Artifact> pluginArtifacts = getPluginArtifacts();
        for ( Iterator<Artifact> it = pluginArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();

            String propName = artifact.getDependencyConflictId();

            antProject.setProperty( propertyPrefix + propName, artifact.getFile().getPath() );
        }

        // Add a property containing the list of versions for the mapper
        StringBuffer versionsBuffer = new StringBuffer();
        for ( Iterator<Artifact> it = depArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();

            versionsBuffer.append( artifact.getVersion() + File.pathSeparator );
        }
        antProject.setProperty( versionsPropertyName, versionsBuffer.toString() );

        // Add properties in deprecated format to depenedency artifacts
        // This should be removed in future versions of the antrun plugin.
        for ( Iterator<Artifact> it = depArtifacts.iterator(); it.hasNext(); )
        {
            Artifact artifact = it.next();

            String propName = getDependencyArtifactPropertyName( artifact );

            antProject.setProperty( propName, artifact.getFile().getPath() );
        }
        Set<Artifact> plugins = mavenProject.getPluginArtifacts();
        if(plugins != null){
	        for ( Iterator<Artifact> it = plugins.iterator(); it.hasNext(); )
	        {
	            Artifact artifact = it.next();
	            if(!artifact.isResolved()){
		            try {
						ArtifactResolver artifactResolver = getArtifactResolver();
						artifactResolver.resolve(artifact, mavenProject.getRemoteArtifactRepositories(), getLocalRepository());
					} catch (Exception e) {
						throw new BuildException("Cannot resolve artifact: " + artifact.getDependencyConflictId());
					}
	            }
	            String propName = artifact.getDependencyConflictId();
	            artifact.getArtifactHandler().getDirectory();
	            File file = artifact.getFile();
	            antProject.setProperty( propertyPrefix + propName, file.getPath() );
	        }
        }        
	}
    /**
     * Prefix for legacy property format.
     * @deprecated This should only be used for generating the old property format.
     */
    public static final String DEPENDENCY_PREFIX = "maven.dependency.";

    /**
     * Returns a property name for a dependency artifact.  The name is in the format
     * maven.dependency.groupId.artifactId[.classifier].type.path
     *
     * @param artifact
     * @return property name
     * @deprecated The dependency conflict ID should be used as the property name.
     */
    public static String getDependencyArtifactPropertyName( Artifact artifact )
    {
        String key = DEPENDENCY_PREFIX + artifact.getGroupId() + "." + artifact.getArtifactId()
            + ( artifact.getClassifier() != null ? "." + artifact.getClassifier() : "" )
            + ( artifact.getType() != null ? "." + artifact.getType() : "" ) + ".path";
        return key;
    }
}
