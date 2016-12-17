package classify;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import org.geotools.geometry.jts.JTSFactoryFinder;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import flickrdownload.Utils;


public class ClassifyUtils {
	/** 
	 * ���ָ����Χ��N�����ظ����� 
	 * ����HashSet��������ֻ�ܴ�Ų�ͬ��ֵ 
	 * @param min ָ����Χ��Сֵ 
	 * @param max ָ����Χ���ֵ 
	 * @param n ��������� 
	 * @param HashSet<Integer> set ���������� 
	 */  
	   public static void randomSet(int min, int max, int n, HashSet<Integer> set) {  
	       if (n > (max - min + 1) || max < min) {  
	           return;  
	       }  
	       for (int i = 0; i < n; i++) {  
	           // ����Math.random()����  
	           int num = (int) (Math.random() * (max - min)) + min;  
	           set.add(num);// ����ͬ��������HashSet��  
	       }  
	       int setSize = set.size();  
	       // ����������С��ָ�����ɵĸ���������õݹ�������ʣ�����������������ѭ����ֱ���ﵽָ����С  
	       if (setSize < n) {  
	        randomSet(min, max, n - setSize, set);// �ݹ�  
	       }  
	   }  

	   public static int getrandom(int min,int max)
	   {
		   int num =  (int)(Math.random()*(max-min));
		   System.out.println("random="+num +" max="+max);
		   return num;
	   }
	   

	  
	   
	   public static FlickrPoint[] getFlickrPointsfromdatabase(String loaction) throws Exception
	   {
		   Connection connection = flickrdownload.DataBase.GetPostgresqlConnection();
		   Statement statement = connection.createStatement();
		   //String sql = "select ST_AsText(geom),id from "+flickrdownload.DataBase.tablename+" where sign = '"+loaction+"'"
		   		//+ " and datetaken between '2012-01-01 00:00:00' and '2016-01-01 00:00:00';";
		   String sql = "select ST_AsText(geom),id,datetaken from "+flickrdownload.DataBase.tablename+" where sign = '"+loaction+"';";
		   ResultSet rs = statement.executeQuery(sql);
		   ArrayList<FlickrPoint> list = new ArrayList<>();
		   double lo_max = Double.MIN_VALUE,la_max = Double.MIN_VALUE;
		   double lo_min = Double.MAX_VALUE,la_min = Double.MIN_VALUE;
		   while(rs.next())
		   {
			   Point p = Utils.parsepointfromwkt(rs.getString(1));
			   /*if(p.getX()<lo_min) lo_min = p.getX();
			   if(p.getX()>lo_max) lo_max = p.getX();
			   if(p.getY()<la_min) la_min = p.getY();
			   if(p.getY()>la_max) la_max = p.getY();*/
			   Date date = rs.getDate(3);
			   list.add(new FlickrPoint(rs.getString(2),p.getX(), p.getY(),date));
		   }
		   System.out.println("input data successfully!");
		  /* FlickrPoint[] points = new FlickrPoint[list.size()];
		   for(int i = 0;i<points.length;i++)
		   {
			   FlickrPoint tempp = list.get(i);
			   points[i] = new FlickrPoint(tempp.ID,(tempp.lon-lo_min)*100/(lo_max-lo_min), (tempp.lat-la_min)*100/(la_max-la_min));
		   }*/
		   return  list.toArray(new FlickrPoint[]{});
	   }
}
