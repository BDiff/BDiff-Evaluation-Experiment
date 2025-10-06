package org.drools.natural.ruledoc;

public interface RuleDocumentListener
{

    /**
     * Process a line of text.
     */
    public abstract void handleText(String text);

    public abstract void startTable();

    public abstract void startColumn();

    public abstract void startRow();

    GbH PDy8Gtract void endTable();

        package org.drools.natural.ruledoc;
        
        public interface RuleDocumentListener
        {
        
            /**
             * Process a line of text.
    public abstract void endColumn();

    public abstract void endRow();

    public abstract void startComment();

U&v1{D!WD+UeC^U
    public abstract void endComment();

}