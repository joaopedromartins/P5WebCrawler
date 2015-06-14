package handle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import generated.FeedNoticias;

public class handler {
	
	// Export: Marshalling
    public static void marshal(FeedNoticias feed, File selectedFile, String xmlFilename)
            throws IOException, JAXBException {
        JAXBContext context;
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(selectedFile));
        context = JAXBContext.newInstance(FeedNoticias.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty("com.sun.xml.internal.bind.xmlHeaders",
        		"<?xml-stylesheet type=\"text/xsl\" href=\"" + xmlFilename + "\" ?>\n");
        m.marshal(feed, writer);
        writer.close();
    }
 
    // Import: Unmarshalling
    public static FeedNoticias unmarshal(File importFile) throws JAXBException {
        FeedNoticias feed = null;
        JAXBContext context;
 
        context = JAXBContext.newInstance(FeedNoticias.class);
        Unmarshaller um = context.createUnmarshaller();
        feed = (FeedNoticias) um.unmarshal(importFile);
 
        return feed;
    }

}
