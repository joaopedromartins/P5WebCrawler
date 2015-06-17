package pt.uc.dei.aor.paj.handle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import pt.uc.dei.aor.paj.generated.FeedNoticias;

public class handler {
	
	
	
	// Export: Marshalling

	public static void marshal(FeedNoticias feed, File selectedFile, String xmlFilename) throws IOException, JAXBException {
        final FileOutputStream fileOutputStream = new FileOutputStream(selectedFile);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));

        Marshaller m = JAXBContext.newInstance(FeedNoticias.class).createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty("com.sun.xml.internal.bind.xmlHeaders", "\n<?xml-stylesheet type=\"text/xsl\" href=\"" + xmlFilename + "\" ?>\n");

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
