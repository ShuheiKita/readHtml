package html;

public class SampleImage {
	private String main;
	private String thumb;
	private String[] samples;

	public SampleImage(){

	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	public String getThumb() {
		return thumb;
	}

	public void setThumb(String thumb) {
		this.thumb = thumb;
	}

	public String[] getSamples() {
		return samples;
	}

	public void setSamples(String[] samples) {
		this.samples = samples;
	}


	public String toString(){
		String str = "";
		str += "\nmain: "+this.main;
		str += "\nthunb: "+this.thumb;
		if(this.samples!=null)
		for(String img: this.samples){
			str += "\nsample: "+img;
		}
		return str;
	}
}
