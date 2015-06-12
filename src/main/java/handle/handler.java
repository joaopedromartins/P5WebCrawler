package handle;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import generated.Noticias;

public class handler {
	
	// Export: Marshalling
    public static void marshal(Noticias noticias, File selectedFile)
            throws IOException, JAXBException {
        JAXBContext context;
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(selectedFile));
        context = JAXBContext.newInstance(Noticias.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(noticias, writer);
        writer.close();
    }
 
    // Import: Unmarshalling
    public static Noticias unmarshal(File importFile) throws JAXBException {
        Noticias noticias = null;
        JAXBContext context;
 
        context = JAXBContext.newInstance(Noticias.class);
        Unmarshaller um = context.createUnmarshaller();
        noticias = (Noticias) um.unmarshal(importFile);
 
        return noticias;
    }

}
