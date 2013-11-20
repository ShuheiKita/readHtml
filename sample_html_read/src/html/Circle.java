package html;

public class Circle {
	private String DLSiteId;
	private int id;
	private String name;
	private String site_url;

	public Circle(){

	}

	public String getDLSiteId(){
		return this.DLSiteId;
	}
	public void setDLSiteId(String id){
		this.DLSiteId = id;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSite_url() {
		return site_url;
	}

	public void setSite_url(String site_url) {
		this.site_url = site_url;
	}

	public  String toString(){
		String str = "";
		str += "\ncircleName: "+this.name;
		str += "\ncircleSite: "+this.site_url;
		return str;
	}
}
