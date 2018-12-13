package com.agileengine;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JsoupFindByIdSnippet {

    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindByIdSnippet.class);

    private static String CHARSET_NAME = "utf8";

    public static void main(String[] args) {

        // Jsoup requires an absolute file path to resolve possible relative paths in HTML,
        // so providing InputStream through classpath resources is not a case
        String resourcePath = "./samples/sample-0-origin.html";
        String targetElementId = "make-everything-ok-button";

        Optional<Element> buttonOpt = findElementById(new File(resourcePath), targetElementId);
        parseAttributesFromElementAndOutput(buttonOpt, "Target", LOGGER);
    }

    public static Optional<String> parseAttributesFromElementAndOutput(Optional<Element> elementOpt, String elementName, Logger logger) {
        Optional<String> stringifiedAttributesOpt = elementOpt.map(button ->
            button.attributes().asList().stream()
                .map(attr -> attr.getKey() + " = " + attr.getValue())
                .collect(Collectors.joining(", "))
        );

        stringifiedAttributesOpt.ifPresent(attrs -> logger.info("{} element attrs: [{}]", elementName, attrs));

        return stringifiedAttributesOpt;
    }

    public static Optional<Element> findElementById(File htmlFile, String targetElementId) {
        try {
            Document doc = Jsoup.parse(
                    htmlFile,
                    CHARSET_NAME,
                    htmlFile.getAbsolutePath());

            return Optional.of(doc.getElementById(targetElementId));

        } catch (IOException e) {
            LOGGER.error("Error reading [{}] file", htmlFile.getAbsolutePath(), e);
            return Optional.empty();
        }
    }

}