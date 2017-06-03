package com.numberOfFunctions;

import java.io.PrintWriter;
import java.util.concurrent.ThreadLocalRandom;

public class CreateFile {
    public static void main ( String [] args ) throws Exception {

        String save = "package com.example;\n" +
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

        for(int i=0; i<2; i++) {
            int randomNum = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE );

            save += "    @CloudMethod\n" +
                    "    public String run" + randomNum +"(){\n" +
                    "        return \"Run code in cloud\";\n" +
                    "    }\n";
        }

        save += "}\n";

            PrintWriter out = new PrintWriter("src/main/java/com/example/Demo.java");
            out.println( save );
            out.close();
    }
}
