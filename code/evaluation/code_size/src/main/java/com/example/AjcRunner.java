package com.example;

import org.aspectj.tools.ajc.Main;

public class AjcRunner {
    public static void main(String[] args) throws Exception {
        String[] ajcArgs = {
                "-sourceroots", "E:\\OneDrive\\Uni\\17_FS\\Bachelorarbeit\\jcs_lambda\\code\\evaluation\\code_size\\src",
                "-source", "1.8",
                "-target", "1.8",
                "-outjar", "my_aspects.jar"
        };
        Main.main(ajcArgs);

        // C:\Users\StefanWürsten\.m2\repository\org\aspectj\aspectjrt\1.8.10\aspectjrt-1.8.10.jar
    }
}
