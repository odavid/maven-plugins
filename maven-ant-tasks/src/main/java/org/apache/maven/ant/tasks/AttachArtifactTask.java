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

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.FileUtils;

public class AttachArtifactTask extends AbstractMavenAntTask{
	/**
     * The file to attach.
     */
    private File file;
    
    /**
     * The classifier of the artifact to attach
     */
    private String classifier;
    
    /**
     * The type of the artifact to attach.  Defaults to file extension.
     */
    private String type;


	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public String getClassifier() {
		return classifier;
	}

	public void setClassifier(String classifier) {
		this.classifier = classifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
    
    
    @SuppressWarnings("deprecation")
	public void execute(){
		if (file == null) {
			throw new BuildException("File is a required parameter.");
		}

		if (!file.exists()) {
			throw new BuildException("File does not exist: " + file);
		}

		if (type == null) {
			type = FileUtils.getExtension(file.getName());
		}

        MavenProject mavenProject = getMavenProject();

        MavenProjectHelper mavenProjectHelper = getMavenProjectHelper();
        if(mavenProjectHelper == null){
            MavenSession session = getMavenSession();
            try {
            	mavenProjectHelper = (MavenProjectHelper)session.getContainer().lookup(MavenProjectHelper.ROLE);
			} catch (ComponentLookupException e) {
			}
        }
        
        if ( mavenProjectHelper == null ) {
            throw new BuildException( "Maven project helper reference not found: " + getMavenProjectHelperRefId() );
        }

        log( "Attaching " + file + " as an attached artifact", Project.MSG_VERBOSE );
        mavenProjectHelper.attachArtifact( mavenProject, type, classifier, file );
    }
}
