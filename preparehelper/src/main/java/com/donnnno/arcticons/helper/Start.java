package com.donnnno.arcticons.helper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

public class Start {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("No flavor specified. (you, black, normal, dayNight)");
            return;
        }

        final Path currentPath = Paths.get(System.getProperty("user.dir"));
        final String rootDir = currentPath.getFileName().toString().equals("preparehelper") ? ".." : ".";
        final Path root = Paths.get(rootDir);

        final String sourceDir = root.resolve("icons/white").toString();
        final String xmlDir = root.resolve("app/src/main/res/xml").toString();
        final String generatedDir = root.resolve("generated").toString();
        final String assetsDir = root.resolve("app/src/main/assets").toString();
        final String appFilter = root.resolve("newicons/appfilter.xml").toString();
        final String valuesDir = root.resolve("app/src/main/res/values").toString();

        final String flavor = args[0];
        System.out.println("Processing flavor: " + flavor);

        final String destDir = switch (flavor) {
            case "you"      -> root.resolve("app/src/you/res/drawable-anydpi").toString();
            case "dayNight" -> root.resolve("app/src/dayNight/res/drawable-anydpi").toString();
            case "black"    -> root.resolve("app/src/light/res/drawable-anydpi").toString();
            case "normal"   -> root.resolve("app/src/dark/res/drawable-anydpi").toString();
            default         -> null;
        };

        if (destDir == null) {
            System.err.println("Unknown flavor: " + flavor);
            return;
        }

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {

            var svgTask = CompletableFuture.runAsync(() -> {
                runTimedTask("SvgToVector", () -> SvgConverter.process(sourceDir, destDir, flavor));
            }, executor);

            var xmlTask = CompletableFuture.runAsync(() -> {
                runTimedTask("XML Merger", () ->
                        XMLCreator.mergeNewDrawables(valuesDir, generatedDir, assetsDir, sourceDir, xmlDir, appFilter));
            }, executor);

            CompletableFuture.allOf(svgTask, xmlTask).join();
        }
    }

    private static void runTimedTask(String name, TaskRunnable runnable) {
        try {
            runnable.run();
            System.out.println(name + " task completed ");
        } catch (Exception e) {
            System.err.println("Error in " + name + ": " + e.getMessage());
        }
    }

    @FunctionalInterface
    interface TaskRunnable {
        void run() throws Exception;
    }
}