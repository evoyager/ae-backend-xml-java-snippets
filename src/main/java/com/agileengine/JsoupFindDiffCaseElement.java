package com.agileengine;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.agileengine.JsoupCssSelectSnippet.findElementsByQuery;
import static com.agileengine.JsoupCssSelectSnippet.parseAttributesFromElementsAndOutput;
import static com.agileengine.JsoupFindByIdSnippet.findElementById;
import static com.agileengine.JsoupFindByIdSnippet.parseAttributesFromElementAndOutput;

public class JsoupFindDiffCaseElement {
    private static Logger LOGGER = LoggerFactory.getLogger(JsoupFindDiffCaseElement.class);
    private static String INPUT_ORIGIN_FILE_PATH;
    private static String INPUT_OTHER_SAMPLE_FILE_PATH;
    private static String ORIGIN_ELEMENT_NAME = "Origin";
    private static String DIFF_ELEMENT_NAME = "Diff";
    private static String TARGET_ELEMENT_ID = "make-everything-ok-button";
    private static String DELIMITER = " > ";

    public static void main(String[] args) {

        INPUT_ORIGIN_FILE_PATH = args[0];
        INPUT_OTHER_SAMPLE_FILE_PATH = args[1];

        Optional<Element> originButtonOpt = findElementById(new File(INPUT_ORIGIN_FILE_PATH), TARGET_ELEMENT_ID);
        String originButtonParentTagName = "";
        if (originButtonOpt.isPresent()) {
            originButtonParentTagName = originButtonOpt.get().parent().tagName();
        }

        parseAttributesFromElementAndOutput(originButtonOpt, ORIGIN_ELEMENT_NAME, LOGGER);

        String finalOriginButtonParentTagName = originButtonParentTagName;
        Optional<List<String>> cssQueriesOpt = originButtonOpt.map(button ->
            button.attributes().asList().stream()
                .map(attr -> {
                    String buttonTag = originButtonOpt.get().tagName();
                    String query = finalOriginButtonParentTagName + " > " + buttonTag + "[" + attr.getKey() + "=\"" + attr.getValue() + "\"]";
                    return query;
                })
                .collect(Collectors.toList())
        );
        if (cssQueriesOpt.isPresent()) {
            for (String cssQuery : cssQueriesOpt.get()) {
                Optional<Elements> diffButtonsOpt = findElementsByQuery(new File(INPUT_OTHER_SAMPLE_FILE_PATH), cssQuery);
                if (diffButtonsOpt.isPresent() && diffButtonsOpt.get().size() == 1) {
                    Optional<Element> diffButtonOpt = Optional.of(diffButtonsOpt.get().get(0));
                    parseAttributesFromElementsAndOutput(diffButtonsOpt, DIFF_ELEMENT_NAME, LOGGER);
                    parseSameAndDiffAttributesAndOutput(originButtonOpt, diffButtonOpt);
                    LOGGER.info("CSS query: {}", cssQuery);
                    outputAbsolutePathOfElements(diffButtonsOpt);
                    break;
                }
            }
        }
    }

    private static void parseSameAndDiffAttributesAndOutput(Optional<Element> originElement, Optional<Element> diffElement) {
        List<Attribute> originElementAttributes = originElement.get().attributes().asList();

        Optional<String> sameAttributesOpt = diffElement.map(button ->
            button.attributes().asList().stream()
                .filter(attr -> originElementAttributes.contains(attr))
                .map(attr -> attr.getKey() + " = " + attr.getValue())
                .collect(Collectors.joining(", "))
        );

        sameAttributesOpt.ifPresent(attrs -> {
            LOGGER.info("{} element same attrs number: [{}]", DIFF_ELEMENT_NAME, attrs.chars().filter(ch -> ch == ',').count());
            LOGGER.info("{} element same attrs: [{}]", DIFF_ELEMENT_NAME, attrs);
        });

        Optional<String> diffAttributesOpt = diffElement.map(button ->
            button.attributes().asList().stream()
                .filter(attr -> !originElementAttributes.contains(attr))
                .map(attr -> attr.getKey() + " = " + attr.getValue())
                .collect(Collectors.joining(", "))
        );

        diffAttributesOpt.ifPresent(attrs -> {
            long attrNumber = attrs.chars().filter(ch -> ch == ',').count() + 1;
            int attrNumberZerroOrOne = attrs.length() == 0 ? 0 : 1;
            LOGGER.info("{} element different attrs number: [{}]", DIFF_ELEMENT_NAME, attrNumber > 0 ? attrNumber : attrNumberZerroOrOne);
            LOGGER.info("{} element different attrs: [{}]", DIFF_ELEMENT_NAME, attrs);
        });
    }

    private static void outputAbsolutePathOfElements(Optional<Elements> elementsOpt) {
        elementsOpt.ifPresent(elementsList ->
            elementsList.forEach(element -> {
                    StringBuilder absPath=new StringBuilder();
                    Elements parents = element.parents();

                    for (int i = parents.size()-1; i >= 0; i--) {
                        Element curElement = parents.get(i);
                        Element currentSibling = curElement;
                        String currentElementTag = curElement.tagName();
                        int numberOfSiblingsWithSameTag = 0;
                        while (currentSibling.previousElementSibling() != null) {
                            currentSibling = currentSibling.previousElementSibling();
                            if (currentSibling.tagName().equals(currentElementTag)) {
                                numberOfSiblingsWithSameTag++;
                            }
                        }
                        if (i != parents.size()-1) {
                            absPath.append(DELIMITER);
                        }
                        absPath.append(curElement.tagName());
                        if (numberOfSiblingsWithSameTag != 0) {
                            absPath.append("[");
                            absPath.append(numberOfSiblingsWithSameTag);
                            absPath.append("]");
                        }
                    }
                    absPath.append(DELIMITER);
                    String elementTag = element.tagName();
                    absPath.append(elementTag);
                    LOGGER.info("Absolute path: {}", absPath.toString());
                }
            )
        );
    }
}
