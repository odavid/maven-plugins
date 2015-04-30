package org.codehaus.plexus.component.factory.ant;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Main;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.helper.ProjectHelper2;
import org.codehaus.plexus.component.MapOrientedComponent;
import org.codehaus.plexus.component.configurator.ComponentConfigurationException;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;
import org.codehaus.plexus.component.repository.ComponentRequirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.IOUtil;
import org.slf4j.LoggerFactory;

import com.github.odavid.maven.ant.logger.MavenAntLogger;

public class AntScriptInvoker
    extends AbstractLogEnabled
    implements MapOrientedComponent
{

    public static final String BASEDIR_PARAMETER = "basedir";

    public static final String MESSAGE_LEVEL_PARAMETER = "messageLevel";

    private final ComponentDescriptor descriptor;

    private File script;

    private final String scriptResource;

    private String target;

    private Map references = new HashMap();

    private Properties properties = new Properties();

    private Project project;

    private File basedir;

    private String messageLevel;
    
    private final ClassLoader invokerClassLoader;
    private final String resourceName;

    public AntScriptInvoker( ComponentDescriptor descriptor, ClassLoader loader)
        throws IOException, ComponentInstantiationException
    {
    	this.invokerClassLoader = loader;
        this.descriptor = descriptor;

        String impl = descriptor.getImplementation();
        
        int colon = impl.indexOf( ":" );

        if ( colon > -1 ){
            this.resourceName = impl.substring( 0, colon );
            this.target = impl.substring( colon + 1 );
        }
        else{
        	this.resourceName = impl;
        }

        scriptResource = resourceName;
    }

	private void writeAntTempBuildScriptToDisk(File outDir) throws ComponentInstantiationException, IOException, FileNotFoundException {
		InputStream input = null;
        OutputStream output = null;
        try{
            input = invokerClassLoader.getResourceAsStream( resourceName );
            
            if ( input == null )
            {
                throw new ComponentInstantiationException( "Cannot find Ant script resource: '" + resourceName + "' in classpath of: " + invokerClassLoader);
            }

            if(outDir != null){
            	if(!outDir.isFile() && !outDir.exists()){
            		outDir.mkdirs();
            	}
            	//Keeping backward compatible - if cannot create output dir or is not a directory, keep working as before using temp file
            	if(!outDir.isDirectory()){
            		outDir = null;
            	}else{
            		script = new File(outDir, resourceName);
            	}
            }
            if(outDir == null){
	            script = File.createTempFile( "plexus-ant-component", ".build.xml" );
            }

            output = new FileOutputStream( script );
            script.deleteOnExit();

            IOUtil.copy( input, output );
        }finally{
            IOUtil.close( input );
            IOUtil.close( output );
        }
	}

    public static String[] getImplicitRequiredParameters()
    {
        return new String[] { BASEDIR_PARAMETER };
    }

    public static String[] getImplicitOptionalParameters()
    {
        return new String[] { MESSAGE_LEVEL_PARAMETER };
    }

    public void addComponentRequirement( ComponentRequirement rd, Object rv )
        throws ComponentConfigurationException
    {
        if ( !descriptor.getRequirements().contains( rd ) )
        {
            throw new ComponentConfigurationException( "Requirement: " + rd.getHumanReadableKey()
                + " is not listed in this component's descriptor." );
        }

        references.put( rd.getRole() + "_" + rd.getRoleHint(), rv );
    }

    public void setComponentConfiguration( Map componentConfiguration )
        throws ComponentConfigurationException
    {
        for ( Iterator it = componentConfiguration.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();
            
            Object key = entry.getKey();
            Object val = entry.getValue();
            
            if ( ( key instanceof String ) && ( val instanceof String ) )
            {
                properties.setProperty( (String) key, (String) val );
            }
            else
            {
                references.put( key, val );
            }
        }

        Object basedirInput = componentConfiguration.get( BASEDIR_PARAMETER );

        if ( basedirInput instanceof File )
        {
            this.basedir = (File) basedirInput;
        }
        else if ( basedirInput != null )
        {
            this.basedir = new File( String.valueOf( basedirInput ) );
        }
        else
        {
            throw new ComponentConfigurationException( "\'" + BASEDIR_PARAMETER + "\' parameter is missing." );
        }

        Object messageLevelInput = componentConfiguration.get( MESSAGE_LEVEL_PARAMETER );

        if ( messageLevelInput != null )
        {
            this.messageLevel = String.valueOf( messageLevelInput );
        }

        // ----------------------------------------------------------------------------
        // We need things like the basedir in order to initialize the ant project and
        // we need to initialize the project here so that it can be augmented with
        // classpath references and other properties before the ant task execution.
        // This is a little brittle as we're relying on a call for configuration
        // to signal ant project initialization ... jvz.
        // ----------------------------------------------------------------------------
    	try {
			writeAntTempBuildScriptToDisk(basedir != null ? new File(basedir, "target/.ant-mojo"): null);
		} catch (ComponentInstantiationException | IOException e) {
			throw new ComponentConfigurationException("Cannot write Ant Temporary file to disk", e);
		}
        initializeProject();
    }

    public void invoke()
        throws AntComponentExecutionException
    {
//        InputStream oldSysIn = System.in;
//        PrintStream oldSysOut = System.out;
//        PrintStream oldSysErr = System.err;

        try
        {
//            project.setDefaultInputStream( System.in );
//
//            System.setIn( new DemuxInputStream( project ) );
//            System.setOut( new PrintStream( new DemuxOutputStream( project, false ) ) );
//            System.setErr( new PrintStream( new DemuxOutputStream( project, true ) ) );

        	org.slf4j.Logger slf4JLogger = LoggerFactory.getLogger("ant-script-invoker") ;
            DefaultLogger antLogger = new MavenAntLogger(project, slf4JLogger);

            int level = convertMsgLevel( messageLevel );

            Logger logger = getLogger();
            if ( logger != null )
            {
                logger.debug( "Ant message level is set to: " + messageLevel + "(" + level + ")" );
            }

            antLogger.setMessageOutputLevel( level );

            project.addBuildListener( antLogger );
        	
            project.fireBuildStarted();

            Throwable error = null;

            try
            {
                try
                {
                    ProjectHelper helper = new ProjectHelper2();

                    project.addReference( "ant.projectHelper", helper );

                    helper.parse( project, script );
                }
                catch ( BuildException ex )
                {
                    error = ex;
                    throw new AntComponentExecutionException( scriptResource, target, "Failed to parse.", ex );
                }

                try
                {
                    project.executeTarget( target );
                }
                catch ( BuildException e )
                {
                    error = e;
                    throw new AntComponentExecutionException( scriptResource, target, "Failed to execute.", e );
                }
            }
            finally
            {
                project.fireSubBuildFinished( error );
            }

        }
        finally
        {
            // help the gc
            project = null;

//            System.setIn( oldSysIn );
//            System.setOut( oldSysOut );
//            System.setErr( oldSysErr );
        }
    }

    private void initializeProject()
    {
        this.project = new Project();

        project.init();
        project.setUserProperty( "ant.version", Main.getAntVersion() );
        project.setProperty( "ant.file", script.toString() );

        project.setBaseDir( basedir );
        
        for ( Iterator it = references.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();

            String key = (String) entry.getKey();

            project.addReference( key, entry.getValue() );
        }
        
        for ( Iterator it = properties.entrySet().iterator(); it.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) it.next();
            
            String key = (String) entry.getKey();
            
            project.setUserProperty( key, properties.getProperty( key ) );
        }
        
    }

    protected int convertMsgLevel( String msgLevel )
    {
        int level;

        if ( msgLevel == null )
        {
            return Project.MSG_ERR;
        }

        msgLevel = msgLevel.toLowerCase();

        if ( msgLevel.equals( "error" ) )
        {
            level = Project.MSG_ERR;
        }
        else if ( msgLevel.equals( "warning" ) || msgLevel.equals( "warn" ) )
        {
            level = Project.MSG_WARN;
        }
        else if ( msgLevel.equals( "information" ) || msgLevel.equals( "info" ) )
        {
            level = Project.MSG_INFO;
        }
        else if ( msgLevel.equals( "debug" ) )
        {
            level = Project.MSG_DEBUG;
        }
        else if ( msgLevel.equals( "verbose" ) )
        {
            level = Project.MSG_VERBOSE;
        }
        else
        {
            Logger logger = getLogger();
            if ( logger != null )
            {
                logger.info( "Unknown Ant Message Level (" + msgLevel + ") -- using \"error\" instead" );
            }

            level = Project.MSG_ERR;
        }

        return level;
    }

    public Project getProject()
    {
        return project;
    }

    public ComponentDescriptor getDescriptor()
    {
        return descriptor;
    }

    public File getScript()
    {
        return script;
    }

    public String getScriptResource()
    {
        return scriptResource;
    }

    public String getTarget()
    {
        return target;
    }

    public Map getReferences()
    {
        return references;
    }

    public Properties getProperties()
    {
        return properties;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public String getMessageLevel()
    {
        return messageLevel;
    }
}
