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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import javax.naming.NamingException;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import jms.Sender;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class WebCrawler {
	
	public static void main(String[] args) {
		new WebCrawler().StartCrlowling();		
	}
	
	public void StartCrlowling(){
		FeedNoticias feed = new FeedNoticias();
		feed.setData(XMLGregorianCalendarConversionUtil.asXMLGregorianCalendar(new Date()));
		int quant = 0;
		NoticiasRegiao europe = CrowlerCNN("http://edition.cnn.com", "/europe");
			quant += europe.getQuantidade();
		NoticiasRegiao us = CrowlerCNN("http://edition.cnn.com", "/us");
			quant += us.getQuantidade();
		NoticiasRegiao china = CrowlerCNN("http://edition.cnn.com", "/china");
			quant += china.getQuantidade();
		NoticiasRegiao asia = CrowlerCNN("http://edition.cnn.com", "/asia");
			quant += asia.getQuantidade();
		NoticiasRegiao middleeast = CrowlerCNN("http://edition.cnn.com", "/middle-east");
			quant += middleeast.getQuantidade();
		NoticiasRegiao africa = CrowlerCNN("http://edition.cnn.com", "/africa");
			quant += africa.getQuantidade();
		NoticiasRegiao americas = CrowlerCNN("http://edition.cnn.com", "/americas");
			quant += americas.getQuantidade();
		
		feed.setQuantidade(quant);
		feed.getNoticiasRegiao().add(europe);
		feed.getNoticiasRegiao().add(us);
		feed.getNoticiasRegiao().add(china);
		feed.getNoticiasRegiao().add(asia);
		feed.getNoticiasRegiao().add(middleeast);
		feed.getNoticiasRegiao().add(africa);
		feed.getNoticiasRegiao().add(americas);
		
		generateXMLFile(feed, "newsteste.xml");
		sendXMLFileToTopic("newsteste.xml");
	}
	
	public NoticiasRegiao CrowlerCNN(String url, String path){
		
		NoticiasRegiao newsAg = new NoticiasRegiao();		
		newsAg.setRegiao(path.substring(1).toUpperCase());

		
		HashSet<String> news = getUrlNoticias(url, path);
		
		for (String newsUrl : news) {
			Noticia n1 = getNoticia(newsUrl);
			newsAg.getNoticia().add(n1);
		}
		newsAg.setQuantidade(newsAg.getNoticia().size());
		return newsAg;
	}
	
	public void generateXMLFile(FeedNoticias cnoticias, String outputfilename){
		try {
			handler.marshal(cnoticias, new File (outputfilename), "text.xsl");
		} catch (IOException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void sendXMLFileToTopic(String filename){
		
		String message = XmlJmsConverter.convertXMLFileToString(filename);
		
		try {
			
			Sender.main(null, message);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	public Noticia getNoticia(String url){
		Noticia n = new Noticia();
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					.userAgent("Mozilla")
					.timeout(10000)
					.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//			System.out.println("url: " + url);
			String titulo ="";  
			
			titulo += doc.select("h1.pg-headline").first().text();				
//			System.out.println("título: " + titulo);
			n.setTitulo(titulo);
			
			Elements metalinkssection = doc.select("meta[itemprop=articleSection]");
			String section = metalinkssection.attr("content");
//			System.out.println("Section: "+section);
			n.setSeccao(section);
			
			Elements metalinksdata = doc.select("meta[itemprop=dateModified]");
			String data = metalinksdata.attr("content");
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateInString = data;
			Date thedate = new Date();
		 
			try {		 
				thedate = formatter.parse(dateInString);
//				System.out.println(thedate);
//				System.out.println(formatter.format(thedate));
				n.setData(handle.XMLGregorianCalendarConversionUtil.asXMLGregorianCalendar(thedate));
		 
			} catch (ParseException e) {
				e.printStackTrace();
			}			
			
//			System.out.println("Data: " + thedate.toString());
			
			
			Elements metalinksurl = doc.select("meta[itemprop=url]");
			String urln = metalinksurl.attr("content");
//			System.out.println("URL: "+urln);
			n.setUrl(urln);
			
			Elements metalinksauthor = doc.select("meta[itemprop=author]");
			String author = metalinksauthor.attr("content");
//			System.out.println("Author: "+author);
			n.setAutor(author);
			
			Elements metalinksheadline = doc.select("meta[itemprop=headline]");
			String headline = metalinksheadline.attr("content");
//			System.out.println("Headline: "+headline);
			n.setCabecalho(headline);
			
			Elements metalinksdescription = doc.select("meta[itemprop=description]");
			String description = metalinksdescription.attr("content");
//			System.out.println("Description: "+description);
			n.setDescricao(description);
			
			Elements corpo =doc.select("p.zn-body__paragraph");
//			System.out.println(corpo.size());
			String corpotx="";
			for (Element paragrafo : corpo) {
				corpotx+=paragrafo.text() + " ";
			}
//			System.out.println("corpo: " + corpotx);
//			System.out.println();
			n.setCorpo(corpotx);
			
			Elements metalinksimage = doc.select("meta[itemprop=image]");
			String image = metalinksimage.attr("content");
//			System.out.println("image: "+image);
			n.setImagem(image);
		return n;
	}
	
	private HashSet<String> getUrlNoticias(String url, String reg){
		String baseUrl = url;
		String path = reg;
		String crowlUrl =baseUrl + path;
		
		String newsUrl;

		HashSet<String> news = new HashSet<>(); 
		
		//data actual
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Calendar dataatual = Calendar.getInstance();
		String datecreated=dateFormat.format(dataatual.getTime());
//		System.out.println(datecreated);
		
		//regiao
		String regiao = path.substring(1).toUpperCase();
//		System.out.println(regiao);

		try {
			Document doc = Jsoup.connect(crowlUrl)
					.userAgent("Mozilla")
					.timeout(10000)
					.get();
			Elements hrefs = doc.select("article");
			
			//carrega os links de todas as noticias da página
			for (Element element : hrefs) {
				newsUrl = element.attr("abs:data-vr-contentbox");
				//escolhe apenas os que são noticias de texto
				if(newsUrl.startsWith("http://edition.cnn.com/2") && !newsUrl.contains("/gallery/")){
//					System.out.println(newsUrl);
					news.add(newsUrl);
				}				
			}
//			System.out.println("--------------Nº de noticias: "+news.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return news;
	}
}
