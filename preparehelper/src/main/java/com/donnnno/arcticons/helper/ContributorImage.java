package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ContributorImage {

    public static void start(String assetsDir, String contributorsXml, String xmlFilePath) throws IOException {
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        xmlFilePath = xmlFilePath + "/contributors.xml";
        extractImageUrls(output, contributorsXml, assetsDir);
        output.append("\n</resources>");
        writeOutput(xmlFilePath, output);
    }

    public static void writeOutput(String pathXml, StringBuilder output) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathXml), StandardCharsets.UTF_8))) {
            writer.write(output.toString());
        }
    }

    public static BufferedImage downloadImages(String imageUrl) {
        try {
            if (imageUrl.toLowerCase().endsWith(".webp")) {
                return null; // we handle .webp separately
            }
            URL url = new URI(imageUrl).toURL();
            return ImageIO.read(url);
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }

    public static void saveImage(BufferedImage image, String imageName, String imagePath) throws IOException {
        File directory = new File(imagePath + "/" + imageName).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }

        File tempPng = File.createTempFile("temp_contributor", ".png");
        ImageIO.write(image, "png", tempPng);

        File webpFile = new File(imagePath + "/" + imageName);
        try {
            convertPngToWebp(tempPng, webpFile);
        } catch (Exception e) {
            System.out.println("Error converting PNG to WebP: " + e.getMessage());
        } finally {
            tempPng.delete();
        }
    }

    private static void saveWebpDirect(String imageUrl, String imageName, String imagePath) throws IOException {
        try {
            URL url = new URI(imageUrl).toURL();
            InputStream in = url.openStream();
            File outputFile = new File(imagePath + "/" + imageName);
            File directory = outputFile.getParentFile();
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
            }
            Files.copy(in, outputFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            in.close();
        } catch (URISyntaxException e) {
            throw new IOException("Invalid image URL: " + imageUrl, e);
        }
    }

    private static void convertPngToWebp(File pngFile, File webpFile) throws IOException, InterruptedException {
        ProcessBuilder builder = new ProcessBuilder(
                "cwebp",
                "-q", "50", // compress to 70% quality
                pngFile.getAbsolutePath(),
                "-o", webpFile.getAbsolutePath()
        );
        Process process = builder.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("cwebp conversion failed with exit code " + exitCode);
        }
    }

    private static void appendCategory(StringBuilder output, String name, String contribution, String image, String link) {
        output.append("\n\t<contributor\n\t\tname=\"").append(name)
                .append("\"\n\t\tcontribution=\"").append(contribution)
                .append("\"\n\t\timage=\"").append(image)
                .append("\"\n\t\tlink=\"").append(link).append("\" />");
    }

    public static String setPlaceholderImage(int temp) {
        int lastDigit = Math.abs(temp % 10);
        return "assets://contributors/face_" + lastDigit + ".webp";
    }

    public static void extractImageUrls(StringBuilder output, String contributorsXml, String assetsDir) {
        try {
            File inputFile = new File(contributorsXml);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("contributor");

            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getAttribute("name");
                    String contribution = eElement.getAttribute("contribution");
                    String imageURL = eElement.getAttribute("image");
                    String link = eElement.getAttribute("link");
                    if (link.startsWith("https://github.com/") && imageURL.isEmpty()) {
                        imageURL = link + ".png";
                    }
                    if (imageURL.isEmpty()) {
                        imageURL = setPlaceholderImage(temp);
                        appendCategory(output, name, contribution, imageURL, link);
                    } else if (imageURL.startsWith("assets://")) {
                        appendCategory(output, name, contribution, imageURL, link);
                    } else {
                        String imageName = "contributors/downloaded/contributor_" + temp + ".webp";
                        imageURL = "assets://" + imageName;

                        if (imageURL.toLowerCase().endsWith(".webp")) {
                            try {
                                saveWebpDirect(eElement.getAttribute("image"), imageName, assetsDir);
                                appendCategory(output, name, contribution, imageURL, link);
                            } catch (Exception e) {
                                imageURL = setPlaceholderImage(temp);
                                appendCategory(output, name, contribution, imageURL, link);
                            }
                        } else {
                            BufferedImage image = downloadImages(eElement.getAttribute("image"));
                            if (image != null) {
                                saveImage(image, imageName, assetsDir);
                                appendCategory(output, name, contribution, imageURL, link);
                            } else {
                                imageURL = setPlaceholderImage(temp);
                                appendCategory(output, name, contribution, imageURL, link);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}
