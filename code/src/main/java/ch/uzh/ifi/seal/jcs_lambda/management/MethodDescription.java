package ch.uzh.ifi.seal.jcs_lambda.management;

import ch.uzh.ifi.seal.jcs_lambda.utility.Util;

public class MethodDescription {
    private String lastUpdate;
    private String checksum;

    public MethodDescription ( String checksum ){
        this.checksum = checksum;

        setLastUpdateToNow();
    }

    public void setLastUpdate(String lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdateToNow (){
        lastUpdate = Util.getCurrentDate();
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public String getChecksum() {
        return checksum;
    }
}
