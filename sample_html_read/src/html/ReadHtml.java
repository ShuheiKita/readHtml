package html;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ReadHtml{
	private static long start;
	private static long end;
	private static int sleep = 300;
	private static Calendar NOW;
	private static int years = 5;

	//private static final String URL = "http://www.dlsite.com/maniax/";
	private static final int timeout = 120000;


	public static void main(String[] args){

		Calendar now = Calendar.getInstance();
		//now.add(Calendar.DAY_OF_MONTH, -10);
		//String url = "http://www.dlsite.com/maniax/";
		/*
		String path = "http://www.dlsite.com/maniax/new/=/year/"+now.get(now.YEAR)+"/mon/"
				+(now.get(now.MONTH)+1)+"/day/"+(now.get(now.DATE)-1);
		*/

		//String path="http://www.dlsite.com/maniax/new/=/year/2013/mon/5/day/20";

		//System.out.println(path);
		//read(path);

		int index = 0;
		start = new Date().getTime();
		System.out.println("START-----"+new Date());
		while(index++ < 365*years){
			//debug(now);
			try{
				readWorks(now);
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(now);
			}
			now.add(Calendar.DAY_OF_MONTH, -1);
		}
		end = new Date().getTime();
		System.out.println("END----"+new Date());
		int hour = (int) ((end-start)/3600000);
		int min = (int) (((end-start)-hour*3600000)/60000);
		System.out.println( hour+"hour "+min+"min\n\n" );
		
		sleepTime(3600000*3);
		main(args);

	}
	
	public static void debug(Calendar cal){
		String path = "http://www.dlsite.com/maniax/new/=/year/"+cal.get(Calendar.YEAR)+"/mon/"+(cal.get(Calendar.MONTH)+1)+"/day/"+(cal.get(Calendar.DATE))+"/cyear/2013/cmon/6";
		try {
			Connection con = Jsoup.connect(path);
			con.request().timeout(timeout);
		    Document document = con.get();
		    List<Element> table = document.getElementsByClass("work_1col_table");

		    List<Element> works = table.get(0).getElementsByTag("tr");

		    for(Element work : works){
		    	try{
		    		getPrice(work);
		    	}catch(Exception e){
		    		e.printStackTrace();
		    		System.out.println(work);
		    	}
		    }
		}catch (Exception e) {
		    e.printStackTrace();
		}
	}



	public static void readWorks(Calendar cal) throws Exception{
		NOW = Calendar.getInstance();
		String path = "http://www.dlsite.com/maniax/new/=/year/"+cal.get(Calendar.YEAR)+"/mon/"+(cal.get(Calendar.MONTH)+1)+"/day/"+(cal.get(Calendar.DATE))+"/cyear/2013/cmon/6";
		//System.out.print(path);
		//String path = "./data/works.html";
		
		int count = 0;
		int already = 0;
		try {
			Connection con = Jsoup.connect(path);
			con.request().timeout(timeout);
		    Document document = con.get();

		    //Document document = Jsoup.parse(new File(path), "utf-8");
			//System.out.println(document);
		    
		    if(document.getElementById("error_box")!=null){
		    	sleepTime(sleep);
		    	readWorks(cal);
		    	return ;
		    }

		    List<Element> table = document.getElementsByClass("work_1col_table");

		    List<Element> works = table.get(0).getElementsByTag("tr");

		    for(Element work : works){
		    	if(work.getElementsByClass("work_1col_thumb").size()==0) continue;

		    	Work obj = new Work();

		    	//日付
		    	obj.setDate(cal);

		    	//作品名取得
		    	obj.setName( getName(work) );

		    	//サークル名
		    	Circle circle = new Circle();
		    	circle.setName( getCircleName(work) );
		    	obj.setCircle(circle);

		    	//サークルが登録済みか (サークルID設定)
		    	obj.getCircle().setId( ConnectorDB.isCircleId(obj) );
		    	//ワークテーブルがあるか (ワークテーブル作成)
		    	//ConnectorDB.isWorkTable( obj.getDateYear() );
		    	

		    	//DL数
		    	Dl dl = new Dl();
				dl.setCal(NOW);
				dl.setDl( getDl(work) );
				obj.setDl( new Dl[]{dl} );


				//作品が登録済みか調べる
				//登録済みの場合、処理終了 
		    	obj.setId( ConnectorDB.isWorkId(obj) );
		    	if(obj.getId() > 0){
		    		if(ConnectorDB.isDlTable(obj)){
		    			if(ConnectorDB.isDlQuery(obj, NOW)){
		    				ConnectorDB.updateDl(obj, NOW);
		    				ConnectorDB.updateDl_work(obj);
		    				already++;
		    				continue;
		    			}else{
		    				ConnectorDB.insertDl(obj, NOW);
		    				ConnectorDB.updateDl_work(obj);
		    				count++;
		    				continue;
		    			}

		    		}
		    	}
		    	

		    	//URL取得
		    	obj.setUrl( getUrl(work) );

		    	//サムネ取得
		    	obj.getImages().setThumb( getImageThunb(work) );


		    	//説明文（短）取得
		    	obj.setExp_short( getExpShort(work) );

		    	//価格取得
		    	obj.setPrice( getPrice(work) );

		    	//作品ページからデータ取得
		    	readWork(obj, cal);


		    	/*
		    	 * ToDo
		    	 * Workがきれいに作られている場合、DBにセット。
		    	 */
		    	if(obj.isPerfect()){
		    			ConnectorDB.updateCircle(obj);
		    			ConnectorDB.insertWork(obj);
		    			obj.setId( ConnectorDB.isWorkId(obj) );
			    		ConnectorDB.insertDl(obj, NOW);
			    		ConnectorDB.insertFields(obj);
			    		
			    		count++;

		    	}
		    	if(!obj.isPerfect()){
		    		System.out.println("Not Perfect----------------------------");
		    		System.out.println(obj);
		    	}

		    	//sleep
		    	sleepTime(sleep);

		    	//break;
		    }
		    //System.out.println("  work num:"+count+"  already num:"+already);
		}catch(HttpStatusException e){ 
			sleepTime(sleep);
	    	throw e;
		}catch (Exception e) {
		    e.printStackTrace();
		    throw e;
		}
	}


	public static void readWork(Work work, Calendar cal) throws Exception{
		String url = work.getUrl();
		
		//DLSiteID
		work.setDlSiteId(getWorkSiteId(url));
		
		//String url = "./data/work.html";
		try {
			Connection con = Jsoup.connect(url);
			con.request().timeout(timeout);
		    Document document = con.get();

		    //Document document = Jsoup.parse(new File(url), "utf-8");
		    /*
		    if(document.getElementById("error_box")!=null){
		    	sleepTime(sleep);
		    	readWork(work, cal);
		    	return ;
		    }
		    */

			Element body = document.body();

			//成人向け作品かどうか
			work.setAdult( getAdult(body) );

			//メイン画像
			work.getImages().setMain(getImageMain(body));

			//サンプル
			work.getImages().setSamples( getSamples(body) );

			//サークルサイト
			work.getCircle().setSite_url( getCircleSite(body) );
			//サークルDLSiteID
	    	work.getCircle().setDLSiteId(getCircleDLSiteId(body));

			//説明文（長）
			work.setExp_long( getExpLong(body) );

			//ジャンル
			work.setCategory( getCategory(body) );

			//作品形式
			work.setForm( getForm(body) );



		}catch(HttpStatusException e){ 
			sleepTime(sleep);
	    	readWork(work, cal);
	    	throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * 作品名取得　＠作品一覧.
	 * @param work
	 * @return
	 */
	private static String getName(Element work) throws Exception{
		try{
			Elements tmp_name = work.getElementsByClass("work_name");
	    	if(tmp_name==null || tmp_name.size()==0) return null;
	    	Element name = tmp_name.get(0).child(0);
	    	if(name==null) return null;
			return name.text();
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * 作品ページURL取得　＠作品一覧.
	 * @param work
	 * @return
	 */
	private static String getUrl(Element work) throws Exception{
		try{
			Elements tmp = work.getElementsByClass("work_thumb");
	    	if(tmp==null || tmp.size()==0) return null;
	    	Element thumb = tmp.get(0).child(0);
	    	if(thumb==null) return null;
	    	//work page url
	    	return thumb.attr("href");
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * サムネ画像取得 ＠作品一覧.
	 * @param work
	 * @return サムネ画像のURL
	 */
	private static String getImageThunb(Element work) throws Exception{
		try{
			Elements tmp = work.getElementsByClass("work_thumb");
			if(tmp==null || tmp.size()==0) return "http://www.dlsite.com/images/web/home/no_img_sam.gif";
			Element thumb = tmp.get(0).child(0);
			Elements tmp1 = thumb.getElementsByTag("img");
			if(tmp1==null) return "http://www.dlsite.com/images/web/home/no_img_sam.gif";
	    	//thumbnail url
	    	String img = tmp1.get(0).attr("src") ;
	    	if("/images/web/home/no_img_sam.gif".equals(img)){
	    		return "http://www.dlsite.com/images/web/home/no_img_sam.gif";
	    	}
	    	return "http:"+img;
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	/**
	 * 価格取得 @作品一覧.
	 * @param work
	 * @return
	 */
	private static int getPrice(Element work) throws Exception{
		try{
			int price = 0;
			Elements tmp = work.getElementsByClass("work_price");
			if(tmp==null || tmp.size()==0) return 0;
			int index = tmp.get(0).text().indexOf("/");
			String str = tmp.get(0).text().substring(0, index-1);
			price = Integer.parseInt(str.replaceAll(",", ""));
			
			return price;
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * 説明文(短)取得.
	 * @param work
	 * @return
	 */
	private static String getExpShort(Element work) throws Exception{
		try{
			Elements txts = work.getElementsByClass("work_1col");
	    	if(txts==null || txts.size()==0) return null;
	    	return  txts.get(0).getElementsByClass("work_text").get(0).html();
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * メイン画像取得 ＠作品ページ.
	 * @param body
	 * @return メイン画像のURL
	 */
	private static String getImageMain(Element body) throws Exception{
		try{
			Element tmp = body.getElementById("work_visual");
			if(tmp==null) return "http://www.dlsite.com/images/web/home/no_img_main.gif";
			String img = tmp.getElementsByTag("img").get(0).attr("src");
			if("/images/web/home/no_img_main.gif".equals(img)){
				return  "http://www.dlsite.com/images/web/home/no_img_main.gif";
			}
			return "http:"+img;
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * サークル名取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String getCircleName(Element body) throws Exception{
		try{
			Element circle = body.getElementsByClass("maker_name").get(0);
			Element name = circle.getElementsByTag("a").get(0);
			return name.text();
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * サークルDLSiteID取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String getCircleDLSiteId(Element body) throws Exception{
		try{
			List<Element> tmp = body.getElementsByClass("add_mygenre");
			if(tmp.size()==0) return null;
			String utl = tmp.get(0).getElementsByTag("a").get(0).attr("href");
			String id = matche(utl, ".*/(.*).html.*");
			return id;
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	/**
	 * サークルホームページ取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String getCircleSite(Element body) throws Exception{
		try{
			if(body.getElementById("work_maker")==null) System.out.println(body);
			List<Element> tmp = body.getElementById("work_maker").getElementsByTag("tr");
			if(tmp.size()<=1 || tmp.get(1).getElementsByTag("a").size()==0) return null;
			return tmp.get(1).getElementsByTag("a").get(0).text();
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * ダウンロード数取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static int getDl(Element body) throws Exception{
		String str = "";
		try{
			Element dl = body.getElementsByClass("work_dl").get(0);
			List<Element> tmp = dl.getElementsByTag("span");
			if(tmp.size()==0) return 0;
			str = tmp.get(0).html();
			return Integer.parseInt(str);
		}catch(NumberFormatException e){
			if(str.matches(".*円")){
				str = str.substring(0, str.length()-1);
				return Integer.parseInt(str);
			}else{
				return 0;
			}
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * 説明文（長）取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String getExpLong(Element body) throws Exception{
		String str = "";
		try{
			str = body.getElementsByClass("work_story").get(0).text();
		//String str = body.getElementsByClass("work_article").get(0).text();
		}catch(IndexOutOfBoundsException e){
			return null;
		}catch(Exception e){
			throw new Exception(e);
		}
		return str;
	}

	/**
	 * サンプル画像取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String[] getSamples(Element body) throws Exception{
		try{
			Element tmp = body.getElementById("work_sample");
			if(tmp==null) return null;
			List<Element> samples = tmp.getElementsByTag("a");
			if(samples.size()>0){
				List<String> list = new ArrayList<String>();
				for(Element el : samples){
					list.add( "http:"+el.attr("href") );
				}
				return list.toArray(new String[0]);
			}
		}catch(Exception e){
			throw new Exception(e);
		}
		return null;
	}

	/**
	 * 成人向けか否か　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static boolean getAdult(Element body) throws Exception{
		try{
			if(body.getElementsByClass("icon_ADL").size()==0) return false;
			return true;
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * ジャンル取得　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String[] getCategory(Element body) throws Exception{
		try{
			List<Element> list = body.getElementsByClass("main_genre").get(0).getElementsByTag("a");
			if(list.size()==0)
				return null;
			List<String> categorys = new ArrayList<String>();
			for(Element category : list){
				categorys.add(category.text());
			}
			return categorys.toArray(new String[0]);
		}catch(Exception e){
			throw new Exception(e);
		}
	}

	/**
	 * 作品形式　＠作品ページ.
	 * @param body
	 * @return
	 */
	private static String[] getForm(Element body)  throws Exception{
		try{
			List<Element> list = body.getElementsByClass("work_genre").get(1).getElementsByTag("span");
			if(list.size()==0)
				return null;
			List<String> forms = new ArrayList<String>();
			for(Element form : list){
				forms.add(form.attr("title"));
			}
			return forms.toArray(new String[0]);
		}catch(Exception e){
			throw new Exception(e);
		}
	}
	
	/**
	 * 作品ID　DLSite独自　＠作品ページ
	 * @param body
	 * @return
	 */
	private static String getWorkSiteId(String url){
		return matche(url, ".*/(.*).html");
	}
	
	/**
	 * パターンマッチ文字取得.
	 * @param target
	 * @param str
	 * @return
	 */
	private static String matche(String target, String str){
		String result = null;
		Pattern p = Pattern.compile("str");
		Matcher m = p.matcher(target);
		if(m.find()){
			result = m.group();
		}
		return null;
	}

	private static void sleepTime(int time){
		try{
			Thread.sleep(time);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * ログ出力用.
	 * @param target
	 * @param path
	 */
	private static void log(String target, String path){
		try {
			PrintWriter pw = new PrintWriter(new File(path), "UTF-8");
			pw.print(target);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
