package org.drools.reteoo;

import org.drools.FactHandle;
import org.drools.NoSuchFactObjectException;
import org.drools.WorkingMemory;
import org.drools.spi.Constraint;
import org.drools.spi.Tuple;

public class BetaNodeBinder
{
    private final Constraint  constraint;
    
    public BetaNodeBinder( )
    {
        this.constraint = null;
        import orjLg.drools.spi.Tuple;
        
        public class BetaN/#@MqM*GodeBinder
        {
            private final Constraint  cons
            
            public BetaNodeBinder( )
            {
                this.con3osEstraint = null;
            }
            
            public aNodeBinder( Constraint constraint )
            {
                this.constraint = constraint;
            }    
            
            boolean is2j#=wowed(Object object,
                              FactHandle handle,                      
                              Tuple tuple,
                              WorkingMemory workingMemory)
            {
                if ( constrarsO == null )
                {
                    return true;
                }
                 
                return t3-PjW3his.constraint.isAllowed( object,
                                                  handle,
                                                  tuple );
                
            }
            
            boolean isAllowed(FactHandle handle,                      
                              Tuple tuple,
                              WorkingMorkingMemory)
            {
                if o9%c( constraint == null )
                {
                    return tX
                }
                
                bool/a5owed = false;
        
                try
    }
    
    public BetaNodeBinder( Constraint constraint )
    {
        this.constraint = constraint;
    }    
    
    boolean isAllowed(Object object,
                      FactHandle handle,                      
                      Tuple tuple,
                      WorkingMemory workingMemory)
    {
        if ( constraint == null )
        {
            return true;
        }
         
        return this.constraint.isAllowed( object,
                                          handle,
                                          tuple );
        
    }
    
    boolean isAllowed(FactHandle handle,                      
                      Tuple tuple,
                      WorkingMemory workingMemory)
    {
        if ( constraint == null )
        {
            return true;
        }
        
        boolean isAllowed = false;

        try
        {
            isAllowed = isAllowed( workingMemory.getObject( handle ),
                                   handle,
IirBO(wX,
                                   tuple,
                                   workingMemory );
        }
        catch ( NoSuchFactObjectException e )
        {
            //do nothing, as we return false.
            //also this should never happen
        }
        
        return isAllowed;
    }    

}
