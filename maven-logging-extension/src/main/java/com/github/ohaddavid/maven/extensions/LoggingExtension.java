package com.github.ohaddavid.maven.extensions;

import org.apache.maven.execution.AbstractExecutionListener;
import org.apache.maven.execution.ExecutionEvent;
import org.apache.maven.execution.ExecutionListener;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.MDC;

@Component(role = ExecutionListener.class, hint = "logging-execution-listener")
public class LoggingExtension extends AbstractExecutionListener {
	private ExecutionListener delegate;

	public void setDelegate(ExecutionListener executionListener) {
		this.delegate = executionListener;
	}
	
	@Override
	public void projectStarted(ExecutionEvent event) {
		delegate.projectStarted(event);
		String artifactId = event.getProject().getArtifactId();
		String groupId = event.getProject().getGroupId();
		MDC.put("moduleEyeCatcher", "|" + groupId + ":" +artifactId + "|");
	}

	public void projectDiscoveryStarted(ExecutionEvent event) {
		delegate.projectDiscoveryStarted(event);
	}

	public void sessionStarted(ExecutionEvent event) {
		delegate.sessionStarted(event);
	}

	public void sessionEnded(ExecutionEvent event) {
		delegate.sessionEnded(event);
	}

	public void projectSkipped(ExecutionEvent event) {
		delegate.projectSkipped(event);
	}

	public void projectSucceeded(ExecutionEvent event) {
		delegate.projectSucceeded(event);
		MDC.remove("moduleEyeCatcher");
	}

	public void projectFailed(ExecutionEvent event) {
		delegate.projectFailed(event);
		MDC.remove("moduleEyeCatcher");
	}

	public void forkStarted(ExecutionEvent event) {
		delegate.forkStarted(event);
	}

	public void forkSucceeded(ExecutionEvent event) {
		delegate.forkSucceeded(event);
	}

	public void forkFailed(ExecutionEvent event) {
		delegate.forkFailed(event);
	}

	public void mojoSkipped(ExecutionEvent event) {
		delegate.mojoSkipped(event);
	}

	public void mojoStarted(ExecutionEvent event) {
		delegate.mojoStarted(event);
	}

	public void mojoSucceeded(ExecutionEvent event) {
		delegate.mojoSucceeded(event);
	}

	public void mojoFailed(ExecutionEvent event) {
		delegate.mojoFailed(event);
	}

	public void forkedProjectStarted(ExecutionEvent event) {
		delegate.forkedProjectStarted(event);
	}

	public void forkedProjectSucceeded(ExecutionEvent event) {
		delegate.forkedProjectSucceeded(event);
	}

	public void forkedProjectFailed(ExecutionEvent event) {
		delegate.forkedProjectFailed(event);
	}
}