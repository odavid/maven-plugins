package com.github.ohaddavid.maven.extensions;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.ExecutionListener;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

@Component( role = AbstractMavenLifecycleParticipant.class )
public class LoggingLifecycleParticipant 
    extends AbstractMavenLifecycleParticipant
{
    @Requirement( role = ExecutionListener.class, hint = "logging-execution-listener" )
    private LoggingExtension executionListener;

    @Override
    public void afterSessionStart( MavenSession session )
        throws MavenExecutionException
    {
        // initialize delegate
    	executionListener.setDelegate(session.getRequest().getExecutionListener());
        session.getRequest().setExecutionListener( executionListener );
    }
    
    @Override
    public void afterProjectsRead(MavenSession session)
    		throws MavenExecutionException {
    	// TODO Auto-generated method stub
    	afterSessionStart(session);
    }
}