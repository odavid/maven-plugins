package org.apache.maven.ant.tasks;

import org.apache.tools.ant.BuildException;

public class ExportMavenProjectProperty extends AbstractMavenAntTask {
	private String property;
	
	private String value;
	
	private boolean overwrite;
	
	
	public boolean isOverwrite() {
		return overwrite;
	}


	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}


	public String getProperty() {
		return property;
	}


	public void setProperty(String property) {
		this.property = property;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	@Override
	public void execute() throws BuildException {
		boolean alreadyExist = getMavenProject().getProperties().containsKey(property);
		if(alreadyExist && overwrite || !alreadyExist){
			getMavenProject().getProperties().setProperty(property, value);
		}
	}
}
