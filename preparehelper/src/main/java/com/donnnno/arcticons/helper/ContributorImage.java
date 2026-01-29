package com.donnnno.arcticons.helper;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilderFactory;

public class ContributorImage {

    private static final String WEBP = "webp";
    private static final String PREFIX_ASSETS = "assets://";

    public static void start(String assetsDir, String contributorsXml, String xmlOutputDir) throws IOException {
        StringBuilder output = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");

        // Ensure directory existence
        Path outputPath = Paths.get(xmlOutputDir, "contributors.xml");
        Files.createDirectories(outputPath.getParent());

        processContributors(output, contributorsXml, assetsDir);

        output.append("\n</resources>");
        Files.writeString(outputPath, output.toString(), StandardCharsets.UTF_8);
    }

    private static void processContributors(StringBuilder output, String contributorsXml, String assetsDir) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(contributorsXml));
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("contributor");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element el = (Element) node;

                    String name = el.getAttribute("name");
                    String contribution = el.getAttribute("contribution");
                    String link = el.getAttribute("link");
                    String imageURL = el.getAttribute("image");

                    // 1. Auto-generate GitHub avatar link if image is missing
                    if (imageURL.isEmpty() && link.startsWith("https://github.com/")) {
                        imageURL = link + ".png";
                    }

                    // 2. Determine final image path
                    String finalImagePath;
                    if (imageURL.isEmpty()) {
                        finalImagePath = getPlaceholderImage(i);
                    } else if (imageURL.startsWith(PREFIX_ASSETS)) {
                        finalImagePath = imageURL;
                    } else {
                        // 3. Download and convert external images
                        String localName = String.format(Locale.ROOT, "contributors/downloaded/contributor_%d.webp", i);
                        boolean success = downloadAndSave(imageURL, assetsDir, localName);
                        finalImagePath = success ? PREFIX_ASSETS + localName : getPlaceholderImage(i);
                    }

                    appendContributorTag(output, name, contribution, finalImagePath, link);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing contributors: " + e.getMessage());
        }
    }

    private static boolean downloadAndSave(String urlStr, String assetsDir, String localName) {
        try {
            BufferedImage image = ImageIO.read(URI.create(urlStr).toURL());
            if (image == null) return false;

            Path destination = Paths.get(assetsDir, localName);
            Files.createDirectories(destination.getParent());

            return ImageIO.write(image, WEBP, destination.toFile());
        } catch (Exception e) {
            return false;
        }
    }

    private static String getPlaceholderImage(int index) {
        return String.format(Locale.ROOT, "assets://contributors/face_%d.webp", Math.abs(index % 10));
    }

    private static void appendContributorTag(StringBuilder sb, String name, String contribution, String image, String link) {
        sb.append(String.format(Locale.ROOT,
                "\n\t<contributor\n\t\tname=\"%s\"\n\t\tcontribution=\"%s\"\n\t\timage=\"%s\"\n\t\tlink=\"%s\" />",
                name, contribution, image, link));
    }
}