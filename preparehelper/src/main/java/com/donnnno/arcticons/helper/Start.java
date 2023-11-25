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
        if (rootDirName.equals("preparehelper")){
            rootDir = "..";
        }
        String sourceDir = rootDir + "/icons/white";
        String resDir;
        String destDir;
        System.out.println("root Dir: " + rootPath);
        System.out.println("root Dir Name: " + rootDirName);
        if (args.length > 0) {
            String flavor = args[0];
            // Use the flavor as needed
            System.out.println("Processing with flavor: " + flavor);
            //String appFilterFile = rootDir + "/app/assets/appfilter.xml";
            switch (flavor) {
                case "you":
                    resDir = rootDir + "/app/src/you/res";
                    destDir = resDir + "/drawable-anydpi";
                    // Convert svg to drawable in runtime
                    SvgConverter.process(sourceDir, destDir, flavor);
                    break;

                case "light":
                    if (false) {
                        resDir = rootDir + "/app/src/light/res";
                        destDir = resDir + "/drawable-anydpi";
                        // Convert svg to drawable in runtime
                        SvgConverter.process(sourceDir, destDir, flavor);
                    }
                    break;

                case "dark":
                    if (false) {
                        resDir = rootDir + "/app/src/dark/res";
                        destDir = resDir + "/drawable-anydpi";
                        // Convert svg to drawable in runtime
                        SvgConverter.process(sourceDir, destDir, flavor);
                    }
                    break;
                case "dayNight":
                        resDir = rootDir + "/app/src/dayNight/res";
                        destDir = resDir + "/drawable-anydpi";
                        // Convert svg to drawable in runtime
                        SvgConverter.process(sourceDir, destDir, flavor);
                    break;
            }
            System.out.println("SvgToVector task completed");
            // Read appfilter xml and create icon, drawable xml file.
                /* try {
            ConfigProcessor.loadAndCreateConfigs(appFilterFile, resDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        }
    }
}