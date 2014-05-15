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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.ant.tasks.support.SpecificScopesArtifactFilter;
import org.apache.maven.ant.tasks.support.TypesArtifactFilter;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.types.FileSet;

/**
 * Ant task which create a fileset for each dependency in a Maven project, and a 
 * fileset containing all selected dependencies.
 * 
 * @author pgier
 */
public class DependencyFilesetsTask extends AbstractMavenAntTask{
    

    /**
     * The string to prepend to all dependency filesets.
     */
    private String prefix = "";

    /**
     * A comma separated list of artifact types to include.
     */
    private String types = "";

    /**
     * A comma separated list of dependency scopes to include.
     */
    private String scopes = "";

	private String projectDependenciesId = MavenAntTasksConstants.DEFAULT_PROJECT_DEPENDENCIES_ID;

	
	public String getProjectDependenciesId() {
		return projectDependenciesId;
	}

	public void setProjectDependenciesId(String projectDependenciesId) {
		this.projectDependenciesId = projectDependenciesId;
	}

	public String getPrefix() {
		if (prefix == null) {
			prefix = "";
		}
		return prefix;
	}

    /**
     * Prefix to be added to each of the dependency filesets. Default is empty string.
     */
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getTypes() {
		return types;
	}

	public void setTypes(String types) {
		this.types = types;
	}

	public String getScopes() {
		return scopes;
	}

	public void setScopes(String scopes) {
		this.scopes = scopes;
	}    

    public void execute(){
    	MavenProject mavenProject = getMavenProject();
        // Add filesets for depenedency artifacts
		Set<Artifact> depArtifacts = filterArtifacts( mavenProject.getArtifacts() );

        FileSet dependenciesFileSet = new FileSet();
        dependenciesFileSet.setProject( getProject() );
        ArtifactRepository localRepository = getLocalRepository();
        dependenciesFileSet.setDir( new File( localRepository.getBasedir() ) );

        for ( Iterator<Artifact> it = depArtifacts.iterator(); it.hasNext(); ){
            Artifact artifact = it.next();

            String relativeArtifactPath = localRepository.pathOf( artifact );
            dependenciesFileSet.createInclude().setName( relativeArtifactPath );

            String fileSetName = getPrefix() + artifact.getDependencyConflictId();

            FileSet singleArtifactFileSet = new FileSet();
            singleArtifactFileSet.setProject( getProject() );
            singleArtifactFileSet.setFile( artifact.getFile() );
            getProject().addReference( fileSetName, singleArtifactFileSet );
        }

        getProject().addReference( ( getPrefix() + projectDependenciesId  ), dependenciesFileSet );
    }



    /**
     * Filter a set of artifacts using the scopes and type filters.
     * 
     * @param artifacts
     * @return
     */
    public Set<Artifact> filterArtifacts( Set<Artifact> artifacts )
    {
		if (scopes == null) {
			scopes = "";
		}
		if (types == null) {
			types = "";
		}

		if (scopes.equals("") && types.equals("")) {
			return artifacts;
		}

		AndArtifactFilter filter = new AndArtifactFilter();
		if (!scopes.equals("")) {
			filter.add(new SpecificScopesArtifactFilter(getScopes()));
		}
		if (!types.equals("")) {
			filter.add(new TypesArtifactFilter(getTypes()));
		}

		Set<Artifact> artifactsResult = new LinkedHashSet<Artifact>();
		for (Iterator<Artifact> iter = artifacts.iterator(); iter.hasNext();) {
			Artifact artifact = iter.next();
			if (filter.include(artifact)) {
				artifactsResult.add(artifact);
			}
		}
		return artifactsResult;
    }
}
