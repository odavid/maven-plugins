package org.apache.maven.ant.tasks;

import java.util.Properties;

import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.BuildException;
import org.codehaus.plexus.util.StringUtils;

public class ImportPomProperties extends AbstractMavenAntTask{

	private String keyPattern;
	
	private String prefix = "";
	
	public String getKeyPattern() {
		return keyPattern;
	}

	public void setKeyPattern(String keyPattern) {
		this.keyPattern = keyPattern;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public void execute() throws BuildException {
		if(StringUtils.isEmpty(keyPattern)){
			throw new BuildException("keyPattern is required");
		}

		MavenProject mavenProject = getMavenProject();
		Properties mavenProperties = mavenProject.getProperties();
		
		for(String key: mavenProperties.stringPropertyNames()){
			if(key.matches(keyPattern)){
				getProject().setProperty(prefix + key, mavenProject.getProperties().getProperty(key));
			}
		}
	}
}
