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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.codehaus.plexus.util.StringUtils;

public class MavenClasspathTask extends AbstractMavenAntTask {

	public void execute() throws BuildException {
		Project antProject = getProject();
		MavenProject mavenProject = getMavenProject();

		try {
			Path combinedPath = new Path(antProject);
			Path compileClasspath = new Path(getProject());
			compileClasspath.setPath(StringUtils.join(mavenProject
					.getCompileClasspathElements().iterator(),
					File.pathSeparator));

			antProject.addReference("maven.dependency.classpath", compileClasspath);
			antProject.addReference("maven.compile.classpath", compileClasspath);
			createPathPropertyForClasspathWithJars("compile_classpath", compileClasspath);
			combinedPath.add(compileClasspath);
			
			Path runtimeClasspath = new Path(antProject);
			runtimeClasspath.setPath(StringUtils.join(mavenProject
					.getRuntimeClasspathElements().iterator(),
					File.pathSeparator));
			antProject.addReference("maven.runtime.classpath", runtimeClasspath);
			createPathPropertyForClasspathWithJars("runtime_classpath", runtimeClasspath);

			Path testClasspath = new Path(antProject);
			testClasspath.setPath(StringUtils.join(mavenProject.getTestClasspathElements().iterator(), File.pathSeparator));
			antProject.addReference("maven.test.classpath", testClasspath);
			createPathPropertyForClasspathWithJars("test_classpath", testClasspath);

			Collection<Artifact> pluginArtifacts = getPluginArtifacts();
			if (pluginArtifacts != null) {
				Path pluginClasspath = getPathFromArtifacts(pluginArtifacts, antProject);
				/* set maven.plugin.classpath with plugin dependencies */
				antProject.addReference("maven.plugin.classpath", pluginClasspath);
				createPathPropertyForClasspathWithJars("plugin_classpath", pluginClasspath);
				combinedPath.add(pluginClasspath);
			}
			createPathPropertyForClasspathWithJars("combined_classpath", combinedPath);
			
		} catch (DependencyResolutionRequiredException e) {
			throw new BuildException("DependencyResolutionRequiredException: "
					+ e.getMessage(), e);
		}
	}
	
	private void createPathPropertyForClasspathWithJars(String property, Path path){
		PathConvert pathConvert = new PathConvert();
		pathConvert.setProject(getProject());
		pathConvert.setProperty(property);
		pathConvert.add(path);
		GlobPatternMapper mapper = new GlobPatternMapper();
		mapper.setFrom("*.jar");
		mapper.setTo("*.jar");
		pathConvert.add(mapper);
		pathConvert.execute();
	}

	/**
	 * @param artifacts
	 * @param antProject
	 * @return a path
	 * @throws DependencyResolutionRequiredException
	 */
	private Path getPathFromArtifacts(Collection<Artifact> artifacts,
			Project antProject) throws DependencyResolutionRequiredException {
		if (artifacts == null) {
			return new Path(antProject);
		}

		List<String> list = new ArrayList<String>(artifacts.size());
		for (Iterator<Artifact> i = artifacts.iterator(); i.hasNext();) {
			Artifact a = i.next();
			if(!a.isResolved()){
				try {
					getArtifactResolver().resolve(a, getMavenProject().getRemoteArtifactRepositories(), getLocalRepository());
				} catch (Exception e) {
					throw new DependencyResolutionRequiredException(a);
				}
			}
			File file = a.getFile();
			if (file == null) {
				throw new DependencyResolutionRequiredException(a);
			}
			if(!file.getName().endsWith(".pom")){
				list.add(file.getPath());
			}
		}

		Path p = new Path(antProject);
		p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));

		return p;
	}

}
