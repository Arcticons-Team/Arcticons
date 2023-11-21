package com.donnnno.arcticons.helper;

public class Start {

    public static void main(String[] args) {
        if (args.length > 0) {
            String flavor = args[0];
            // Use the flavor as needed
            System.out.println("Processing with flavor: " + flavor);
            String rootDir = "..";
            String sourceDir = rootDir + "/icons/white";
            String resDir;
            String destDir;
            //String appFilterFile = rootDir + "/app/assets/appfilter.xml";
            switch (flavor) {
                case "you":
                    resDir = rootDir + "/app/src/you/res";
                    destDir = resDir + "/drawable-anydpi";
                    // Convert svg to drawable in runtime
                    SvgConverter.process(sourceDir, destDir, flavor);
                    break;

                case "light":
                    resDir = rootDir + "/app/src/light/res";
                    destDir = resDir + "/drawable-anydpi";
                    // Convert svg to drawable in runtime
                    SvgConverter.process(sourceDir, destDir, flavor);
                    break;

                case "dark":
                    resDir = rootDir + "/app/src/dark/res";
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