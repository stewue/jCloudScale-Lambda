package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

import java.io.File;

public interface CloudProvider {
    void registerMethod ( String functionName, String handlerName, File file );
}
