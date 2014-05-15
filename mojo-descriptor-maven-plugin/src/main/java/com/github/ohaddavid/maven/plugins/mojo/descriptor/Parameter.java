package com.github.ohaddavid.maven.plugins.mojo.descriptor;

public class Parameter {

	public static final Parameter LOCAL_REPO = new Parameter("maven.local.repository",
			"${localRepository}",
			true, true, "org.apache.maven.artifact.repository.ArtifactRepository", "${localRepository}");

	public static final Parameter MAVEN_SESSION = new Parameter("maven.session",
			"${session}",
			true, true, "org.apache.maven.execution.MavenSession", "${session}");

	public static final Parameter MAVEN_PROJECT = new Parameter("maven.project",
			"${project}",
			true, true, "org.apache.maven.project.MavenProject", "${project}");

	public static final Parameter PLUGIN_ARTIFACTS = new Parameter("maven.pluginArtifacts", "${plugin.artifacts}", 
			true, true, "java.util.List", "${plugin.artifacts}");
			
	private String name;
	private String expression;
	private boolean required;
	private boolean readonly;
	private String type;
	private String defaultValue;
	private String description;

	public Parameter(){}

	public Parameter(String name, String expression, boolean required, boolean readonly, String type, String defaultValue) {
		this(name, expression, required, readonly, type, defaultValue, null);
	}
	public Parameter(String name, String expression, boolean required, boolean readonly, String type, String defaultValue, String description) {
		this.name = name;
		this.expression = expression;
		this.required = required;
		this.readonly = readonly;
		this.type = type;
		this.defaultValue = defaultValue;
		this.description = description;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	public boolean isReadonly() {
		return readonly;
	}
	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDefaultValue() {
		return defaultValue;
	}
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
