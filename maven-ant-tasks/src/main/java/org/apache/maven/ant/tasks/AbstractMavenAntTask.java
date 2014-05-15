package org.apache.maven.ant.tasks;

import java.util.Collection;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class AbstractMavenAntTask extends Task {
    private String mavenProjectRefId = MavenAntTasksConstants.DEFAULT_MAVEN_PROJECT_REFID;
	private String mavenSessionRefId = MavenAntTasksConstants.DEFAULT_SESSION_REFID;
	private String pluginArtifactsId = MavenAntTasksConstants.DEFAULT_PLUGIN_ARTIFACTS_REFID;
	private String mavenProjectHelperRefId = MavenAntTasksConstants.DEFAULT_MAVEN_PROJECT_HELPER_REFID;
	private String localRepositoryRefId = MavenAntTasksConstants.DEFAULT_LOCAL_REPOSITORY_REFID;
	

	public String getLocalRepositoryRefId() {
		return localRepositoryRefId;
	}

	public void setLocalRepositoryRefId(String localRepositoryRefId) {
		this.localRepositoryRefId = localRepositoryRefId;
	}

	public String getMavenProjectHelperRefId() {
		return mavenProjectHelperRefId;
	}

	public void setMavenProjectHelperRefId(String mavenProjectHelperRefId) {
		this.mavenProjectHelperRefId = mavenProjectHelperRefId;
	}

	public String getMavenSessionRefId() {
		return mavenSessionRefId;
	}

	public void setMavenSessionRefId(String mavenSessionRefId) {
		this.mavenSessionRefId = mavenSessionRefId;
	}

	public String getMavenProjectRefId() {
		return mavenProjectRefId;
	}

	public void setMavenProjectRefId(String mavenProjectRefId) {
		this.mavenProjectRefId = mavenProjectRefId;
	}
	
	public String getPluginArtifactsId() {
		return pluginArtifactsId;
	}

	public void setPluginArtifactsId(String pluginArtifactsId) {
		this.pluginArtifactsId = pluginArtifactsId;
	}

	protected MavenProject getMavenProject() throws BuildException{
        if ( this.getProject().getReference( mavenProjectRefId ) == null ){
            throw new BuildException( "Maven project reference not found: " + mavenProjectRefId );
        }

        return (MavenProject) this.getProject().getReference( mavenProjectRefId );
	}
	
	protected MavenSession getMavenSession() throws BuildException{
		if(this.getProject().getReference(mavenSessionRefId) == null){
            throw new BuildException( "Maven session reference not found: " + mavenSessionRefId );
		}
		return (MavenSession)this.getProject().getReference(mavenSessionRefId);
	}
	
	protected Collection<Artifact> getPluginArtifacts() throws BuildException{
		Collection<Artifact> pluginArtifacts = this.getProject().getReference(pluginArtifactsId);
		return pluginArtifacts;
	}
	protected MavenProjectHelper getMavenProjectHelper(){
		return (MavenProjectHelper)getProject().getReference(mavenProjectHelperRefId);
	}
	
	protected ArtifactRepository getLocalRepository(){
		return (ArtifactRepository) getProject().getReference( localRepositoryRefId);
	}
	@SuppressWarnings("deprecation")
	protected ArtifactResolver getArtifactResolver(){
		try{
			return (ArtifactResolver)getMavenSession().getContainer().lookup(ArtifactResolver.class.getName());
		}catch(Exception e){
			throw new RuntimeException("Could not find artifact resolver in context");
		}
	}
	@SuppressWarnings("deprecation")
	protected BuildPluginManager getPluginManager(){
		try{
			return (BuildPluginManager )getMavenSession().getContainer().lookup(BuildPluginManager.class.getName());
		}catch(Exception e){
			throw new RuntimeException("Could not find BuildPluginManager  in context");
		}
	}
	
	
	/**
	 * Only relevant for ant based mojo
	 * @return
	 */
	protected MojoExecution getCurrentMojoExecution(){
		return (MojoExecution)getProject().getReference("mojoExecution");
	}
}
