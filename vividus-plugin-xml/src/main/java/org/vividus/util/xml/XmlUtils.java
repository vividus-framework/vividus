/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.util.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.vividus.util.pool.UnsafeGenericObjectPool;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public final class XmlUtils
{
    private static final String INDENTATION_LEVEL = "4";
    private static final String YES = "yes";

    private static final UnsafeGenericObjectPool<XPathFactory> XPATH_FACTORY = new UnsafeGenericObjectPool<>(
            XPathFactory::newInstance);
    private static final UnsafeGenericObjectPool<TransformerFactory> TRANSFORMER_FACTORY =
            new UnsafeGenericObjectPool<>(TransformerFactory::newInstance);
    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static final UnsafeGenericObjectPool<DocumentBuilder> DOCUMENT_BUILDER;

    static
    {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
        try
        {
            DOCUMENT_BUILDER_FACTORY.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        }
        catch (ParserConfigurationException e)
        {
            throw new IllegalStateException(e);
        }
        DOCUMENT_BUILDER = new UnsafeGenericObjectPool<>(new BasePooledObjectFactory<>()
        {
            @Override
            public DocumentBuilder create() throws ParserConfigurationException
            {
                return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            }

            @Override
            public PooledObject<DocumentBuilder> wrap(DocumentBuilder obj)
            {
                return new DefaultPooledObject<>(obj);
            }
        });
    }

    private XmlUtils()
    {
    }

    public static Document convertToDocument(String xml)
    {
        return DOCUMENT_BUILDER.apply(documentBuilder ->
        {
            try
            {
                return documentBuilder.parse(createInputSource(xml));
            }
            catch (SAXException | IOException e)
            {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    /**
     * Search by XPath in XML
     * @param xml XML
     * @param xpath xpath
     * @return Search result
     */
    public static Optional<String> getXmlByXpath(String xml, String xpath)
    {
        return XPATH_FACTORY.apply(xPathFactory -> {
            try
            {
                InputSource source = createInputSource(xml);
                NodeList nodeList = (NodeList) xPathFactory.newXPath().evaluate(xpath, source, XPathConstants.NODESET);
                Node singleNode = nodeList.item(0);
                Properties outputProperties = new Properties();
                outputProperties.setProperty(OutputKeys.OMIT_XML_DECLARATION, YES);
                return transform(new DOMSource(singleNode), outputProperties);
            }
            catch (XPathExpressionException e)
            {
                throw new IllegalStateException(e.getMessage(), e);
            }
        });
    }

    public static void validateXmlAgainstXsd(String xml, String xsd) throws SAXException, IOException
    {
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(createStreamSource(xsd))
                .newValidator()
                .validate(createStreamSource(xml));
    }

    public static void transform(String xml, String xslt, Consumer<String> transformedXmlConsumer,
            Consumer<TransformerException> transformerExceptionConsumer)
    {
        StreamSource xmlSource = createStreamSource(xml);
        StreamSource xsltSource = createStreamSource(xslt);
        TRANSFORMER_FACTORY.accept(transformerFactory ->
        {
            try
            {
                Transformer transformer = transformerFactory.newTransformer(xsltSource);
                String transformedXml = transform(xmlSource, transformer);
                transformedXmlConsumer.accept(transformedXml);
            }
            catch (TransformerException e)
            {
                transformerExceptionConsumer.accept(e);
            }
        });
    }

    /**
     * Performs formatting of xml string using default properties.
     * <br>
     * Default properties
     * <table border="1">
     * <caption>Default properties</caption>
     * <thead><tr><td>Key</td><td>Value</td></tr></thead>
     * <tbody>
     * <tr><td>indent</td><td>yes</td></tr>
     * <tr><td>{http://xml.apache.org/xalan}indent-amount</td><td>4</td></tr>
     * </tbody>
     * </table>
     * @param xml A xml string to format
     * @return Optional containing formatted xml string or empty optional if exception happened during formatting.
     */
    public static Optional<String> format(String xml)
    {
        Properties outputProperties = new Properties();
        outputProperties.setProperty(OutputKeys.INDENT, YES);
        outputProperties.setProperty("{http://xml.apache.org/xalan}indent-amount", INDENTATION_LEVEL);
        return transform(createStreamSource(xml), outputProperties);
    }

    private static Optional<String> transform(Source xmlSource, Properties outputProperties)
    {
        return TRANSFORMER_FACTORY.apply(transformerFactory -> {
            try
            {
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperties(outputProperties);
                String result = transform(xmlSource, transformer);
                return Optional.of(result);
            }
            catch (TransformerException e)
            {
                return Optional.empty();
            }
        });
    }

    private static String transform(Source xmlSource, Transformer transformer) throws TransformerException
    {
        StringWriter stringWriter = new StringWriter();
        transformer.transform(xmlSource, new StreamResult(stringWriter));
        return stringWriter.toString();
    }

    private static StreamSource createStreamSource(String str)
    {
        return createSource(str, StreamSource::new);
    }

    private static InputSource createInputSource(String str)
    {
        return createSource(str, InputSource::new);
    }

    private static <T> T createSource(String str, Function<StringReader, T> sourceFactory)
    {
        return sourceFactory.apply(new StringReader(str));
    }
}
