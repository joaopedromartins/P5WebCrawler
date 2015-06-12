package crowler;

import generated.Noticia;
import generated.Noticias;
import handle.XMLGregorianCalendarConversionUtil;
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

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



public class WebCrawler {
	
	public void CrowlerCNN(String url, String path){
		
		Noticias newsAg = new Noticias();		
		newsAg.setData(XMLGregorianCalendarConversionUtil.asXMLGregorianCalendar(new Date()));
		
		HashSet<String> news = getUrlNoticias(url, path);
		
		for (String newsUrl : news) {
			Noticia n1 = getNoticia(newsUrl);
			newsAg.getNoticia().add(n1);
		}
		newsAg.setQuantidade(newsAg.getNoticia().size());
		
		try {
			handler.marshal(newsAg, new File ("newsteste.xml"));
		} catch (IOException | JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Noticia getNoticia(String url){
		Noticia n = new Noticia();
		Document doc = null;
		try {
			doc = Jsoup.connect(url)
					.timeout(10000)
					.get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			System.out.println("url: " + url);
			String titulo ="";  
			
			titulo += doc.select("h1.pg-headline").first().text();				
			System.out.println("título: " + titulo);
			n.setTitulo(titulo);
			
			Elements metalinkssection = doc.select("meta[itemprop=articleSection]");
			String section = metalinkssection.attr("content");
			System.out.println("Section: "+section);
			n.setSeccao(section);
			
			Elements metalinksdata = doc.select("meta[itemprop=dateModified]");
			String data = metalinksdata.attr("content");
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
			String dateInString = data;
			Date thedate = new Date();
		 
			try {		 
				thedate = formatter.parse(dateInString);
				System.out.println(thedate);
				System.out.println(formatter.format(thedate));
				n.setData(handle.XMLGregorianCalendarConversionUtil.asXMLGregorianCalendar(thedate));
		 
			} catch (ParseException e) {
				e.printStackTrace();
			}			
			
//			Calendar cal = Calendar.getInstance();
//			Date thedate = new Date();
//			try {
//				thedate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).parse(data);
//			} catch (ParseException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'Z");
//	
			//2015-06-11T22:58:53Z
//			try {
//				
//				cal.setTime(sdf.parse("2015-06-11T22:58:53Z" + "-0000"));
//			
//			} catch (ParseException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			System.out.println("data formatada: "+thedate.getTime());
			System.out.println("Data: " + thedate.toString());
			
			
			Elements metalinksurl = doc.select("meta[itemprop=url]");
			String urln = metalinksurl.attr("content");
			System.out.println("URL: "+urln);
			n.setUrl(urln);
			
			Elements metalinksauthor = doc.select("meta[itemprop=author]");
			String author = metalinksauthor.attr("content");
			System.out.println("Author: "+author);
			n.setAutor(author);
			
			Elements metalinksheadline = doc.select("meta[itemprop=headline]");
			String headline = metalinksheadline.attr("content");
			System.out.println("Headline: "+headline);
			n.setCabecalho(headline);
			
			Elements metalinksdescription = doc.select("meta[itemprop=description]");
			String description = metalinksdescription.attr("content");
			System.out.println("Description: "+description);
			n.setDescricao(description);
			
			Elements corpo =doc.select("p.zn-body__paragraph");
			System.out.println(corpo.size());
			String corpotx="";
			for (Element paragrafo : corpo) {
				corpotx+=paragrafo.text() + "\n";
			}
			System.out.println("corpo: " + corpotx);
			System.out.println();
			n.setCorpo(corpotx);
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
		System.out.println(datecreated);
		
		//regiao
		String regiao = path.substring(1).toUpperCase();
		System.out.println(regiao);

		try {
			Document doc = Jsoup.connect(crowlUrl)
					.timeout(10000)
					.get();
			Elements hrefs = doc.select("article");
			
			//carrega os links de todas as noticias da página
			for (Element element : hrefs) {
				newsUrl = element.attr("abs:data-vr-contentbox");
				//escolhe apenas os que são noticias de texto
				if(newsUrl.startsWith("http://edition.cnn.com/2")){
					System.out.println(newsUrl);
					news.add(newsUrl);
				}				
			}
			System.out.println("--------------Nº de noticias: "+news.size());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return news;
	}

	public static void main(String[] args) {
		new WebCrawler().CrowlerCNN("http://edition.cnn.com", "/europe");
		
	}

}
