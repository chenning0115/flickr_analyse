package classify;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.geotools.geometry.iso.io.wkt.GeometryToWKTString;
import org.geotools.util.InterpolationConverterFactory;
import org.omg.CORBA.PUBLIC_MEMBER;

import flickrdownload.DataBase;
import opendap.dap.test.expr_test;
import ucar.nc2.iosp.nexrad2.Level2Record;

/*
 * ���������ݲ�ĳ�����Ҫ����Ϊ���������㷨�ṩ����֧��
 * ����ԭʼ�����ݵ��ṩ�������ܶ�ʱ��ʱ�洢�����table
 * Ϊ�㷨�ṩ��ȡ��������ܶȵķ���
 */
public class DataSupporter {

	public FlickrPoint[] points;
	//public double[][] distable;
	public IGetDistance getDistanceimpl =  new GetDistanceImp2();
	
	public DataSupporter(String location,boolean able_distable)
	{
		try{ 
			points = ClassifyUtils.getFlickrPointsfromdatabase(location);
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/*
	 * ����¼����ֻ���������ݿ���м���
	 */
	public double GetDistance(FlickrPoint p1,FlickrPoint p2)
	{
		if(p1==null||p2==null) return -1;
		double val =getDistanceimpl.GetDistance(p1, p2);
		//System.out.println(val);
		return val;
	}
	/*
	 * �Ȳ����Ƿ��Ѿ��л���
	 */
	public double GetDistance(int i1,int i2)
	{
		return GetDistance(points[i1], points[i2]);
	}
	
	
	public class GetDistanceImp2 implements IGetDistance
	{

		@Override
		public double GetDistance(FlickrPoint p1, FlickrPoint p2) {
			// TODO Auto-generated method stub
			double detlon = p1.lon - p2.lon;
			double detlat = p1.lat - p2.lat;
			double valbase = detlon*detlon + detlat*detlat;
			return valbase;
		}
		
	}
	
	
	public class GetDistanceImpl implements IGetDistance
	{
		public double scale_1 = 5.0;
		public double scale_2 = 3.0;
		public double scale_3 = 2.0;

		@Override
		public double GetDistance(FlickrPoint p1, FlickrPoint p2) {
			// TODO Auto-generated method stub
			Connection connection = null;
			 Statement statement = null;
			try{
				 connection = flickrdownload.DataBase.GetPostgresqlConnection();
				 statement = connection.createStatement();
				 String sql = "select secetype from "+DataBase.table_roadname+" where (ST_Intersects("+DataBase.table_roadname+".geom,ST_GeomFromText('LINESTRING("+p1.lon+" "+p1.lat+","+p2.lon+" "+p2.lat+")',4326))) = 'TRUE';";
				 //System.out.println(sql);
				 ResultSet rs = statement.executeQuery(sql);
				 int num_1=0,num_2=0,num_3 = 0;
				 while(rs.next())
				 {
					 switch(rs.getString(1)){
					 case "主干路": num_1++;break;
					 case "快速路": num_2++;break;
					 case "高速路": num_3++;break;
					 }
				 }
				 
				 double detlon = p1.lon - p2.lon;
				 double detlat = p1.lat - p2.lat;
				 double valbase = detlon*detlon + detlat*detlat;
				 int count = num_1+num_2+num_3;
				 if(count==0) return valbase;
				 else return valbase+0.00005;
				 //System.out.println(valbase);
				 //return valbase;
				 //return (detlon*detlon+detlat*detlat)*scale_1*(num_1+num_2+num_3);
				 
				 
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			finally {
				try{
					statement.close();
					connection.close();
				}catch(Exception e)
				{
					
				}
			}
			return 0;
		}
		
	}
	
	
	public ArrayList<String> GetNearIds(int pointindex,double r)
	{
		ArrayList<String> ids = new ArrayList<>();
		FlickrPoint p1 = points[pointindex];
		for(int i=0;i<points.length;i++)
		{
			FlickrPoint p2 = points[i];
			double deltx = p1.lon-p2.lon;
			double delty = p1.lat - p2.lat;
			if(deltx*deltx+delty*delty>r) continue;//���г���ɸѡ������������·���ݾ��Ѿ������˰뾶����ô�Զ������õ�
			else if(GetDistance(pointindex, i)<=r)
				ids.add(points[i].ID);
		}
		return ids;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
