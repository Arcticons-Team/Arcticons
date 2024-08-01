package com.donnnno.arcticons.helper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PrepareRelease {

    public static void main(String[] args) throws Exception {
        String check;
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
            String task = args[0];
            System.out.println("Processing with task: " + task);
            switch (task) {
                case "checkonly":
                    check = "--checkonly";
                    break;
                case "release":
                    check = "";
                    try {
                        ContributorImage.start(assetsDir, contributorsXml, xmlDir);
                        System.out.println("Contributor Image task completed");
                    } catch (Exception e) {
                        System.out.println("Error occurred: " + e.getMessage());
                    }
                    break;
                default:
                    return;
            }
            executePythonScript(rootDir + "/scripts/preparerelease.py",check);
        }
    }

    public static void executePythonScript(String scriptPath, String check) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder("python", scriptPath, check, ".." );
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
