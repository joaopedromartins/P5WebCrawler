package crowler;

import generated.FeedNoticias;
import generated.Noticia;
import generated.NoticiasRegiao;
import handle.XMLGregorianCalendarConversionUtil;
import handle.XmlJmsConverter;
import handle.handler;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;

import javax.jms.JMSException;
import javax.naming.NamingException;
import javax.xml.bind.JAXBException;

import jmsTopic.TopicSendClient;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WebCrawler {
	
	Logging unpublishedLog = new Logging();
	
	public static void main(String[] args) {				
			
		(new WebCrawler()).StartCrlowling();
	}
	
	

	public void StartCrlowling() {
		
		checkUnsendMessages();
		
		FeedNoticias feed = new FeedNoticias();
		feed.setData(XMLGregorianCalendarConversionUtil
				.asXMLGregorianCalendar(new Date()));
		int quant = 0;
		NoticiasRegiao europe = CrowlerCNN("http://edition.cnn.com", "/europe");
		quant += europe.getQuantidade();
		NoticiasRegiao us = CrowlerCNN("http://edition.cnn.com", "/us");
		quant += us.getQuantidade();
		NoticiasRegiao china = CrowlerCNN("http://edition.cnn.com", "/china");
		quant += china.getQuantidade();
		NoticiasRegiao asia = CrowlerCNN("http://edition.cnn.com", "/asia");
		quant += asia.getQuantidade();
		NoticiasRegiao middleeast = CrowlerCNN("http://edition.cnn.com",
				"/middle-east");
		quant += middleeast.getQuantidade();
		NoticiasRegiao africa = CrowlerCNN("http://edition.cnn.com", "/africa");
		quant += africa.getQuantidade();
		NoticiasRegiao americas = CrowlerCNN("http://edition.cnn.com",
				"/americas");
		quant += americas.getQuantidade();

		
		feed.setQuantidade(quant);
		feed.getNoticiasRegiao().add(europe);
		feed.getNoticiasRegiao().add(us);
		feed.getNoticiasRegiao().add(china);
		feed.getNoticiasRegiao().add(asia);
		feed.getNoticiasRegiao().add(middleeast);
		feed.getNoticiasRegiao().add(africa);
		feed.getNoticiasRegiao().add(americas);
		

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		Calendar dataatual = Calendar.getInstance();
		String data = dateFormat.format(dataatual.getTime());
		
		String filename = "newsteste_" + data + ".xml";
		generateXMLFile(feed, filename);
		

		if(!sendXMLFileToTopic(filename)){
			int count =0;
			System.out.println("»» Falha ao enviar publicação para o servidor");
			while(count < 5 && !sendXMLFileToTopic(filename)){
				count++;
				System.out.println(count+"ª tentativa");
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			if(count == 5){
				unpublishedLog.adicionarLinha(filename);
			}
			
		} else {
			System.out.println("Mensagem enviada com sucesso!");
			try{
				 
	    		File file = new File(filename);
	 
	    		if(file.delete()){
	    			System.out.println(file.getName() + " is deleted!");
	    		}else{
	    			System.out.println("Delete operation is failed.");
	    		}
	 
	    	}catch(Exception e){
	 
	    		e.printStackTrace();
	 
	    	}
		}
		
		
		System.out.println("--Terminado!--");
	}

	public NoticiasRegiao CrowlerCNN(String url, String path) {

		NoticiasRegiao newsAg = new NoticiasRegiao();
		newsAg.setRegiao(path.substring(1).toUpperCase());
		System.out.println("A gerar feed de noticias: " + newsAg.getRegiao());

		HashSet<String> news = getUrlNoticias(url, path);
		int count = 0;
		for (String newsUrl : news) {
			Noticia noticiaAtual = getNoticia(newsUrl);
			if(noticiaAtual != null){
				newsAg.getNoticia().add(noticiaAtual);
			} else 
				System.out.println("Noticia ignorada");
			count++;
			System.out.println(count * 100 / news.size() + "%");
		}
		newsAg.setQuantidade(newsAg.getNoticia().size());
		return newsAg;
	}

	public void generateXMLFile(FeedNoticias cnoticias, String outputfilename) {
		System.out.println("A gerar ficheiro XML: " + outputfilename);
		try {
			handler.marshal(cnoticias, new File(outputfilename), "text.xsl");
		} catch (IOException | JAXBException e) {
			System.out.println("Falha ao gerar o ficheiro XML (Marshal)");
		}
	}
	
	public void checkUnsendMessages(){
		ArrayList<String> unpublished = unpublishedLog.lerTodasAsLinha();
		ArrayList<String> remain_unpublished = new ArrayList<String>();
		if(unpublished.size() > 0){
			System.out.println(unpublished.size() + " publicações pendentes!\n"
					+ "A tentar publicar...");
			for (String string : unpublished) {			
				if(!sendXMLFileToTopic(string)){
					remain_unpublished.add(string);
				}else {
					try{
						 
			    		File file = new File(string);
			 
			    		if(file.delete()){
			    			System.out.println(file.getName() + " is deleted!");
			    		}else{
			    			System.out.println("Delete operation is failed.");
			    		}
			 
			    	}catch(Exception e){
			 
			    		e.printStackTrace();
			 
			    	}
				}
				unpublishedLog.escreverFicheiro(remain_unpublished);
			}
		}
		
	}

	public boolean sendXMLFileToTopic(String filename) {
		System.out.println("A enviar mensagem para o topico");
		String message = XmlJmsConverter.convertXMLFileToString(filename);

		try {
			TopicSendClient top = new TopicSendClient();
			
			top.send(message);
			
			return true;
		} catch (JMSException | NamingException e) {			
			System.out.println("Falha ao publicar o topico");
			return false;
		}

	}

	public Noticia getNoticia(String url) {
		Noticia noticia = new Noticia();
		Document doc = null;
		
		int count=0;
		// dangerous - at least use a counter
		while (doc == null && count < 5) {
			try {
				doc = Jsoup.connect(url).userAgent("Mozilla").timeout(10000)
						.get();
			} catch (IOException e) {
				count++;
				System.out.println("Falha ao carregar a noticia "+ count + "ª tentativa");
			}
		}
		if(count == 5){
			return null;
		}

		String titulo = doc.select("h1.pg-headline").first().text();

		// System.out.println("título: " + titulo);
		noticia.setTitulo(titulo);

		Elements metalinkssection = doc.select("meta[itemprop=articleSection]");
		String section = metalinkssection.attr("content");
		// System.out.println("Section: "+section);
		noticia.setSeccao(section);

		Elements metalinksdata = doc.select("meta[itemprop=dateModified]");
		String data = metalinksdata.attr("content");

		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		String dateInString = data;
		Date thedate = new Date();

		try {
			thedate = formatter.parse(dateInString);
			// System.out.println(thedate);
			// System.out.println(formatter.format(thedate));
			noticia.setData(handle.XMLGregorianCalendarConversionUtil
					.asXMLGregorianCalendar(thedate));

		} catch (ParseException e) {
			System.out.println("Falha ao converter a data");
		}

		// System.out.println("Data: " + thedate.toString());

		Elements metalinksurl = doc.select("meta[itemprop=url]");
		String urln = metalinksurl.attr("content");
		// System.out.println("URL: "+urln);
		noticia.setUrl(urln);

		Elements metalinksauthor = doc.select("meta[itemprop=author]");
		String author = metalinksauthor.attr("content");
		// System.out.println("Author: "+author);
		noticia.setAutor(author);

		Elements metalinksheadline = doc.select("meta[itemprop=headline]");
		String headline = metalinksheadline.attr("content");
		// System.out.println("Headline: "+headline);
		noticia.setCabecalho(headline);

		Elements metalinksdescription = doc
				.select("meta[itemprop=description]");
		String description = metalinksdescription.attr("content");
		// System.out.println("Description: "+description);
		noticia.setDescricao(description);

		Elements corpo = doc.select("p.zn-body__paragraph");
		// System.out.println(corpo.size());
		String corpotx = "";
		for (Element paragrafo : corpo) {
			corpotx += paragrafo.text() + " ";
		}
		// System.out.println("corpo: " + corpotx);
		// System.out.println();
		noticia.setCorpo(corpotx);

		Elements metalinksimage = doc.select("meta[itemprop=image]");
		String image = metalinksimage.attr("content");
		// System.out.println("image: "+image);
		noticia.setImagem(image);
		return noticia;
	}

	private HashSet<String> getUrlNoticias(String url, String reg) {
		String baseUrl = url;
		String path = reg;
		String crowlUrl = baseUrl + path;

		String newsUrl;

		HashSet<String> news = new HashSet<>();

		// data actual
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar dataatual = Calendar.getInstance();
		String datecreated = dateFormat.format(dataatual.getTime());
		// System.out.println(datecreated);

		// regiao
		String regiao = path.substring(1).toUpperCase();
		// System.out.println(regiao);

		try {
			Document doc = Jsoup.connect(crowlUrl).userAgent("Mozilla")
					.timeout(10000).get();
			Elements hrefs = doc.select("article");

			// carrega os links de todas as noticias da página
			for (Element element : hrefs) {
				newsUrl = element.attr("abs:data-vr-contentbox");
				// escolhe apenas os que são noticias de texto
				if (newsUrl.startsWith("http://edition.cnn.com/2")
						&& !newsUrl.contains("/gallery/")) {
					// System.out.println(newsUrl);
					news.add(newsUrl);
				}
			}
			// System.out.println("--------------Nº de noticias: "+news.size());
		} catch (IOException e) {
			System.out.println("Falha ao carregar a lista de noticias");
		} catch (IllegalArgumentException e) {
			System.out.println("Falha ao carregar a lista de noticias");
		}
		return news;
	}
}
