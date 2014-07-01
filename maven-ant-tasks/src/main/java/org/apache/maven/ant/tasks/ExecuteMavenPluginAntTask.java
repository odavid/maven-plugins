package org.apache.maven.ant.tasks;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.util.List;

import org.apache.maven.model.Plugin;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DynamicConfigurator;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class ExecuteMavenPluginAntTask extends AbstractMavenAntTask {
	private String groupId;
	private String artifactId;
	private String version;
	private String goal;
	private Xpp3DomDynamicElement configuration;

	public void setGoal(String goal) {
		this.goal = goal;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public void execute() throws BuildException {
		try {
			if(StringUtils.isEmpty(version)){
				List<Plugin> plugins = getMavenProject().getPluginManagement().getPlugins();
				for(Plugin plugin: plugins){
					if(plugin.getArtifactId().equals(artifactId) && plugin.getGroupId().equals(groupId)){
						version = plugin.getVersion();
						log("Found plugin version in plugin management - " + groupId +":" + ":"+ artifactId + ":" + version);
						break;
					}
				}
			}
			if(StringUtils.isEmpty(version)){
				String message = "Version for plugin " + groupId +":" + ":"+ artifactId + ", is not specified and not defined in plugin management";
				throw new BuildException(message);
			}
			executeMojo(
					plugin(groupId(groupId), artifactId(artifactId),
							version(version)),
					goal(goal),
					configuration.getElement(),
					executionEnvironment(getMavenProject(), getMavenSession(),
							getPluginManager()));

		} catch (Throwable e) {
			throw new BuildException("Cannot unpack-dependencies: "
					+ e.getMessage(), e);
		}
	}

	public Xpp3DomDynamicElement createConfiguration(){
		configuration = new Xpp3DomDynamicElement("configuration");
		return configuration;
	}
	public void addConfiguration(Xpp3DomDynamicElement configuation){
		configuration = configuation;
	}
	

	
	public static class Xpp3DomDynamicElement implements DynamicConfigurator{
		private Xpp3Dom element;
		
		public Xpp3DomDynamicElement(String name){
			this.element = new Xpp3Dom(name);
		}
		
		@Override
		public void setDynamicAttribute(String name, String value)
				throws BuildException {
			this.element.setAttribute(name, value);
		}
		public Xpp3Dom getElement(){
			return element;
		}

		@Override
		public Object createDynamicElement(String name) throws BuildException {
			Xpp3DomDynamicElement child = new Xpp3DomDynamicElement(name);
			element.addChild(child.getElement());
			return child;
		}
		public void addText(String text) throws BuildException{
			this.element.setValue(text);
		}
	}
}
