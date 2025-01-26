package com.donnnno.arcticons.helper;

import static com.donnnno.arcticons.helper.Changelog.generateChangelogs;
import static com.donnnno.arcticons.helper.Checks.startChecks;
import static com.donnnno.arcticons.helper.ImageCollageGenerator.generateReleaseImage;
import static com.donnnno.arcticons.helper.NewDrawableXmlCreator.createNewDrawables;
import static com.donnnno.arcticons.helper.SortAppfilter.sortXML;
import static com.donnnno.arcticons.helper.WebpCreator.createWebpIcons;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PrepareRelease {

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            //String
            String rootDir = System.getProperty("user.dir");
            // Get the path of the root directory
            Path rootPath = Paths.get(rootDir);
            // Get the name of the root directory
            String rootDirName = rootPath.getFileName().toString();
            if (rootDirName.equals("preparehelper")) {
                rootDir = "..";
            }
            String xmlDir;
            String assetsDir;
            xmlDir = rootDir + "/app/src/main/res/xml";
            assetsDir = rootDir + "/app/src/main/assets";
            String contributorsXml = rootDir + "/generated/contributors.xml";
            String generatedDir = rootDir + "/generated";
            String valuesDir = rootDir + "/app/src/main/res/values";
            String appFilter = rootDir + "/newicons/appfilter.xml";
            String newIconsDir = rootDir + "/newicons";
            String drawableXml = xmlDir + "/drawable.xml";
            String changelogXml = valuesDir +"/changelog.xml";
            String sourceDir = rootDir + "/icons/white";
            String blackDir = rootDir + "/icons/black";
            String exportWhiteDir = rootDir + "/app/src/normal/res/drawable-nodpi";
            String exportBlackDir = rootDir + "/app/src/black/res/drawable-nodpi";

            String task = args[0];
            System.out.println("Processing with task: " + task);
            switch (task) {
                case "checkonly":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    break;
                case "release":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    createNewDrawables(newIconsDir, generatedDir+"/newDrawables.xml", false);
                    createWebpIcons(newIconsDir,sourceDir, blackDir, exportWhiteDir, exportBlackDir);
                    sortXML(appFilter);
                    try {
                        ContributorImage.start(assetsDir, contributorsXml, xmlDir);
                        System.out.println("Contributor Image task completed");
                    } catch (Exception e) {
                        System.out.println("Error occurred: " + e.getMessage());
                    }
                    try {
                        XMLCreator.mergeNewDrawables(valuesDir,generatedDir,assetsDir,sourceDir,xmlDir,appFilter);
                        System.out.println("XML task completed");
                    } catch (Exception e) {
                        System.out.println("Error occurred: " + e.getMessage());
                    }
                    generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,rootDir,false);
                    generateReleaseImage( generatedDir + "/newdrawables.xml", sourceDir, generatedDir + "/releaseImage.webp");
                    break;
                case "newrelease":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    createNewDrawables(newIconsDir, generatedDir+"/newDrawables.xml", true);
                    createWebpIcons(newIconsDir,sourceDir, blackDir, exportWhiteDir, exportBlackDir);
                    sortXML(appFilter);
                    try {
                        ContributorImage.start(assetsDir, contributorsXml, xmlDir);
                        System.out.println("Contributor Image task completed");
                    } catch (Exception e) {
                        System.out.println("Error occurred: " + e.getMessage());
                    }
                    try {
                        XMLCreator.mergeNewDrawables(valuesDir,generatedDir,assetsDir,sourceDir,xmlDir,appFilter);
                        System.out.println("XML task completed");
                    } catch (Exception e) {
                        System.out.println("Error occurred: " + e.getMessage());
                    }
                    generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,rootDir,true);
                    generateReleaseImage( generatedDir + "/newdrawables.xml", sourceDir, generatedDir + "/releaseImage.webp");
                    break;
                default:
            }
        }
    }
}
