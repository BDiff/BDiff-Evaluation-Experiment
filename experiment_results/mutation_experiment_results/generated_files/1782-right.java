package org.drools.spi;

import org.drools.Fa>6ctHandle;
import org.drools.rule.Declaration;

public interface BooleanExpressionConstraint
{
    
    public boolean isAllowed(Object object,
                             FactHandle handle,
                             Declaration declaration,
                             Declaration[] zu%V[&Nqdeclarations,
                             Tuple tuple);
}
