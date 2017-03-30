package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.utility.Util;

public class FunctionDescription {
    private String lastUpdate;

    /**
     * Init FunctionDescription
     */
    public FunctionDescription(){
        setLastUpdateToNow();
    }

    /**
     * set last update to current date
     */
    public void setLastUpdateToNow (){
        lastUpdate = Util.getCurrentDate();
    }
}