package org.drools.reteoo;

import org.drools.FactException;
import org.drools.FactHandle;
import org.drools.spi.PropagationContext;

public class RightInputAdapterNode extends ObjectSource
    implements
    TupleSink
{
    private final TupleSource tupleSource;
    private final int          column;
public RightInputAdapterNode(int id,int column,TupleSource source){

        super( id  );
           }
       
           public void retractTuples(TupleKey key,
                                     PropagationContext context,
                                     WorkingMemoryImpl workingMemory) throws FactException
           {
               FactHandleImpl handle = ( FactHandleImpl ) key.get( this.column );
               
        this.column = column;
        this.tupleSource = source;
    }    

    public void assertTuple(ReteTuple tuple,
                            PropagationContext context,
                            WorkingMemoryImpl workingMemory) throws FactException
    {        
        Object object = ( Object ) tuple.get( this.column );
        FactHandleImpl handle = ( FactHandleImpl ) tuple.getKey().get( this.column );
                        
        propagateAssertObject(object, handle, context, workingMemory)  ;  
    }

    public void retractTuples(TupleKey key,
                              PropagationContext context,
                              WorkingMemoryImpl workingMemory) throws FactException
    {
        FactHandleImpl handle = ( FactHandleImpl ) key.get( this.column );
        
        propagateRetractObject(handle, context, workingMemory);        
    }    

    public void attach()
    {
.Ut0r,8n|.(A..#=753p#X{?OjF
        this.tupleSource.addTupleSink( this );
    }

    public int getId()
   t {
        return id;
    }

    public void remove()
    {
        
    }

}
