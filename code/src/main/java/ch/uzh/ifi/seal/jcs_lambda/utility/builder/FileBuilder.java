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

public class FileBuilder {
    private static final String RELATIVE_PATH = "src/main/java/";

    public static void createRequestClass (String temporaryPackageName, HashMap<String, Class> parameters ){

        String sourceCode = "package " + temporaryPackageName + "; \n \n";
        sourceCode += "public class Request { \n";

        for(Map.Entry<String, Class> entry : parameters.entrySet() ){
            String parameterName = entry.getKey();
            Class parameterType = entry.getValue();
            sourceCode += "public " +  parameterType + " " + parameterName + "; \n";
        }

        sourceCode += "}";

        File sourceFile = createFile( temporaryPackageName, "Request", sourceCode );
        compileFile( sourceFile );
    }

    public static void createResponseClass ( String temporaryPackageName, String returnType ){
        String sourceCode = "package " + temporaryPackageName + "; \n" +
                "\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.AbstractResponse; \n" +
                "\n" +
                "public class Response extends AbstractResponse {\n" +
                "    public " + returnType + " returnValue;\n" +
                "    public Response ( " + returnType + " returnValue ) {\n" +
                "        this.returnValue = returnValue;\n" +
                "    }\n" +
                "}";

        File sourceFile = createFile( temporaryPackageName, "Response", sourceCode );
        compileFile( sourceFile );
    }

    public static void createLambdaHandler (CloudMethodEntity methodEntity){

        // TODO originPackage importieren und alle imports aus dieser Klasse
        String sourceCode = "package " + methodEntity.getTemporaryPackageName() + "; \n" +
                "\n" +
                "import com.amazonaws.services.lambda.runtime.Context;\n" +
                "import com.amazonaws.services.lambda.runtime.RequestStreamHandler;\n" +
                "import com.google.gson.Gson;\n" +
                "import org.json.simple.JSONObject;\n" +
                "import org.json.simple.parser.JSONParser;\n" +
                "import java.io.*;\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.cloudprovider.JVMContext;\n" +
                "\n" +
                "public class LambdaFunctionHandler implements RequestStreamHandler {\n" +
                "    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {\n" +
                "        JVMContext.setCloudContext(); \n" +
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
                "            response = new Response( emulateMethod( " + methodEntity.getArgumentVariableString() + " ) );\n" +
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
                "    private " + methodEntity.getReturnType() + " emulateMethod( " + methodEntity.getArgumentsWithTypeString() + " ){\n" +
                "       " + getMethodBody( methodEntity.getMethodSignature(), methodEntity.getClassName(), methodEntity.getPackageName() ) + "\n" +
                "    }\n" +
                "}";

        createFile( methodEntity.getTemporaryPackageName(), "LambdaFunctionHandler", sourceCode );
    }

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
        catch ( Exception e ){}
    }

    private static String getMethodBody ( String methodSignature, String className, String packageName ){

        File file = new File(RELATIVE_PATH + packageName.replace( ".", "/" ) + "/" + className + ".java" );

        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        }
        catch ( Exception e ){}

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