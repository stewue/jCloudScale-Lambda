package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

import java.io.File;

public interface CloudProvider {
    /**
     * Upload a function to AWS and register it
     * @param functionName the name of the function that we create
     * @param handlerName the start point of the execution
     * @param file path to the file, that we would upload
     */
    void registerMethod ( String functionName, String handlerName, File file );
}
