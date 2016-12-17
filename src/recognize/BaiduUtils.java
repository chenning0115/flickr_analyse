package recognize;


import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.apache.xml.resolver.helpers.PublicId;
import org.geotools.wfs.v2_0.bindings.ReturnFeatureTypesListTypeBinding;
import org.json.JSONObject;
import org.json.JSONWriter;

import classify.ClassifyResult;
import classify.FlickrPoint;
import flickrdownload.PhotoPage.FlickPhoto;

public class BaiduUtils {

	//	WGS84������������ϵ
	public static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
	public static double PI = 3.14159265358979324;
	public static double transformLat(double x,double y)
	{
		double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * PI) + 40.0 * Math.sin(y / 3.0 * PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * PI) + 320 * Math.sin(y * PI / 30.0)) * 2.0 / 3.0;
        return ret;
	}
	public static double transformLon(double x,double y)
	{
		double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * PI) + 20.0 * Math.sin(2.0 * x * PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * PI) + 40.0 * Math.sin(x / 3.0 * PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * PI) + 300.0 * Math.sin(x / 30.0 * PI)) * 2.0 / 3.0;
        return ret;
	}
	public static FlickrPoint delt(double lat,double lon)
	{
		double a = 6378245.0; //  a: 卫星椭球坐标投影到平面地图坐标系的投影因子。
		double  ee = 0.00669342162296594323; //  ee: 椭球的偏心率。
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 *PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * PI);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * PI);
        return new FlickrPoint(null, dLon, dLat, null);
       
	}
	public static FlickrPoint bd_encrypt1(FlickrPoint ggLatLng)  
    {  
        double x = ggLatLng.lon, y = ggLatLng.lat;  
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);  
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        FlickrPoint point_new = new FlickrPoint(ggLatLng.ID, z * Math.cos(theta) + 0.0065 , z * Math.sin(theta) + 0.006,ggLatLng.datetaken);
        return point_new;
    }  
	public static FlickrPoint bd_encrypt(FlickrPoint p)
	{
		double Lat = p.lat;
		double Lon = p.lon;
		FlickrPoint p1 = delt(Lat, Lon);
		double gcjLat =  Lat + p1.lat;
		double gcjLon = Lon + p1.lon;
		
		double x = gcjLon, y = gcjLat;  
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);  
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);  
        double bdLon = z * Math.cos(theta) + 0.0065;  
        double bdLat = z * Math.sin(theta) + 0.006;
        return new FlickrPoint(null, bdLon, bdLat, null);
	}
	public static void getbdjson(ClassifyResult result)
	{
		try{
			FlickrPoint[] centers = result.centerpoints;
			BaiduPOIBaseInfo[] infos = result.centerinfo;
			ByteArrayOutputStream outputStream1 = new ByteArrayOutputStream();
			PrintWriter writer1 = new PrintWriter(new OutputStreamWriter(outputStream1,"UTF-8"));
			JSONWriter jwriter1 = new JSONWriter(writer1);
			jwriter1.object().array();
			
			ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
			PrintWriter writer2 = new PrintWriter(new OutputStreamWriter(outputStream2,"UTF-8"));
			JSONWriter jwriter2 = new JSONWriter(writer2);
			jwriter2.object().array();
			for(int i=0;i<centers.length;i++)
			{
				FlickrPoint p1 = centers[i];
				BaiduPOIBaseInfo info = infos[i];
				FlickrPoint p2 = bd_encrypt(p1);
				jwriter1.array().value(p1.lon).value(p1.lat).value(i+","+info.poi_name); 
				if (info.poi_name.equals("notrec"))
				{
					jwriter2.array().value(p2.lon).value(p2.lat).value(i); 
				}
			}
			jwriter1.endArray();
			writer1.flush();
			
			jwriter2.endArray();
			writer2.flush();
			
			String str_result1 = outputStream1.toString("UTF-8");
			outputStream1.flush();
			System.out.println(str_result1);
			
			String str_result2 = outputStream2.toString("UTF-8");
			outputStream2.flush();
			System.out.println(str_result2);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
