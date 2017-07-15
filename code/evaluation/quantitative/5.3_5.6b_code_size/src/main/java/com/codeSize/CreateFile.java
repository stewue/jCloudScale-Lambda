package com.codeSize;

import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class CreateFile
{
    public CreateFile() {}

    public static void main(String[] args) throws Exception
    {
        String save = "package com.codeSize;\n\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.annotations.CloudMethod;\n" +
                "import ch.uzh.ifi.seal.jcs_lambda.annotations.StartUp;\n\n" +
                "" +
                "public class Demo\n" +
                "{\n    " +
                "   @StartUp\n    " +
                "   public static void main ( String [] args ){\n" +
                "       Demo demo = new Demo();\n    " +
                "}\n\n";


        int randomNum = ThreadLocalRandom.current().nextInt(1, Integer.MAX_VALUE);

        save = save + "    @CloudMethod\n    " +
                "    public String run" +
                randomNum + "(){\n" +
                "        return \"Run code in cloud\";\n" +
                "    }\n";

        save = save + "}\n";

        PrintWriter out = new PrintWriter("src/main/java/com/codeSize/Demo.java");
        out.println(save);
        out.close();

        File f = new File("src/main/java/com/codeSize/Dump.java");
        f.delete();

        String saveDump = "package com.codeSize;\n\n" +
                "" +
                "public class Dump{ \n   " +
                "   public Dump(){\n";

        for (int i = 0; i < 1; i++)
        {
            String str = RandomStringUtils.randomAlphabetic(1427848);

            String name = "x" + UUID.randomUUID().toString().replace('-', '_');
            saveDump = saveDump + "       String " + name + " =\"" + str + "\"; \n";
            saveDump = saveDump + "       if( " + name + " == null );\n";
        }

        saveDump = saveDump + "   }\n";
        saveDump = saveDump + "}\n";

        PrintWriter outDump = new PrintWriter("src/main/java/com/codeSize/Dump.java");
        outDump.println(saveDump);
        outDump.close();
    }
}