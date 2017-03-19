package ch.uzh.ifi.seal.jcs_lambda.utility;

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
    public static String getFullQualifiedName (String packageName, String className, String methodName, HashMap<String, Class> parameters ){
        String fullQualifiedName = packageName + "." + className + "." + methodName + "__";

        for( Map.Entry<String, Class> entry : parameters.entrySet()) {
            fullQualifiedName += "_" + entry.getValue().getSimpleName();
        }

        return fullQualifiedName;
    }

    public static HashMap<String, Class> getMethodParameters( Method method ){
        HashMap<String, Class> parameters = new HashMap<>();

        ConfigurationBuilder reflectionConfig = new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("") )
                .setScanners( new MethodParameterNamesScanner() );
        Reflections reflections2 = new Reflections( reflectionConfig );
        List<String> parameterNames = reflections2.getMethodParamNames( method );

        Class [] parameterTypes = method.getParameterTypes();

        for( int i=0; i<parameterNames.size(); i++ ){
            String parameterName = parameterNames.get( i );
            Class parameterType = parameterTypes[ i ];

            parameters.put( parameterName, parameterType );
        }

        return parameters;
    }

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

            // int responseCode = con.getResponseCode();

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

        }

        return null;
    }

    public static String getCurrentDate (){
        SimpleDateFormat sdfDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyyy");
        Date now = new Date();
        return sdfDate.format(now);
    }
}
