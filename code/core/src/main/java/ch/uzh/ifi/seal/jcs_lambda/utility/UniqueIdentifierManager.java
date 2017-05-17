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

    /**
     * create for each object a unique id
     * @param obj context
     * @return uuid
     */
    public String getUuid( Object obj ){
        String key = registered.get( obj );

        // if object isn't registered, than create now id
        if( key == null ){
            String uuid = UUID.randomUUID().toString();

            // add
            registered.put( obj, uuid );

            return uuid;
        }
        // object is registered, than return id
        else{
            return key;
        }
    }
}
