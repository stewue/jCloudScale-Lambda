package ch.uzh.ifi.seal.jcs_lambda.utility.builder;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class CodeLastModified {
    /**
     * Check if code was modified since last time
     * @return result
     */
    public static boolean isModified(){
        long lastModified = getLastModified();

        long lastDeployed = 0;

        try {
            String content = Files.toString( new File("jcs_lambda/lastModified.txt"), Charsets.UTF_8 );
            content = content.replace("\n", "").replace("\r", "");
            lastDeployed = Long.valueOf( content );
        }
        catch ( Exception e ){
        }

        return lastDeployed < lastModified;
    }

    /**
     * set a new last modified date
     */
    public static void updateLastModified(){
        try{
            // create folder if not exists
            File file = new File("jcs_lambda/lastModified.txt");
            file.getParentFile().mkdirs();

            PrintWriter writer = new PrintWriter("jcs_lambda/lastModified.txt", "UTF-8" );
            writer.print( getLastModified() );
            writer.close();
        } catch (IOException e) {
            System.exit( -1 );
        }
    }

    /**
     * Get last modified of src folder
     * @return last modifed date as long
     */
    private static long getLastModified() {
        File directory = new File("src" );

        return getLastModifiedRecursively( directory.getAbsolutePath() );
    }

    /**
     * Get last modified of a path
     * @return last modifed date as long
     */
    private static long getLastModifiedRecursively ( String path ){
        File root = new File( path );
        File[] list = root.listFiles();

        // folder is empty
        if (list == null){
            return 0;
        }

        long highestLastModified = 0;

        for ( File file : list ) {
            long lastModified = 0;

            if ( file.isDirectory() ) {
                lastModified = getLastModifiedRecursively( file.getAbsolutePath() );
            }
            else {
                // check if its not a temporary file
                if( !file.getAbsoluteFile().toString().contains( ( CodeModifier.RELATIVE_PATH + CodeModifier.TEMPORARY_PACKAGE ).replace("/", "\\" ) ) ){
                    lastModified = file.lastModified();
                }
            }

            if( lastModified > highestLastModified ){
                highestLastModified = lastModified;
            }
        }

        return highestLastModified;
    }
}
