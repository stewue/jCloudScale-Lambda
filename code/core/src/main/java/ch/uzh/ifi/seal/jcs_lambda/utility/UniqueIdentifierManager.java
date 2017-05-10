package ch.uzh.ifi.seal.jcs_lambda.utility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UniqueIdentifierManager {
    private static UniqueIdentifierManager instance;

    private Map<Object, String> registered = new HashMap<Object, String>();

    public static UniqueIdentifierManager getInstance(){
        if( instance == null ){
            instance = new UniqueIdentifierManager();
        }

        return instance;
    }

    public String getUuid( Object obj ){
        String key = registered.get( obj );

        if( key == null ){
            String uuid = UUID.randomUUID().toString();

            // add
            registered.put( obj, uuid );

            return uuid;
        }
        else{
            return key;
        }
    }
}
