package crowler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Logging {
	
	private static final String logname = "myFile.log";
	
//	public static void main(String[] args) {
//		
//		adicionarLinha("linha1");
//		adicionarLinha("linha2");
//		adicionarLinha("linha3");
//		adicionarLinha("linha4");
//		
//		ArrayList<String> t = new ArrayList<String>();
//		t.add("nova1");
//		t.add("nova1");
//		t.add("nova1");
//		t.add("nova1");
//		t.add("nova1");
//		
//		escreverFicheiro(t);
//		
//		for (String string : lerTodasAsLinha()) {
//			System.out.println(string);
//		}
//
//	}
	
	public void adicionarLinha(String texto){
		System.out.println("adicionar a linha "+texto);
		try {
			
			File file = new File(logname);
			
			if (!file.exists()) {
				file.createNewFile();
			}
			
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.append(texto + "\n");
			bw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void escreverFicheiro(ArrayList<String> texto){
		try {

			File file = new File(logname);

			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			String buffer="";
			for (String string : texto) {
				buffer+=string + "\n";
			}
			
			bw.append(buffer);
			bw.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public ArrayList<String> lerTodasAsLinha(){
		ArrayList<String> read = new ArrayList<>();
		try {
			File file = new File(logname);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				read.add(line);
			}
			fileReader.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return read;
	}

}
