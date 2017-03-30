package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.utility.Util;

public class FunctionDescription {
    private String lastUpdate;
    private String fullQualifiedName;

    /**
     * Init FunctionDescription
     */
    public FunctionDescription( String fullQualifiedName ){
        setLastUpdateToNow();

        this.fullQualifiedName = fullQualifiedName;
    }

    /**
     * set last update to current date
     */
    public void setLastUpdateToNow (){
        lastUpdate = Util.getCurrentDate();
    }

    public String getFullQualifiedName () {
        return fullQualifiedName;
    }
}