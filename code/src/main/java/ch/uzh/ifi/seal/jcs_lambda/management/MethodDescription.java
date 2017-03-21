package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.utility.Util;

public class MethodDescription {
    private String lastUpdate;
    private String checksum;

    /**
     * Init MethodDescription
     * @param checksum current checksum of the method
     */
    public MethodDescription ( String checksum ){
        this.checksum = checksum;

        setLastUpdateToNow();
    }

    /**
     * set last update to current date
     */
    public void setLastUpdateToNow (){
        lastUpdate = Util.getCurrentDate();
    }

    /**
     * Get checksum
     * @return checksum hash
     */
    public String getChecksum() {
        return checksum;
    }
}
