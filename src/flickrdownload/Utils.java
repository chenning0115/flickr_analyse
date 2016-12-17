package flickrdownload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;

import javax.swing.text.DateFormatter;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class Utils {
	public static String BasePath = "f:/flickr_beijing/";

	public static BlockingQueue<String> savepaths;
	public static LinkedList<String> linkedList;
	public static Date startdate;
	public static Date enddate;
	public static int  inter_hour = 2;
	public static PrintWriter writer;
	public static String baiduapikey = "lmCO1xRKxv8UCYGgpUGijx6A";
	public static int Threadnum = 2000;
	
	static
	{
		try {
			writer = new PrintWriter(new FileWriter(new File("e:/flickrmetadata.txt")));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static Point parsepointfromwkt(String wkt_point)throws Exception
	{
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	    WKTReader reader = new WKTReader(geometryFactory);
	    Point p = (Point)reader.read(wkt_point);
	    return p;
	}
	
	public static String convertencoding(String data,String oldencode,String Toencode)
	{
		try{
			byte[] bytedata = data.getBytes(oldencode);
			return new String(bytedata,Toencode);
		}catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
		
	}
	
	
	
}
