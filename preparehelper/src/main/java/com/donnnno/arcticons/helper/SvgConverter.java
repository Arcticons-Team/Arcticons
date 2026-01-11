package com.donnnno.arcticons.helper;

import com.android.ide.common.vectordrawable.Svg2Vector;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Objects;

public class SvgConverter {
    private static Path sourceSvgPath;
    private static Path destinationVectorPath;
    private static String flavor;
    private static final FileVisitor<Path> fileVisitor = new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            // Skip folder which is processing svgs to xml
            if (dir.equals(destinationVectorPath)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            Path newDirectory = destinationVectorPath.resolve(sourceSvgPath.relativize(dir));
            try {
                Files.createDirectories(newDirectory);
            } catch (FileAlreadyExistsException e) {
                System.out.println("Directory already exists");
            } catch (IOException e) {
                System.out.println("Error creating directory");
                return FileVisitResult.SKIP_SUBTREE;
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            convertToVector(file, destinationVectorPath.resolve(sourceSvgPath.relativize(file)));
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.out.println("File visit failed");
            return FileVisitResult.CONTINUE;
        }
    };

    public static void process(String sourceDirectory, String destDirectory, String getFlavor) {
        sourceSvgPath = Paths.get(sourceDirectory);
        destinationVectorPath = Paths.get(destDirectory);
        flavor = getFlavor;
        try {
            EnumSet<FileVisitOption> options = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
            // check first if source is a directory
            if (Files.isDirectory(sourceSvgPath)) {
                Files.walkFileTree(sourceSvgPath, options, Integer.MAX_VALUE, fileVisitor);
            } else {
                System.out.println("source not a directory");
            }
        } catch (IOException e) {
            System.out.println("Error processing files" + e.getMessage());
        }
    }

    private static void convertToVector(Path svgSource, Path vectorTargetPath) {
        // convert only if it is .svg
        if (svgSource.getFileName().toString().endsWith(".svg")) {
            Path targetFile = Path.of(XMLhelper.getFileWithExtension(vectorTargetPath));
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                Svg2Vector.parseSvgToXml(svgSource, byteArrayOutputStream);
                if (Objects.equals(flavor, "you")){
                createAdaptive(byteArrayOutputStream, String.valueOf(targetFile));
                } else if (Objects.equals(flavor, "black")){
                    createDrawable(byteArrayOutputStream, String.valueOf(targetFile),"#000000");
                }else if (Objects.equals(flavor, "normal")){
                    createDrawable(byteArrayOutputStream, String.valueOf(targetFile),"#ffffff");
                }else if (Objects.equals(flavor, "dayNight")) {
                    createDrawable(byteArrayOutputStream, String.valueOf(targetFile), "@color/icon_color");
                }
            } catch (Exception e) {
                System.out.println("Error converting file " + svgSource.getFileName());
            }
        } else {
            System.out.println("Skipping file as it's not an svg " + svgSource.getFileName());
        }
    }
    private static void createDrawable(ByteArrayOutputStream byteArrayOutputStream, String resPath, String color) throws Exception {
        String px = "1";
        String XmlContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        Document document = DocumentHelper.parseText(XmlContent);
        updateXmlPath(document, "android:strokeColor", color);
        updateXmlPath(document, "android:fillColor", color);
        updateXmlPath(document, "android:strokeWidth", px);
        updateRootElement(document, "android:tint", color);
        XMLhelper.writeDocumentToFile(document, resPath);
    }

    private static void createAdaptive(ByteArrayOutputStream byteArrayOutputStream, String resPath) throws Exception {
        String fg = "@color/icon_color";
        String bg = "@color/icon_background_color";
        String px = "1.2";
        String foregroundXmlContent = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        Document foregroundDocument = DocumentHelper.parseText(foregroundXmlContent);
        Element rootElement = foregroundDocument.getRootElement();

        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("adaptive-icon")
                .addAttribute("xmlns:android", "http://schemas.android.com/apk/res/android");
        root.addElement("background")
                .addAttribute("android:drawable", bg);
        root.addElement("foreground").addElement("inset")
                .addAttribute("android:inset", "25%")
                .add(rootElement);
        updateXmlPath(document, "android:strokeColor", fg);
        updateXmlPath(document, "android:fillColor", fg);
        updateXmlPath(document, "android:strokeWidth", px);
        updateRootElement(document, "android:tint", fg);
        XMLhelper.writeDocumentToFile(document, resPath);
    }


    private static void updateRootElement(Document aDocument, String key, String value) {

        String keyWithoutNameSpace = key.substring(key.indexOf(":") + 1);
        org.dom4j.Attribute attr = aDocument.getRootElement().attribute(keyWithoutNameSpace);
        if (attr != null) {
            if (!attr.getValue().equals("#00000000")) {
                attr.setValue(value);
            }
        } else {
            aDocument.getRootElement().addAttribute(key, value);
        }
    }

    private static void updateXmlPath(Document xmlDocument, String searchKey, String attributeValue) {
        updateXmlPath(xmlDocument.getRootElement(), searchKey, attributeValue);
    }

    private static void updateXmlPath(Element parentElement, String searchKey, String attributeValue) {
        String keyWithoutNameSpace = searchKey.substring(searchKey.indexOf(":") + 1);

        for (Object e : parentElement.elements()) {
            if (e instanceof Element element) {
                if ("path".equals(element.getName())) {
                    org.dom4j.Attribute attr = element.attribute(keyWithoutNameSpace);
                    if (attr != null && !attr.getValue().equals("#00000000")) {
                        attr.setValue(attributeValue);
                    }
                }

                // Recursively process child elements
                updateXmlPath(element, searchKey, attributeValue);
            }
        }
    }

}
