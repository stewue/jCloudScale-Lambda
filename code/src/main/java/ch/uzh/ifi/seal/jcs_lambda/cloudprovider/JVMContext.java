package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

public class JVMContext {
    private static boolean context = false;
    private static String contextId;

    public static boolean getContext(){
        return context;
    }

    public static void setServerContext(){
        context = true;
    }

    public static void setContextId ( String id ){
        contextId = id;
    }

    public static String getContextId (){
        return contextId;
    }
}
