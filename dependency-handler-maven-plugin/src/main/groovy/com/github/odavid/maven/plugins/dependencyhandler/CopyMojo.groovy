package com.github.odavid.maven.plugins.dependencyhandler

import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.ResolutionScope

@Mojo(name="copy", requiresDependencyResolution=ResolutionScope.COMPILE, requiresProject=true, threadSafe=true)
class CopyMojo extends AbstractDependencyHandlerMojo{

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		dependencyMavenPluginCopy(filterDependencies())
	}

}
