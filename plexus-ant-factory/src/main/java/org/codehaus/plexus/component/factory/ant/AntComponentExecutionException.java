package org.codehaus.plexus.component.factory.ant;

public class AntComponentExecutionException
    extends Exception
{

    static final long serialVersionUID = 1;

    private final String script;

    private final String target;

    private final String originalMessage;

    public AntComponentExecutionException( String script, String target, String message, Throwable cause )
    {
        super( "Executing Ant script: " + script + " [" + ( ( target == null ) ? ( "default-target" ) : ( target ) )
            + "]: " + message, cause );

        this.script = script;
        this.target = target;
        this.originalMessage = message;
    }

    public AntComponentExecutionException( String script, String target, String message )
    {
        super( "Executing Ant script: " + script + " [" + ( ( target == null ) ? ( "default-target" ) : ( target ) )
            + "]: " + message );

        this.script = script;
        this.target = target;
        this.originalMessage = message;
    }

    public final String getOriginalMessage()
    {
        return originalMessage;
    }

    public final String getScript()
    {
        return script;
    }

    public final String getTarget()
    {
        return target;
    }

}
