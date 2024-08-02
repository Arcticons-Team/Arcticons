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
        String xmlDir;
        String newXML;
        String categoryGamesXml;
        String assetsDir;
        String appFilter;
        //System.out.println("root Dir: " + rootPath);
        //System.out.println("root Dir Name: " + rootDirName);
        if (args.length > 0) {
            String flavor = args[0];
            // Use the flavor as needed
            System.out.println("Processing with flavor: " + flavor);
            //String appFilterFile = rootDir + "/app/assets/appfilter.xml";
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


            xmlDir =rootDir+"/app/src/main/res/xml";
            newXML = rootDir+"/generated/newdrawables.xml";
            categoryGamesXml = rootDir+"/generated/games.xml";
            assetsDir = rootDir + "/app/src/main/assets";
            appFilter = rootDir + "/newicons/appfilter.xml";



            try {
                XMLCreator.mergeNewDrawables(xmlDir+"/drawable.xml",newXML,categoryGamesXml,assetsDir,sourceDir,xmlDir,appFilter);
                System.out.println("XML task completed");
            } catch (Exception e) {
                System.out.println("Error occurred: " + e.getMessage());
            }
        }
    }
}