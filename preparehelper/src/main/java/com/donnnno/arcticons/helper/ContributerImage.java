package com.donnnno.arcticons.helper;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ContributerImage {
    public static void main(String[] args) throws IOException {
        String rootDir = System.getProperty("user.dir");
        Path rootPath = Paths.get(rootDir);
        String rootDirName = rootPath.getFileName().toString();
        if (rootDirName.equals("preparehelper")) {
            rootDir = "..";
        }
        String xmlFilePath = rootDir + "/app/src/main/res/xml/contributors.xml";
        String assetsDir = rootDir + "/app/src/main/assets";
        String contributorsXml = rootDir + "/generated/contributors.xml";

        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        extractImageUrls(output,contributorsXml,assetsDir);
        output.append("\n</resources>");
        writeOutput(xmlFilePath,output);
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
                    System.out.println("Downloaded image from: " + imageUrl);
                    return img;
                } else {
                    System.err.println("Failed to download image from: " + imageUrl);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        return null;
    }

    public static void saveImage(BufferedImage image, String imageName,String imagePath) {
        try {
            ImageIO.write(image, "png", new File(imagePath+"/"+imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void appendCategory(StringBuilder output, String name,String contribution,String image,String link) {
        output.append("\n\t<contributor\n\t\tname=\"").append(name)
                .append("\"\n\t\tcontribution=\"").append(contribution)
                .append("\"\n\t\timage=\"").append(image)
                .append("\"\n\t\tlink=\"").append(link).append("\" />");
    }

    public static void extractImageUrls(StringBuilder output,String contributorsXml,String assetsDir) {
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

                    if (imageURL.isEmpty()) {
                        appendCategory(output,name,contribution,imageURL,link);
                    }else if (link.startsWith("assets://")) {
                        appendCategory(output,name,contribution,imageURL,link);
                    }else {
                        BufferedImage image = downloadImages(imageURL);
                        if (image != null) {
                            String imageName ="contributors/contributor_" + temp+".png";
                            imageURL = "assets://" + imageName;
                            saveImage(image,imageName,assetsDir);
                            appendCategory(output,name,contribution,imageURL,link);
                        }
                        else {
                            imageURL = "";
                            appendCategory(output, name, contribution, imageURL, link);
                        };
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
