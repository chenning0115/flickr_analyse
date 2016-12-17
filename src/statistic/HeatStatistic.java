package statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.interfaces.ECPublicKey;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import au.com.objectix.jgridshift.Util;
import classify.ClassifyResult;
import classify.FlickrPoint;
import flickrdownload.Utils;
import recognize.BaiduPOIBaseInfo;

public class HeatStatistic {

	public ClassifyResult result;
	int[] flickrpicnums;
	public HeatStatistic(ClassifyResult _result)
	{
		this.result = _result;
		flickrpicnums = new int[result.classnum];
	}
	
	public void countpicnum()
	{
		int[] sign = result.typesign;
		for( int i = 0 ;i<flickrpicnums.length;i++) flickrpicnums[i] = 0;
		for(int i = 0;i< sign.length;i++)
		{
			if(sign[i]>=0)flickrpicnums[sign[i]]++;
		}
	}
	
	public void writedatatoexcel(String filepath)
	{
		try {
			countpicnum();
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(filepath))));
			writer.println("name,lon,lat,flickpnum,baidupnum,baiducnum");
			BaiduPOIBaseInfo[] bdinfo = result.centerinfo;
			FlickrPoint[] centers = result.centerpoints;
			
			for(int i = 0;i<flickrpicnums.length;i++)
			{
				writer.println(bdinfo[i].poi_name+","+centers[i].lon+","+centers[i].lat+","+flickrpicnums[i]+","+bdinfo[i].imagenum+","+bdinfo[i].comment_num);
			}
			writer.flush();
			writer.close();
			System.out.println("out put heatdata successfully!");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void writetop_n_timestatistic(String basepath,int topn)
	{
		for(int i = 0;i<topn;i++)
		{
			writeTimestatisticdata(basepath, i);
		}
		
	}
	//时间序列数据获取，存入csv文件中，按照年，月，日，照片数
	public void writeTimestatisticdata(String basepath,int classtype)
	{
		try {
			java.util.Date startdate,enddate;
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//Utils.savepaths = new ArrayBlockingQueue<>(1000);
			startdate =  format.parse("2005-01-01 00:00:00");
			enddate =  format.parse("2016-01-01 20:00:00");
			int daysnum = (int)(enddate.getTime()-startdate.getTime())/1000/3600/24;
			System.out.println("the daysnum = "+daysnum);
			int[] picnumperday = new int[daysnum];
			FlickrPoint[] points = result.dataSupporter.points;
			int[] typesign = result.typesign;
			for(int i = 0;i<points.length;i++)
			{
				if(typesign[i]==classtype)
				{
					int dayinter = (int)(points[i].datetaken.getTime()-startdate.getTime())/1000/3600/24;
					picnumperday[dayinter]++;
				}
			}
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(basepath+"/picnum_"+classtype+".csv"))));
			
			Calendar c = Calendar.getInstance();
			c.setTime(startdate);
			writer.println("year,month,day,picnum");
			for(int i = 0;i<picnumperday.length;i++)
			{
				java.util.Date tempdate = c.getTime();
				writer.println(tempdate.getYear()+","+tempdate.getMonth()+","+tempdate.getDate()+","+picnumperday[i]);
				c.add(c.DATE,1);
			}
			writer.flush();
			writer.close();
			System.out.println("output the picnum successfully of "+classtype+"!");
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//生成Echart网站画地图散点图和地图密度图数据
	public static void generratewebdata(String filepath,String fileoutput)
	{
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(fileoutput))));
			StringBuffer buffer_data = new StringBuffer();
			StringBuffer buffer_geo = new StringBuffer();
			String tempstr = reader.readLine();
			while((tempstr = reader.readLine())!=null)
			{
				String[] strs = tempstr.split(",");
				String name  = strs[0];
				double lon = Double.parseDouble(strs[1]);
				double lat = Double.parseDouble(strs[2]);
				double heat = Double.parseDouble(strs[3]);
				buffer_data.append("{name: '"+name+"', value: "+heat+"},\r\n");
				buffer_geo.append("'"+name+"':["+lon+","+lat+"],\r\n");
			}
			writer.println(buffer_data);
			writer.println("==============================================");
			writer.println(buffer_geo);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HeatStatistic.generratewebdata("f:/flickrtest/fastsearch_heat.csv","f:/flickrtest/fastweb.txt");
	}

}
