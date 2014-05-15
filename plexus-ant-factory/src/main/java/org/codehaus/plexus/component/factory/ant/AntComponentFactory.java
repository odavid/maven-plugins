package org.codehaus.plexus.component.factory.ant;

import org.codehaus.classworlds.ClassRealm;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.factory.AbstractComponentFactory;
import org.codehaus.plexus.component.factory.ComponentInstantiationException;
import org.codehaus.plexus.component.repository.ComponentDescriptor;

import java.io.IOException;

public class AntComponentFactory
    extends AbstractComponentFactory
{

    public Object newInstance( ComponentDescriptor componentDescriptor, ClassRealm classRealm, PlexusContainer container )
        throws ComponentInstantiationException
    {
        try
        {
            return new AntScriptInvoker( componentDescriptor, new RealmDelegatingClassLoader( classRealm ));
        }
        catch ( IOException e )
        {
            throw new ComponentInstantiationException( "Failed to extract Ant script for: " + componentDescriptor.getHumanReadableKey(), e );
        }
    }

    public String getId()
    {
        return "ant";
    }

}
