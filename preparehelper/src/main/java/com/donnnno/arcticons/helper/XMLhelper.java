package com.donnnno.arcticons.helper;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
public class XMLhelper {
    private static final String UTF_8 = "UTF-8";

    public static List<Element> getElements(Document document, String path) {
        return document.getRootElement().elements(path);
    }

    public static Document getDocument(String xmlPath) throws Exception {
        SAXReader saxReader = new SAXReader();
        saxReader.setEncoding(UTF_8);
        return saxReader.read(new File(xmlPath));
    }

    public static String getFileWithExtension(Path target) {
        String svgFilePath = target.toFile().getAbsolutePath();
        int index = svgFilePath.lastIndexOf(".");
        StringBuilder result = new StringBuilder();
        if (index != -1) {
            result.append(svgFilePath, 0, index);
        }
        result.append('.').append("xml");
        return result.toString();
    }

    public static void writeDocumentToFile(Document outDocument, String outputConfigPath) throws IOException {
        File parentFile = new File(outputConfigPath).getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IOException("Failed to create directory: " + parentFile.getAbsolutePath());
        }

        // Delete existing file if any
        File outputFile = new File(outputConfigPath);
        if (outputFile.exists() && !outputFile.delete()) {
            throw new IOException("Failed to delete existing file: " + outputConfigPath);
        }

        try (FileWriter fw = new FileWriter(outputFile)) {
            XMLWriter xmlWriter = new XMLWriter(fw, OutputFormat.createPrettyPrint());
            xmlWriter.write(outDocument);
            xmlWriter.close();
        }
    }
}