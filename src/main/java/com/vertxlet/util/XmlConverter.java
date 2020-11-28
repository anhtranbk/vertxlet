package com.vertxlet.util;

import io.vertx.core.json.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class XmlConverter {

    public static JsonObject toJson(String xml, String rootNodeName) throws ParserConfigurationException,
            IOException, SAXException {

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new ByteArrayInputStream(
                xml.replaceAll("\n", "").getBytes(StandardCharsets.UTF_8))));

        doc.getDocumentElement().normalize();

        if (!doc.getDocumentElement().getNodeName().equalsIgnoreCase(rootNodeName))
            throw new ParserConfigurationException("Root tag not found");

        Node rootNode = doc.getElementsByTagName(rootNodeName).item(0);
        JsonObject result = new JsonObject();

        XmlConverter.nodeToJsonObject(result, rootNode);

        return result;
    }

    private static void nodeToJsonObject(JsonObject rootObject, Node node) {
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            Node firstNode = node.getFirstChild();
            if (firstNode != null && firstNode.getNodeType() == Node.TEXT_NODE) {
                String text = firstNode.getTextContent();
                try {
                    Long number = Long.parseLong(text);
                    rootObject.put(node.getNodeName(), number);
                } catch (NumberFormatException e) {
                    rootObject.put(node.getNodeName(), text);
                }
                return;
            }

            JsonObject object = new JsonObject();
            rootObject.put(node.getNodeName(), object);

            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                nodeToJsonObject(object, nodeList.item(i));
            }
        }
    }
}
