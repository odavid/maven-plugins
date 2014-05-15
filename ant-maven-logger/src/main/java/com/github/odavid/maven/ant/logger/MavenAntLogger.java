package com.github.odavid.maven.ant.logger;

import static org.apache.tools.ant.Project.MSG_DEBUG;
import static org.apache.tools.ant.Project.MSG_ERR;
import static org.apache.tools.ant.Project.MSG_INFO;
import static org.apache.tools.ant.Project.MSG_VERBOSE;
import static org.apache.tools.ant.Project.MSG_WARN;

import java.io.PrintStream;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Echo;
import org.apache.tools.ant.taskdefs.Echo.EchoLevel;
import org.slf4j.Logger;

public class MavenAntLogger extends DefaultLogger {
	private static final EchoLevel ECHO_LEVEL_INFO;
	static{
		ECHO_LEVEL_INFO = new EchoLevel();
		ECHO_LEVEL_INFO.setValue(EchoLevel.INFO.getValue());
	}

    private Logger logger;
    private Project project;

    public MavenAntLogger(Project project, Logger logger) {
        this.logger = logger;
        this.project = project;
    }

    @Override
    protected void printMessage(String message, PrintStream stream, int priority) {
        if(message == null){
            return;
        }
        switch (priority) {
            case MSG_DEBUG:
                if (logger.isDebugEnabled()) {
                    logger.debug(message.trim());
                }
                break;
            case MSG_VERBOSE:
                if (logger.isDebugEnabled()) {
                    logger.debug(message.trim());
                }
                break;
            case MSG_INFO:
                logger.info(message.trim());
                break;
            case MSG_WARN:
                logger.warn(message.trim());
                break;
            case MSG_ERR:
                logger.error(message.trim());
        }
    }
    
    @Override
    public void messageLogged(BuildEvent event) {
    	//By Default echo is WARN level, but we want to have it as INFO
    	if(event.getTask() instanceof Echo){
    		Echo echo = (Echo)event.getTask();
    		if(event.getPriority() == MSG_WARN){
    			echo.setLevel(ECHO_LEVEL_INFO);
    			BuildEvent newevent = new BuildEvent(echo);
    			newevent.setMessage(event.getMessage(), MSG_INFO);
    			event = newevent;
    		}
    	}
    	super.messageLogged(event);
    }
}
