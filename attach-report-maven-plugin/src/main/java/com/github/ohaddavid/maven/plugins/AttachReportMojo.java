package com.github.ohaddavid.maven.plugins;


import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@Mojo(name = "attach-report", defaultPhase = LifecyclePhase.SITE)
public class AttachReportMojo extends AbstractMavenReport {

	@Parameter(required = true, defaultValue = "attached-report")
	private String name;

	@Parameter(required = true, defaultValue = "This is an attached report")
	private String description;

	@Parameter(required = true, defaultValue = "attached-report")
	private String baseDirectory;

	@Parameter(required = true, defaultValue = "index")
	private String index;

	@Parameter(required = true)
	private File sourceDirectory;

	@Component
	private Renderer siteRenderer;

	@Component
	private MavenProject project;

	@Override
	protected void executeReport(Locale locale) throws MavenReportException {
		File outDir = new File(getReportOutputDirectory(), this.baseDirectory);
		try {
			FileUtils.copyDirectory(sourceDirectory, outDir);
		} catch (IOException e) {
			throw new MavenReportException("Could not copy " + sourceDirectory + " to " + outDir, e);
		}
	}

	@Override
	protected String getOutputDirectory() {
		return new File(getReportOutputDirectory(), this.baseDirectory).getPath();
	}

	@Override
	protected MavenProject getProject() {
		return this.project;
	}

	@Override
	protected Renderer getSiteRenderer() {
		return this.siteRenderer;
	}

	@Override
	public String getDescription(Locale locale) {
		return this.description;
	}

	@Override
	public String getName(Locale locale) {
		return this.name;
	}

	/**
	 * @return html file for attached report index
	 */
	@Override
	public String getOutputName() {
		return this.baseDirectory + '/' + index;
	}

	@Override
	public boolean isExternalReport() {
		return true;
	}

	/**
	 * @return true only if original report was generated. Otherwise, the report would not be created and added to the site
	 */
	@Override
	public boolean canGenerateReport() {
		File indexFile = new File(sourceDirectory, index + ".html");
		return indexFile.exists() && super.canGenerateReport();
	}
}
