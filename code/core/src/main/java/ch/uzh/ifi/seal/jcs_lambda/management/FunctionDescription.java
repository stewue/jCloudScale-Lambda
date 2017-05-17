package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.utility.Util;

public class FunctionDescription {
    private String lastUpdate;
    private String fullQualifiedName;

    /**
     * Init FunctionDescription
     * @param fullQualifiedName full name of the method
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
}