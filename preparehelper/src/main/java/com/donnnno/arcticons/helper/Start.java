package com.donnnno.arcticons.helper;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Start {

    public static void main(String[] args) {
        //String
        String rootDir = System.getProperty("user.dir");
        // Get the path of the root directory
        Path rootPath = Paths.get(rootDir);
        // Get the name of the root directory
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String sourceDir = rootDir + "/icons/white";
        String resDir;
        String destDir;
        String xmlDir = rootDir+"/app/src/main/res/xml";
        String generatedDir = rootDir+"/generated";
        String assetsDir = rootDir + "/app/src/main/assets";
        String appFilter = rootDir + "/newicons/appfilter.xml";
        String valuesDir = rootDir+"/app/src/main/res/values";

        if (args.length > 0) {
            String flavor = args[0];
            // Use the flavor as needed
            System.out.println("Processing with flavor: " + flavor);

            switch (flavor) {
                case "you" -> {
                    resDir = rootDir + "/app/src/you/res";
                    destDir = resDir + "/drawable-anydpi";
                    // Convert svg to drawable in runtime
                    SvgConverter.process(sourceDir, destDir, flavor);
                }
                case "black" -> {
                    if (false) {
                        resDir = rootDir + "/app/src/light/res";
                        destDir = resDir + "/drawable-anydpi";
                        // Convert svg to drawable in runtime
                        SvgConverter.process(sourceDir, destDir, flavor);
                    }
                }
                case "normal" -> {
                    if (false) {
                        resDir = rootDir + "/app/src/dark/res";
                        destDir = resDir + "/drawable-anydpi";
                        // Convert svg to drawable in runtime
                        SvgConverter.process(sourceDir, destDir, flavor);
                    }
                }
                case "dayNight" -> {
                    resDir = rootDir + "/app/src/dayNight/res";
                    destDir = resDir + "/drawable-anydpi";
                    // Convert svg to drawable in runtime
                    SvgConverter.process(sourceDir, destDir, flavor);
                }
            }
            System.out.println("SvgToVector task completed");

            // Read appfilter xml and create icon, drawable xml file.
            try {
                XMLCreator.mergeNewDrawables(valuesDir,generatedDir,assetsDir,sourceDir,xmlDir,appFilter);
                System.out.println("XML task completed");
            } catch (Exception e) {
                System.out.println("Error occurred: " + e.getMessage());
            }
        }
    }
}