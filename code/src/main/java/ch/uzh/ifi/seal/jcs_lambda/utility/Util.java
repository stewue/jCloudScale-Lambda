package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.exception.CloudRuntimeException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
    /**
     * Get the full qualified name of the method
     * @param packageName package as (com.xy.demo)
     * @param className classname
     * @param methodName method name
     * @param parameters HashMap with all parameters
     * @return return the full qualified name
     */
    public static String getFullQualifiedName (String packageName, String className, String methodName, HashMap<String, Class> parameters ){
        String fullQualifiedName = packageName + "." + className + "." + methodName + "__";

        for( Map.Entry<String, Class> entry : parameters.entrySet()) {
            fullQualifiedName += "_" + entry.getValue().getSimpleName();
        }

        return fullQualifiedName;
    }

    /**
     * Get all parameters (name + type) of a method
     * @param method current method
     * @return hash-map with the parameter names and types
     */
    public static HashMap<String, Class> getMethodParameters( Method method ){
        HashMap<String, Class> parameters = new HashMap<>();

        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("") )
                .setScanners( new MethodParameterNamesScanner() );
        Reflections reflections = new Reflections( reflectionConfig );
        List<String> parameterNames = reflections.getMethodParamNames( method );

        Class [] parameterTypes = method.getParameterTypes();

        for( int i=0; i< parameterTypes.length; i++ ){
            String parameterName = parameterNames.get( i );
            Class parameterType = parameterTypes[ i ];

            parameters.put( parameterName, parameterType );
        }

        return parameters;
    }

    /**
     * do a http post request
     * @param url called url
     * @param urlParameters string what represent the body of the request
     * @return response body of the request
     */
    public static String doRequest ( String url, String urlParameters ){
        try {
            URL obj = new URL(url);
            HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();

            //add request header
            con.setRequestMethod("POST");

            // Send post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream( con.getOutputStream() );
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Check if error occurred in cloud
            if( con.getResponseCode() != 200 ){
                throw new CloudRuntimeException( "An error occurred in the cloud method '" + url + "'. Please check the log file from aws cloud watch" );
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        }
        catch ( Exception e ){
            e.printStackTrace();
            throw new RuntimeException( "Unable to create a request or interpret the response" );
        }
    }

    /**
     * Get current date and time in german format
     * @return return current date and time in german format
     */
    public static String getCurrentDate (){
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyyy");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
