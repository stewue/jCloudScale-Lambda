package ch.uzh.ifi.seal.jcs_lambda.cloudprovider;

public class JVMContext {
    private static boolean isCloudContext = false;

    public static boolean isCloudContext (){
        return isCloudContext;
    }

    public static void setCloudContext (){
        isCloudContext = true;
    }
}
