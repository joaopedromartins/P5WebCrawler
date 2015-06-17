package pt.uc.dei.aor.paj.handle;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;



public class XmlJmsConverter {
	
	public final static int BUFFER = 100*1024*1024;
	
	public static String convertXMLFileToString(String fileName) throws IOException 
	{ 
		File xml = new File(fileName);
		
		FileReader fr = new FileReader(xml);
		char[] cbuf = new char[BUFFER];
		
		int n = fr.read(cbuf);
		fr.close();
		String s = new String(cbuf,0,n);
		return s;
		
	}
	
	

}
