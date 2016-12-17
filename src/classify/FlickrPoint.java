package classify;

import java.util.Date;

public class FlickrPoint {

	public  String ID;
	public  double lat;
	public  double lon;
	public Date datetaken;
	
	public FlickrPoint(String id,double lo,double la,Date _datetaken)
	{
		this.ID = id;
		this.lat = la;
		this.lon = lo;
		this.datetaken = _datetaken;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
