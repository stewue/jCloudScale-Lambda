package com.code_size;

import java.io.File;
import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class CreateFile {
    public static void main ( String [] args ) throws Exception {

        String save = "package com.code_size;\n" +
                "\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;\n" +
                "\n" +
                "public class Demo\n" +
                "{\n" +
                "    @StartUp\n" +
                "    public static void main ( String [] args ){\n" +
                "        Demo demo = new Demo();\n" +
                "    }\n" +
                "\n";

            int randomNum = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE );

            save += "    @CloudMethod\n" +
                    "    public String run" + randomNum +"(){\n";

            // TODO Code Missing
            for( int i=0; i<1024; i++){
                save += "ADD HERE RANDOM STRING WITH A SIZE OF 1MB";
            }

            save += "        return \"Run code in cloud\";\n" +
                    "    }\n";

        save += "}\n";

        PrintWriter out = new PrintWriter("src/main/java/com/code_size/Demo.java");
        out.println( save );
        out.close();
    }
}