package ch.uzh.ifi.seal.jcs_lambda.utility;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;

public class UtilFileLoader {
    private static final String RELATIVE_PATH = "src/main/java/";
    private static final String TMP_PACKAGE = "tmp_jcs";

    public static void createTmpFiles( ProceedingJoinPoint joinPoint){

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        String methodName = signature.getMethod().getName();
        String className = signature.getMethod().getDeclaringClass().getSimpleName();
        String originPackageName = signature.getMethod().getDeclaringClass().getPackage().getName();
        String newPackageName = originPackageName + "." + className + "." + methodName; //TODO overloading

        Class[] parameterTypes = signature.getParameterTypes();
        String [] parameterNames = signature.getParameterNames();
        String returnType = signature.getReturnType().getName();

        String methodSignature = methodName + " ( ";
        for( int i=0; i<parameterTypes.length; i++ ){
            Class type = parameterTypes[i];
            Object argumentName = parameterNames[i];

            if( i>0 ){
                methodSignature += ", ";
            }

            methodSignature += type + " " + argumentName;
        }
        methodSignature += " )";

        createRequestClass( newPackageName, parameterTypes, parameterNames );
        createResponseClass( newPackageName, returnType );
        createLambdaHandler( methodSignature, className, newPackageName, parameterTypes, parameterNames, returnType, originPackageName );
    }

    private static void createRequestClass ( String packageName, Class[] parameterTypes, String[] parameterNames ){

        String sourceCode = "package " + TMP_PACKAGE + "." + packageName + "; \n \n";
        sourceCode += "public class Request { \n";

        for( int i = 0; i < parameterTypes.length; i++ ){
            sourceCode += "public " +  parameterTypes[i] + " " + parameterNames[i] + "; \n";
        }

        sourceCode += "}";

        createFile( packageName, "Request", sourceCode );
    }

    private static void createResponseClass ( String packageName, String returnType ){
        String sourceCode = "package " + TMP_PACKAGE + "." + packageName + "; \n" +
                "\n" +
                "public class Response {\n" +
                "    public " + returnType + " returnValue;\n" +
                "    public Response ( " + returnType + " returnValue ) {\n" +
                "        this.returnValue = returnValue;\n" +
                "    }\n" +
                "}";

        createFile( packageName, "Response", sourceCode );
    }

    private static void createLambdaHandler ( String methodSignature, String className, String packageName, Class[] parameterTypes, String[] parameterNames, String returnType, String originPackageName ){
        String arguments = "";
        String argumentsWithType = "";

        for( int i=0; i<parameterTypes.length; i++ ){
            Class type = parameterTypes[i];
            Object argumentName = parameterNames[i];

            if( i>0 ){
                arguments += ", ";
                argumentsWithType += ", ";
            }

            arguments += "request." + argumentName;
            argumentsWithType += type + " " + argumentName;
        }

        String sourceCode = "package " + TMP_PACKAGE + "." + packageName + "; \n" +
                "\n" +
                "import com.amazonaws.services.lambda.runtime.Context;\n" +
                "import com.amazonaws.services.lambda.runtime.RequestStreamHandler;\n" +
                "import com.google.gson.Gson;\n" +
                "import org.json.simple.JSONObject;\n" +
                "import org.json.simple.parser.JSONParser;\n" +
                "import java.io.*;\n" +
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
                "            Request request = gson.fromJson( event.toJSONString() , Request.class );\n" +
                "\n" +
                "            response = new Response( emulateMethod( " + arguments + " ) );\n" +
                "        }\n" +
                "        catch(Exception ex) {\n" +
                "            System.out.println( ex );\n" +
                "        }\n" +
                "\n" +
                "        OutputStreamWriter writer = new OutputStreamWriter(outputStream, \"UTF-8\");\n" +
                "        writer.write( gson.toJson(response) );\n" +
                "        writer.close();\n" +
                "    }\n" +
                "\n" +
                "    private " + returnType + " emulateMethod( " + argumentsWithType + " ){\n" +
                "       " + getMethodBody( methodSignature, className, originPackageName ) + "\n" +
                "    }\n" +
                "}";

        createFile( packageName, "LambdaFunctionHandler", sourceCode );
    }

    private static void createFile ( String packageName, String className, String sourceCode ){
        // create the source
        File sourceFile   = new File( RELATIVE_PATH + TMP_PACKAGE + "/" + packageName.replace( ".", "/" ) + "/" + className + ".java" );
        sourceFile.getParentFile().mkdirs();

        try {
            FileWriter writer = new FileWriter(sourceFile);
            writer.write(sourceCode);
            writer.close();
        }
        catch ( Exception e ){}
/*
        JavaCompiler compiler    = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager =
                compiler.getStandardFileManager(null, null, null);

        fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
                Arrays.asList(new File("target/classes")));
        // Compile the file
        compiler.getTask(null,
                fileManager,
                null,
                null,
                null,
                fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile)))
                .call();
        fileManager.close();*/
    }

    private static String getMethodBody ( String methodSignature, String className, String packageName ){

        File file = new File(RELATIVE_PATH + packageName.replace( ".", "/" ) + "/" + className + ".java" );

        Scanner scanner = null;

        try {
            scanner = new Scanner(file);
        }
        catch ( Exception e ){}

        String source = "";
        while(scanner.hasNext()) {
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

        // trimming all text b4 method signature
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
