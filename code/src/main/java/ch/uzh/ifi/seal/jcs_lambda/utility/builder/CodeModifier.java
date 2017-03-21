package ch.uzh.ifi.seal.jcs_lambda.utility.builder;

import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CodeModifier {
    private static final String RELATIVE_PATH = "src/main/java/";

    /**
     * create a request dto class
     * @param temporaryPackageName package name of the new, temporary created package
     * @param parameters hash-map with the parameters of the origin method
     */
    public static void createRequestClass (String temporaryPackageName, HashMap<String, Class> parameters ){

        String sourceCode = "package " + temporaryPackageName + "; \n \n";
        sourceCode += "public class Request { \n";

        // Generate all attributes
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();
            Class parameterType = entry.getValue();
            sourceCode += "public " +  parameterType + " " + parameterName + "; \n";
        }

        // generate getter for all attributes
        sourceCode += "" +
            "public Object [] getObjectArray() { \n" +
            "       Object [] ret = {";

        int i = 0;
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();

            if( i>0){
                sourceCode += ", ";
            }
            sourceCode += parameterName;
            i++;
        }

        sourceCode += "" +
            "       }; \n" +
            "       return ret; \n" +
            "   } \n" +
            " \n" +

        // generate getter for all types
            "   public Class [] getClassArray() { \n" +
            "       Class [] ret = {";

        int j = 0;
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            Class parameterType = entry.getValue();

            if( j>0){
                sourceCode += ", ";
            }
            sourceCode += parameterType + ".class";
            j++;
        }

        sourceCode += "" +
            "       }; \n" +
            "       return ret; \n" +
            "   } \n" +
            "}";

        File sourceFile = createFile( temporaryPackageName, "Request", sourceCode );
        compileFile( sourceFile );
    }

    /**
     * create a response dto class
     * @param temporaryPackageName package name of the new, temporary created package
     * @param returnType return type of the origin method
     */
    public static void createResponseClass ( String temporaryPackageName, String returnType ){
        String sourceCode = "package " + temporaryPackageName + "; \n" +
                "\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AbstractResponse; \n" +
                "\n" +
                "public class Response extends AbstractResponse {\n" +
                "    public " + returnType + " returnValue;\n" +
                "    public Response ( Object returnValue ) {\n" +
                "        this.returnValue = (" + returnType + ") returnValue;\n" +
                "    }\n" +
                "}";

        File sourceFile = createFile( temporaryPackageName, "Response", sourceCode );
        compileFile( sourceFile );
    }

    /**
     * create a new lambda handler for aws
     * @param methodEntity current method
     */
    public static void createLambdaHandler ( CloudMethodEntity methodEntity ){

        // TODO originPackage importieren und alle imports aus dieser Klasse
        String sourceCode = "package " + methodEntity.getTemporaryPackageName() + "; \n" +
                "\n" +
                "import com.amazonaws.services.lambda.runtime.Context;\n" +
                "import com.amazonaws.services.lambda.runtime.RequestStreamHandler;\n" +
                "import com.google.gson.Gson;\n" +
                "import org.json.simple.JSONObject;\n" +
                "import org.json.simple.parser.JSONParser;\n" +
                "import java.io.*;\n" +
                "import " + methodEntity.getPackageName() + ".*; \n" +


                "import java.lang.reflect.Method; \n" +

                "\n" +
                "public class LambdaFunctionHandler implements RequestStreamHandler {\n" +
                "    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {\n" +
                "        JSONParser parser = new JSONParser();\n" +
                "        Gson gson = new Gson();\n" +
                "\n" +
                "        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));\n" +
                "        Response response = null;\n" +
                "\n" +
                "        try {\n" +
                "            JSONObject event = (JSONObject) parser.parse(reader);\n" +
                "            System.out.println( \"Input: \" +  event.toJSONString() );\n" +
                "            Request request = gson.fromJson( event.toJSONString() , Request.class );\n" +
                "\n" +
                // TODO not hard-coded
                "            Calculator object = new Calculator(); \n" +
                "            Class params[] = request.getClassArray(); \n" +
                "            Object paramsObj[] = request.getObjectArray(); \n \n" +
                "            Method method = object.getClass().getDeclaredMethod(\"" + methodEntity.getMethodName() + "\", params ); \n" +
                "            method.setAccessible(true); \n" +
                "            response = new Response( method.invoke( object, paramsObj ) ); \n" +
                "        }\n" +
                "        catch(Exception ex) {\n" +
                "            System.out.println( ex );\n" +
                "        }\n" +
                "\n" +
                "        OutputStreamWriter writer = new OutputStreamWriter(outputStream, \"UTF-8\");\n" +
                "        writer.write( gson.toJson(response) );\n" +
                "        writer.close();\n" +
                "        \n" +
                "        System.out.println( \"Output: \" +  gson.toJson(response) );\n" +
                "    }\n" +
                "\n" +
                "}";

        createFile( methodEntity.getTemporaryPackageName(), "LambdaFunctionHandler", sourceCode );
    }

    /**
     * create a new file in the src folder
     * @param temporaryPackageName package name of the new, temporary created package
     * @param className class name
     * @param sourceCode source code for the new file
     * @return return created File
     */
    private static File createFile (String temporaryPackageName, String className, String sourceCode ){
        // create the source
        File sourceFile = new File( RELATIVE_PATH + temporaryPackageName.replace( ".", "/" ) + "/" + className + ".java" );
        sourceFile.getParentFile().mkdirs();

        try {
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(sourceCode);
            writer.close();
        }
        catch ( Exception e ){}

        return sourceFile;
    }

    /**
     * compile a file on runtime and add it on runtime to the application
     * @param sourceFile path in java environment
     */
    private static void compileFile ( File sourceFile ){
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

            fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(new File("target/classes")));

            // Compile the file
            compiler.getTask(null,
                    fileManager,
                    null,
                    null,
                    null,
                    fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile)))
                    .call();
            fileManager.close();
        }
        catch ( Exception e ){

        }
    }

    /**
     * return a string of the method body
     * @param methodSignature method name with signature
     * @param className class name
     * @param packageName package name (com.xyz.demo)
     * @return return a string with the whole method body as source code
     */
    public static String getMethodBody ( String methodSignature, String className, String packageName ){
        //TODO refacroting with better idea
        /*
        String[] relative_path_TMP = { "src/main/java/", "src/test/java/" };

        File file;
        Scanner scanner = null;

        for( String path : relative_path_TMP ){
            file = new File(path + packageName.replace( ".", "/" ) + "/" + className + ".java" );

            try {
                scanner = new Scanner(file);
            }
            catch ( Exception e ){

            }

            if( scanner != null ){
                break;
            }
        }*/

        File file = new File(RELATIVE_PATH + packageName.replace( ".", "/" ) + "/" + className + ".java" );

        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        }
        catch ( Exception e ){

        }

        // No source code founded
        if( scanner == null){
            return null;
        }


        String source = "";
        while( scanner.hasNext() ) {
            source += " "+ scanner.next();
        }

        // extract code using the method signature
        methodSignature = methodSignature.trim();
        source = source.trim();

        //appending { to differentiate from argument as it can be matched also if in the same file
        methodSignature = methodSignature+"{";

        //making sure we find what we are looking for
        methodSignature = methodSignature.replaceAll("\\s*[(]\\s*", "(");
        methodSignature = methodSignature.replaceAll("\\s*[)]\\s*", ")");
        methodSignature = methodSignature.replaceAll("\\s*[,]\\s*", ",");
        methodSignature = methodSignature.replaceAll("\\s+", " ");


        source =source.replaceAll("\\s*[(]\\s*", "(");
        source = source.replaceAll("\\s*[)]\\s*", ")");
        source = source.replaceAll("\\s*[,]\\s*", ",");
        source = source.replaceAll("\\s+", " ");


        if(!source.contains(methodSignature)) return null;

        // trimming all text in method signature
        source = source.substring(source.indexOf(methodSignature) );

        //getting last index, a methods ends when there are matching pairs of these {}
        int lastIndex = 0;

        int rightBraceCount = 0;
        int leftBraceCount = 0;

        char [] remainingSource = source.toCharArray();
        for (int i = 0; i < remainingSource.length ; i++) {
            if(remainingSource[i] == '}'){
                rightBraceCount++;
                if(rightBraceCount == leftBraceCount){
                    lastIndex = i;
                    break;
                }
            }
            else if(remainingSource[i] == '{'){
                leftBraceCount++;
            }
        }

        return source.substring( methodSignature.length(), lastIndex );
    }
}