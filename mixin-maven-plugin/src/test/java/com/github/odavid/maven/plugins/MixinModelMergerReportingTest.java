package com.github.odavid.maven.plugins;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.apache.maven.model.Model;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.junit.Before;
import org.junit.Test;

public class MixinModelMergerReportingTest {
	private MixinModelMerger merger;
	private Model source;
	
	@Before
	public void setup(){
		merger = new MixinModelMerger();
		source = new Model();
		source.setReporting(new Reporting());
		source.getReporting().setOutputDirectory("out1");
		source.getReporting().setExcludeDefaults(true);
		
		ReportPlugin plugin = new ReportPlugin();
		plugin.setGroupId("aaa");
		plugin.setArtifactId("aaa");
		plugin.setVersion("1.0");
		
		ReportSet reportSet = new ReportSet();
		reportSet.setId("report-set-1");
		reportSet.setReports(Arrays.asList(new String[]{"1", "2"}));
		plugin.addReportSet(reportSet);
		source.getReporting().addPlugin(plugin);
	}
	
	@Test
	public void testReportingOutputDirectory() {
		Model target = new Model();
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getOutputDirectory(), "out1");
		target.setReporting(new Reporting());
		target.getReporting().setOutputDirectory("out2");
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getOutputDirectory(), "out2");
	}
	@Test
	public void testReportingExcludeDefaults() {
		Model target = new Model();
		target.setReporting(new Reporting());
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getExcludeDefaults(), "true");
		target.getReporting().setExcludeDefaults(false);
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getExcludeDefaults(), "false");
	}
	@Test
	public void testReportingMissingPlugin() {
		Model target = new Model();
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getPlugins().get(0).getArtifactId(), "aaa");
	}
	@Test
	public void testReportingPluginMerge() {
		Model target = new Model();
		ReportPlugin plugin = new ReportPlugin();
		plugin.setGroupId("aaa");
		plugin.setArtifactId("aaa");
		plugin.setVersion("1.1");
		target.setReporting(new Reporting());
		target.getReporting().addPlugin(plugin);
		
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getPlugins().get(0).getVersion(), "1.1");
		assertEquals(target.getReporting().getPlugins().get(0).getReportSets().get(0).getReports().get(0), "1");
	}
	@Test
	public void testReportingPluginMergeWithNewReports() {
		Model target = new Model();
		ReportPlugin plugin = new ReportPlugin();
		
		plugin.setGroupId("aaa");
		plugin.setArtifactId("aaa");
		plugin.setVersion("1.1");
		target.setReporting(new Reporting());
		target.getReporting().addPlugin(plugin);
		
		ReportSet reportSet = new ReportSet();
		reportSet.setId("report-set-1");
		reportSet.setReports(Arrays.asList(new String[]{"3"}));
		plugin.addReportSet(reportSet);

		reportSet = new ReportSet();
		reportSet.setId("report-set-2");
		reportSet.setReports(Arrays.asList(new String[]{"11", "12", "13"}));
		plugin.addReportSet(reportSet);
		
		merger.mergeReporting(target, source);
		assertEquals(target.getReporting().getPlugins().get(0).getVersion(), "1.1");
		assertEquals(target.getReporting().getPlugins().get(0).getReportSets().get(0).getReports().get(0), "3");
		assertEquals(target.getReporting().getPlugins().get(0).getReportSets().get(0).getReports().get(1), "1");
		assertEquals(target.getReporting().getPlugins().get(0).getReportSets().get(0).getReports().get(2), "2");
		assertEquals(target.getReporting().getPlugins().get(0).getReportSets().get(1).getReports().get(2), "13");
	}
}
