package statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.apache.xml.resolver.helpers.PublicId;
import org.geotools.data.ogr.bridj.OsrLibrary.locale_data;
import org.geotools.gml3.bindings.DoubleListBinding;

import com.vividsolutions.jts.triangulate.VoronoiDiagramBuilder;

import classify.FlickrPoint;
import opendap.servers.test.test_SDArray;
import recognize.BaiduUtils;

/*
 * ['1','2','3','4','5','6','7','8','9','10','11','12']
 * ['2005','2006','2007','2008','2009','2010','2011','2012','2013','2014','2015']
 */
public class HeatManager {

	
	public static class tuple
	{
		String name;
		double lng;
		double lat;
		double num;
		int wordnum;
	}
	
	public ArrayList<tuple> readdata(String path)
	{
		ArrayList<tuple> datalist = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(path)),"GBK"));
			String line  = null;
			while((line=reader.readLine())!=null)
			{
				String[] strs = line.split(",");
				tuple temptuple = new tuple();
				temptuple.name = strs[0];
				temptuple.lng = Double.parseDouble(strs[1]);
				temptuple.lat = Double.parseDouble(strs[2]);
				temptuple.num = Double.parseDouble(strs[3]);
				datalist.add(temptuple);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return datalist;
	}
	
	public ArrayList<tuple> distinctdata(ArrayList<tuple> data)
	{
		ArrayList<tuple> resultlist = new ArrayList<>();
		Collections.sort(data, new Comparator<tuple>() {
			@Override
			public int compare(tuple o1, tuple o2) {
				// TODO Auto-generated method stub
				return o1.name.compareTo(o2.name);
			}
		});
		ArrayList<tuple> templist = new ArrayList<>();
		int numsum = 0;
		for(int i=0;i<data.size();i++)
		{
			templist.add(data.get(i));
			for(int j = i+1;j<data.size();)
			{
				if(data.get(j).name.equals(data.get(i).name))
				{
					templist.add(data.get(j));
					j++;i++;
				}
				else{
					break;
				}
			}
			double max = Double.MIN_VALUE;
			int maxindex = -1;
			double sum = 0;
			for(int k=0;k<templist.size();k++)
			{
				if(templist.get(k).num>max)
				{
					max = templist.get(k).num;
					maxindex = k;
				}
				sum+=(templist.get(k).num);
			}
			tuple r = new tuple();
			tuple rk = templist.get(maxindex);
			numsum+=templist.size();
			r.name = rk.name;r.lat = rk.lat;r.lng = rk.lng;
			r.num = sum;
			resultlist.add(r);
			templist.clear();
		}
		return resultlist;
		
	}
	
	public String generateBaiduHeatdata(ArrayList<tuple> data)
	{
		StringBuffer buffer = new StringBuffer();
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		for(int i =0;i<data.size();i++)
		{
			
			double tempvalue = data.get(i).num;
			if(max<tempvalue) max = tempvalue;
			if(min>tempvalue) min = tempvalue;
		}
		double inter = max - min ;
		for(int i =0;i<data.size();i++)
		{
			tuple temptuple = data.get(i);
			FlickrPoint temppoint = BaiduUtils.bd_encrypt(new FlickrPoint("",temptuple.lng, temptuple.lat,new Date(2015, 1, 1)));
			buffer.append("{\"lng\":"+temppoint.lon+",\"lat\":"+temppoint.lat+",\"count\":"+temptuple.num+"},\n");
		}
		return buffer.toString();
	}
	
	public void generatehearword(ArrayList<tuple> data)
	{
		for(int i=0;i<data.size();i++)
		{
			tuple temp = data.get(i);
			temp.wordnum =(int)temp.num/100+1;
			if(temp.wordnum>100) temp.wordnum = 100;
		}
		try{
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("src/heatword.csv")),"GBK"));
			for(int i =0;i<data.size();i++)
			{
				tuple tempt = data.get(i);
				for(int j=0;j<tempt.wordnum;j++)
				{
					writer.print(tempt.name);
				}
				writer.println();
				writer.flush();
			}
			writer.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void yearmon()
	{
		int row = 8;
		int col = 12;
		double[][] data = new double[row][col];
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File("src/yearmonth.csv")),"GBK"));
			String line  = null;
			for(int i =0;i<row;i++)
			{
				line = reader.readLine();
				String[] strs = line.split(",");
				double max = Double.MIN_VALUE;
				double min = Double.MAX_VALUE;
				for(int j =0;j<col;j++)
				{
					data[i][j] = Double.parseDouble(strs[j]);
					if(data[i][j]>max) max = data[i][j];
					if(data[i][j]<min) min = data[i][j];
				}
				for(int j = 0;j<col;j++)
				{
					data[i][j] = ((data[i][j]-min)/(max-min)*100);
					if(data[i][j]==0) data[i][j] = 1; 
				}
			}
			StringBuffer buffer = new StringBuffer();
			
			for(int i =0;i<row;i++)
			{
				for(int j =0;j<col;j++)
				{
					buffer.append("["+i+","+j+","+(int)data[i][j]+"],\n");
				}
			}
			System.out.println(buffer.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		HeatManager heatManager = new HeatManager();
		/*ArrayList<tuple> data = heatManager.readdata("src/heat.csv");
		System.out.println(heatManager.generateBaiduHeatdata(data));
		ArrayList<tuple> res = heatManager.distinctdata(data);
		heatManager.generatehearword(res);*/
		heatManager.yearmon();
		/*Collections.sort(res, new Comparator<tuple>() {
			@Override
			public int compare(tuple o1, tuple o2) {
				// TODO Auto-generated method stub
				return o1.name.compareTo(o2.name);
			}
		});
		double sum = 0;
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File("src/heatresult.csv")),"GBK"));
			for(int i = 0;i<res.size();i++)
			{
				writer.println(res.get(i).name+","+res.get(i).num);
				sum+=res.get(i).num;
				writer.flush();
			}
			writer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(sum+" "+res.size());*/
	}

}
