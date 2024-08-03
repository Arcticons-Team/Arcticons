package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.IOException;
import java.lang.ProcessBuilder;
import java.util.List;


public class ContributorImage {

    public static void start(String assetsDir, String contributorsXml, String xmlFilePath) throws IOException {

        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        xmlFilePath = xmlFilePath + "/contributors.xml";
        extractImageUrls(output, contributorsXml, assetsDir);
        output.append("\n</resources>");
        writeOutput(xmlFilePath, output);
    }


    public static void writeOutput(String pathXml, StringBuilder output) throws IOException {
        // Write to drawable.xml in res directory with UTF-8 encoding
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathXml), StandardCharsets.UTF_8))) {
            writer.write(output.toString());
        }
    }

    public static BufferedImage downloadImages(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            BufferedImage img = ImageIO.read(url);
            if (img != null) {
                //System.out.println("Downloaded image from: " + imageUrl);
                return img;
            }
        } catch (IOException e) {
            //System.err.println("Failed to download image from: " + imageUrl + " " + e.getMessage());
            return null;
        }
        return null;
    }

    public static void saveImage(BufferedImage image, String imageName, String imagePath) throws IOException {
        // Create the directory if it doesn't exist
        try {
            File directory = new File(imagePath + "/" + imageName).getParentFile();
            if (!directory.exists()) {
                boolean good = directory.mkdirs();
                if (!good) {
                    throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
                }
            }
        } catch (SecurityException e) {
            throw new IOException("Failed to create directory: " + e.getMessage(), e);
        }
        try {
            ImageIO.write(image, "webp", new File(imagePath + "/" + imageName));
        } catch (IOException e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }

    private static void appendCategory(StringBuilder output, String name, String contribution, String image, String link) {
        output.append("\n\t<contributor\n\t\tname=\"").append(name)
                .append("\"\n\t\tcontribution=\"").append(contribution)
                .append("\"\n\t\timage=\"").append(image)
                .append("\"\n\t\tlink=\"").append(link).append("\" />");
    }

    public static String setPlaceholderImage(int temp) {
        //set random image to last digit of temp
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
                    } else if (link.startsWith("assets://")) {
                        appendCategory(output, name, contribution, imageURL, link);
                    } else {
                        BufferedImage image = downloadImages(imageURL);
                        if (image != null) {
                            String imageName = "contributors/downloaded/contributor_" + temp + ".webp";
                            imageURL = "assets://" + imageName;
                            saveImage(image, imageName, assetsDir);
                            appendCategory(output, name, contribution, imageURL, link);
                        } else {
                            imageURL = setPlaceholderImage(temp);
                            appendCategory(output, name, contribution, imageURL, link);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }
    }
}
