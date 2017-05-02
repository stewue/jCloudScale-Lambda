package ch.uzh.ifi.seal.jcs_lambda.utility.builder;

import ch.uzh.ifi.seal.jcs_lambda.logging.Logger;
import ch.uzh.ifi.seal.jcs_lambda.management.CloudMethodEntity;
import org.codehaus.plexus.util.FileUtils;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.Map;

public class CodeModifier {
    public static final String RELATIVE_PATH = "src/test/java/";
    public static final String TEMPORARY_PACKAGE = "tmp_jcs";

    /**
     * create a request dto class
     * @param temporaryPackageName package name of the new, temporary created package
     * @param parameters hash-map with the parameters of the origin method
     * @param classVariables hash-map with all class variables, that aren't local
     */
    public static void createRequestClass (String temporaryPackageName, Map<String, Class> parameters, Map<String, Class> classVariables ){

        String sourceCode = "package " + temporaryPackageName + "; \n \n";
        sourceCode += "public class Request { \n"
                    + " public String _uuid_; \n";

        // Generate all attributes
        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();
            String parameterType = entry.getValue().getName();
            sourceCode += "public " +  parameterType + " " + parameterName + "; \n";
        }

        // Generate class variables
        sourceCode += "/* class variables */ \n";
        for(Map.Entry<String, Class> entry : classVariables.entrySet() ){
            String classVariableName = entry.getKey();
            String classVarianleType = entry.getValue().getName();

            // only add class variable to dto if not already a parameter with the same name exists
            if( parameters.get( classVariableName ) == null ){
                sourceCode += "public " +  classVarianleType + " " + classVariableName + "; \n";
            }
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
            String parameterType = entry.getValue().getName();

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
                "public class Response {\n" +
                "    public " + returnType + " returnValue;\n" +
                "    public StackTraceElement [] exceptionStackTrace; \n" +
                "    public Response ( Object returnValue ) {\n" +
                "        this.returnValue = (" + returnType + ") returnValue;\n" +
                "    }\n" +
                "    public Response ( StackTraceElement [] exceptionStackTrace ) {\n" +
                "        this.exceptionStackTrace = exceptionStackTrace;\n" +
                "    }\n" +
                "}";

        File sourceFile = createFile( temporaryPackageName, "Response", sourceCode );
        compileFile( sourceFile );
    }

    /**
     * create a new lambda handler for aws
     * @param methodEntity current method
     */
    public static void createLambdaFunctionHandler ( CloudMethodEntity methodEntity ){

        String sourceCode = "package " + methodEntity.getTemporaryPackageName() + "; \n" +
                "\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext; \n" +
                "import ch.uzh.ifi.seal.jcs_lambda.logging.Logger; \n" +
                "import com.amazonaws.services.lambda.runtime.Context;\n" +
                "import com.amazonaws.services.lambda.runtime.RequestStreamHandler;\n" +
                "import com.google.gson.Gson;\n" +
                "import org.json.simple.JSONObject;\n" +
                "import org.json.simple.parser.JSONParser;\n" +
                "import java.io.*;\n" +
                "import " + methodEntity.getPackageName() + ".*; \n" +


                "import java.lang.reflect.Method; \n" +
                "import java.lang.reflect.Field; \n" +

                "\n" +
                "public class Endpoint implements RequestStreamHandler {\n" +
                "    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {\n" +
                "        JVMContext.setServerContext(); \n" +
                "        JSONParser parser = new JSONParser();\n" +
                "        Gson gson = new Gson();\n" +
                "\n" +
                "        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));\n" +
                "        Response response = null;\n" +
                "\n" +
                "        try {\n" +
                "            JSONObject event = (JSONObject) parser.parse(reader);\n" +
                "            Logger.info( \"Input: \" +  event.toJSONString() );\n" +
                "            Request request = gson.fromJson( event.toJSONString() , Request.class );\n" +
                "\n" +
                "            response = invoke( request );\n" +
                "        }\n" +
                "        catch(Exception ex) {\n" +
                "            Logger.info( ex.getMessage() );\n" +
                "            ex.printStackTrace(); \n" +
                "            response = new Response( ex.getStackTrace() ); \n" +
                "        }\n" +
                "\n" +
                "        OutputStreamWriter writer = new OutputStreamWriter(outputStream, \"UTF-8\");\n" +
                "        writer.write( gson.toJson(response) );\n" +
                "        writer.close();\n" +
                "        \n" +
                "        Logger.info( \"Output: \" +  gson.toJson(response) );\n" +
                "    }\n" +
                "\n" +
                "    private Response invoke( Request request ) throws Exception {\n" +
                "        JVMContext.setContextId( request._uuid_ ); \n" +
                "        " + methodEntity.getClassName() + " object = new " + methodEntity.getClassName() + "(); \n" +
                "        Class params[] = request.getClassArray(); \n" +
                "        Object paramsObj[] = request.getObjectArray(); \n" +
                "\n" +
                "        Method method = object.getClass().getDeclaredMethod(\"" + methodEntity.getMethodName() + "\", params ); \n" +
                "        method.setAccessible(true); \n" +
                "\n" +
                "        Field field; \n" +
                "\n";

                for(Map.Entry<String, Class> entry : methodEntity.getClassVariables().entrySet() ){
                    String name = entry.getKey();
                    sourceCode +=
                            "        field = object.getClass().getDeclaredField(\""+ name + "\" ); \n" +
                            "        field.setAccessible(true); \n" +
                            "        field.set( object, request." + name + " ); \n" +
                            "\n";
                }

                // handle void return type
                if( methodEntity.isReturnTypeVoid() ){
                    sourceCode +=
                            "        method.invoke( object, paramsObj ); \n" +
                            "        return new Response( \"\" ); \n";
                }
                else {
                    sourceCode +=
                        "        return new Response( method.invoke( object, paramsObj ) ); \n";
                }

                sourceCode +=
                "    }\n" +
                "\n" +
                "}";

        createFile( methodEntity.getTemporaryPackageName(), "Endpoint", sourceCode );
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
     * Remove temporary created classes (dto and lambda handler)
     */
    public static void removeTemporaryClasses(){
        File directory = new File( RELATIVE_PATH + TEMPORARY_PACKAGE );

        try{
            FileUtils.deleteDirectory( directory );
        }
        catch ( Exception e ){
            Logger.error( "Unable to remove temporary created files" );
        }
    }
}