package com.github.odavid.maven.plugins.groovy

import org.apache.maven.artifact.Artifact
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.tools.ant.BuildLogger
import org.apache.tools.ant.types.LogLevel
import org.codehaus.gmaven.mojo.GroovyMojo
import org.slf4j.LoggerFactory

import com.github.odavid.maven.ant.logger.MavenAntLogger

abstract class GroovyMojoWithAntLogger extends GroovyMojo{
	private AntBuilder ant
	
	@Parameter(defaultValue='${plugin.artifacts}', readonly=true)
	List<Artifact> pluginArtifacts
	
	private AntBuilder getAnt() {
		if (this.ant == null) {
			AntBuilder ant = new AntBuilder();
			BuildLogger logger = (BuildLogger) ant.getAntProject().getBuildListeners().get(0);
			MavenAntLogger mavenLogger = new MavenAntLogger(ant.getAntProject(), LoggerFactory.getLogger(this.class))
			mavenLogger.messageOutputLevel = (this.log.debugEnabled? LogLevel.DEBUG.level : LogLevel.INFO.level)
			ant.getAntProject().removeBuildListener(logger)
			ant.getAntProject().addBuildListener(mavenLogger)
			this.ant = ant;
		}
		return this.ant;
	}

	@Override
	public Object getProperty(final String property) {
		if ("ant".equals(property)) {
			return getAnt();
		}else{
			return super.getProperty(property);
		}
	}
}
