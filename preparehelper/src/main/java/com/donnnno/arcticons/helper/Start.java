package com.donnnno.arcticons.helper;

public class Start {

    public static void main(String[] args) {
        String rootDir = "..";
        String sourceDir = rootDir + "/icons/white";
        String resDir = rootDir + "/app/src/you/res";
        String appFilterFile = rootDir + "/app/assets/appfilter.xml";
        String destDir =resDir + "/drawable-anydpi";

        // Convert svg to drawable in runtime
        SvgConverter.process(sourceDir,destDir);

        // Read appfilter xml and create icon, drawable xml file.
       /* try {
            ConfigProcessor.loadAndCreateConfigs(appFilterFile, resDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        System.out.println("SvgToVector task completed");
    }
}