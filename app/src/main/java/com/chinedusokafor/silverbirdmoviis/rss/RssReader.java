package com.chinedusokafor.silverbirdmoviis.rss;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


/**
 * Created by cokafor on 1/22/2015.
 */
public class RssReader {

    public static ArrayList<MovieItem> read(URL url) throws SAXException, IOException {
        return read(url.openStream());
    }

    public static ArrayList<MovieItem> read(InputStream stream) throws SAXException, IOException {

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            XMLReader reader = parser.getXMLReader();
            RssHandler handler = new RssHandler();
            InputSource input = new InputSource(stream);
            //input.setEncoding("ISO-8859-1");
            reader.setContentHandler(handler);
            reader.parse(input);

            return handler.getMovieList();

        } catch (ParserConfigurationException e) {
            throw new SAXException();
        }

    }

    public static ArrayList<MovieItem> read(String source) throws SAXException, IOException {
        return read(new ByteArrayInputStream(source.getBytes()));
    }
}
