package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

public class JVMContext {
    private static boolean context = false;

    public static boolean getContext(){
        return context;
    }

    public static void setServerContext(){
        context = true;
    }
}
