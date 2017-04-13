package ch.uzh.ifi.seal.jcs_lambda.utility;

import ch.uzh.ifi.seal.jcs_lambda.annotations.ReadOnly;
import ch.uzh.ifi.seal.jcs_lambda.exception.CloudRuntimeException;
import org.reflections.Reflections;
import org.reflections.scanners.MethodParameterNamesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
    /**
     * Get all class variables as map
     * @param method method object from that we need the variables
     * @return map with all variables
     */
    public static Map<String, Class> getClassVariables( Method method ){
        Map<String, Class> classVariables = new HashMap<>();

        Class clazz = method.getDeclaringClass();
        Field[] fields = clazz.getDeclaredFields();

        for( Field field : fields ){
            Annotation[] annotations = field.getAnnotations();

            for( Annotation annotation : annotations ){
                if( annotation.annotationType().equals( ReadOnly.class ) ){
                    String name = field.getName();
                    Class type = field.getType();

                    classVariables.put( name, type );
                }
            }
        }

        return classVariables;
    }

    /**
     * Get the full qualified name of the method
     * @param packageName package as (com.xy.demo)
     * @param className classname
     * @param methodName method name
     * @param parameters HashMap with all parameters
     * @return return the full qualified name
     */
    public static String getFullQualifiedName (String packageName, String className, String methodName, Map<String, Class> parameters ){
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

        Parameter [] methodParameters = method.getParameters();

        for( Parameter parameter : methodParameters ){
            String parameterName = parameter.getName();
            Class parameterType = parameter.getType();

            parameters.put( parameterName, parameterType );
        }


        return parameters;
    }

    /**
     *
     * @param method
     * @return
     */
    public static boolean isMethodParameterNamePresent( Method method ){
        Parameter [] methodParameters = method.getParameters();

        if( methodParameters.length == 0 ){
            return false;
        }
        else{
            return  methodParameters[0].isNamePresent();
        }
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
