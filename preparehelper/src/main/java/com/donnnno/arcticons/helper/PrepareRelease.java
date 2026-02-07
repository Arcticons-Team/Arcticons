package com.donnnno.arcticons.helper;

import static com.donnnno.arcticons.helper.Changelog.generateChangelogs;
import static com.donnnno.arcticons.helper.Checks.startChecks;
import static com.donnnno.arcticons.helper.ImageCollageGenerator.generateReleaseImage;
import static com.donnnno.arcticons.helper.NewDrawableXmlCreator.createNewDrawables;
import static com.donnnno.arcticons.helper.SortAppfilter.sortXML;
import static com.donnnno.arcticons.helper.WebpCreator.createWebpIcons;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class PrepareRelease {

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("No task specified.");
            return;
        }

        // Determine root path
        final Path tempRoot = Paths.get(System.getProperty("user.dir"));
        final Path root = tempRoot.getFileName().toString().equals("preparehelper")
                ? tempRoot.getParent()
                : tempRoot;

        // Define all directory strings as FINAL
        final String appRes = "app/src/main/res";
        final String xmlDir = root.resolve(appRes + "/xml").toString();
        final String assetsDir = root.resolve("app/src/main/assets").toString();
        final String generatedDir = root.resolve("generated").toString();
        final String valuesDir = root.resolve(appRes + "/values").toString();
        final String appFilter = root.resolve("newicons/appfilter.xml").toString();
        final String newIconsDir = root.resolve("newicons").toString();
        final String sourceDir = root.resolve("icons/white").toString();
        final String blackDir = root.resolve("icons/black").toString();
        final String changelogXml = root.resolve(valuesDir + "/changelog.xml").toString();
        final String exportWhiteDir = root.resolve("app/src/normal/res/drawable-nodpi").toString();
        final String exportBlackDir = root.resolve("app/src/black/res/drawable-nodpi").toString();
        final String contributorsXml = root.resolve("generated/contributors.xml").toString();
        String gradlePath = root.resolve("/app/build.gradle").toString();
        final String rootString = root.toString();

        final String task = args[0];
        System.out.println("Starting task: " + task);

        switch (task) {
            case "checkonly" -> startChecks(appFilter, sourceDir, newIconsDir);

            case "release", "newrelease" -> {
                final boolean isNewRelease = task.equals("newrelease");

                startChecks(appFilter, sourceDir, newIconsDir);

                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    var task1 = CompletableFuture.runAsync(() ->
                            runTask("Contributor Image", () -> ContributorImage.start(assetsDir, contributorsXml, xmlDir)), executor);

                    var task2 = CompletableFuture.runAsync(() -> {
                        runTask("New Drawables", () -> createNewDrawables(newIconsDir, generatedDir + "/newDrawables.xml", isNewRelease));
                        runTask("Webp Creator", () -> createWebpIcons(newIconsDir, sourceDir, blackDir, exportWhiteDir, exportBlackDir));
                        runTask("Sort Appfilter", () -> sortXML(Paths.get(appFilter)));
                        runTask("XML Merger", () -> XMLCreator.mergeNewDrawables(valuesDir, generatedDir, assetsDir, sourceDir, xmlDir, appFilter));
                        runTask("Create Changelogs", () -> generateChangelogs(generatedDir, valuesDir + "/custom_icon_count.xml", appFilter, changelogXml, rootString, isNewRelease));
                        runTask("New Release Image", () -> generateReleaseImage(gradlePath, generatedDir + "/newDrawables.xml", sourceDir, generatedDir + "/releaseImage.webp"));
                    }, executor);

                    CompletableFuture.allOf(task1, task2).join();
                }
            }
            default -> System.err.println("Unknown task: " + task);
        }
    }

    private static void runTask(String name, TaskRunnable runnable) {
        try {
            runnable.run();
            System.out.println(name + " completed");
        } catch (Exception e) {
            System.err.println(name + " failed: " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface TaskRunnable {
        void run() throws Exception;
    }
}