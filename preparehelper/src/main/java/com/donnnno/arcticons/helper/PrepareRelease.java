package com.donnnno.arcticons.helper;

import static com.donnnno.arcticons.helper.Changelog.generateChangelogs;
import static com.donnnno.arcticons.helper.Checks.startChecks;
import static com.donnnno.arcticons.helper.ImageCollageGenerator.generateReleaseImage;

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

            String task = args[0];
            System.out.println("Processing with task: " + task);
            switch (task) {
                case "checkonly":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    break;
                case "release":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    executePythonScript(rootDir + "/scripts/preparerelease.py");
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
                    generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,false);
                    generateReleaseImage( generatedDir + "/newdrawables.xml", sourceDir, generatedDir + "/releaseImage.webp");
                    break;
                case "newrelease":
                    startChecks(appFilter, sourceDir, newIconsDir);
                    executePythonScript(rootDir + "/scripts/preparerelease.py","--new");
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
                    generateChangelogs(generatedDir, valuesDir+"/custom_icon_count.xml", appFilter, changelogXml,true);
                    generateReleaseImage( generatedDir + "/newdrawables.xml", sourceDir, generatedDir + "/releaseImage.webp");
                    break;
                default:
            }

        }
    }

    public static void executePythonScript(String... args) throws Exception {
            List<String> command = new ArrayList<>();
            command.add("python");
            // Add all provided arguments to the command list
            command.addAll(Arrays.asList(args));
            command.add("..");

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Python script execution failed with exit code: " + exitCode);
        }
    }
}
