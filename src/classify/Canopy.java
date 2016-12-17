package classify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import com.ibm.icu.impl.Trie;

import net.sf.jsqlparser.expression.operators.relational.LikeExpression;

public class Canopy {
	DataSupporter dataSupporter;
	FlickrPoint[] points;
	//int[] result;
	ArrayList<HashSet<FlickrPoint>> classLists = new ArrayList<>();
	ArrayList<FlickrPoint> canopycenters = new ArrayList<>();
	FlickrPoint[] center;
	int classnum = 0; 
	double T1;
	double T2;//T2是距离的平方
	
	public Canopy(FlickrPoint[] pointsr,double t1,double t2)
	{
		
	}
	public Canopy(DataSupporter _dataSupporter,double t1,double t2)
	{
		this.dataSupporter = _dataSupporter;
		this.points = dataSupporter.points;
		//result = new int[points.length];
		//for(int i = 0;i<points.length;i++) result[i] = -1;
		this.T1 = t1;
		this.T2 = t2;
	}
	
	
	/*private double calculatedistancesquare(FlickrPoint p1,FlickrPoint p2)
	{
		double la = p1.lat-p2.lat;
		double lo = p1.lon-p2.lon;
		return la*la+lo*lo;
	}*/
	
	private void calculatecenter()
	{
		//not consider the overflow problem
		int k = classnum;
		double[] lonsum = new double[k];
		double[] latsum = new double[k];
		int[] count = new int[k];
		for(int i = 0;i<k;i++) {lonsum[i]=0d;latsum[i]=0d;count[i]=0;}
		
		for(int i = 0;i<k;i++)
		{
			Iterator<FlickrPoint> iterator = classLists.get(i).iterator();
			while(iterator.hasNext())
			{
				FlickrPoint tp = iterator.next();
				lonsum[i]+=tp.lon;
				latsum[i]+=tp.lat;
				count[i]++;
			}
		}
		//calculate new center
		center = new FlickrPoint[k];
		for(int i = 0;i<k;i++)
		{
			
			center[i] = new FlickrPoint(""+i,lonsum[i]/count[i], latsum[i]/count[i],null);
		}
		
	}
	
	private void createnewcanopyset(FlickrPoint temppoint)
	{
		classLists.add(new HashSet<>());classLists.get(classnum).add(temppoint);
		canopycenters.add(temppoint);
		classnum++;
	}
	
	public FlickrPoint[] ExecuteCanopy()
	{
		System.out.println("start to execute canopy...");
		LinkedList<FlickrPoint> list = new LinkedList<>();
		for(int i = 0;i<points.length;i++)
		{
			list.add(points[i]);
		}
		while(!list.isEmpty())
		{
			Iterator<FlickrPoint> iterator = list.iterator();
			if(classnum==0)
			{
				FlickrPoint firstpoint = iterator.next();
				createnewcanopyset(firstpoint);
			}
			while(iterator.hasNext())
			{
				FlickrPoint tempPoint = iterator.next();
				boolean checkt2 = false;
				for(int i = 0;i<classnum;i++)
				{
					double dis = dataSupporter.GetDistance(tempPoint, canopycenters.get(i));
					if(dis<=T1) classLists.get(i).add(tempPoint);
					if(dis<=T2)
					{
						checkt2 = true;
					}
				}
				if(checkt2) //如果存在比T2小的情况，那么删除该点
				{
					iterator.remove();
				}
				else{
					createnewcanopyset(tempPoint);
				}
				
			}
			
		}
		//计算中心
		calculatecenter();
		System.out.println("finish executint canopy...");
		//return canopycenters.toArray(new FlickrPoint[]{});
		return center;
	}
	
	public void toshp(String path)
	{
		int[] result = new int[center.length];
		for(int i = 0;i<center.length;i++) result[i]=i;
		new ToSHP(center, result, result.length, path+"canopy_center_"+center.length+"_").Toshapefile();
	}

	public static void main(String[] args)
	{
		try{
			String cityname = "beijing";
			//FlickrPoint[] points = ClassifyUtils.getFlickrPointsfromdatabase(cityname);
			DataSupporter dataSupporter = new DataSupporter(cityname, false);
			double t2 = 0.000086;
			double t1 = 2*t2;
			Canopy canopy = new Canopy(dataSupporter, t1, t2);
			canopy.ExecuteCanopy();
			canopy.toshp("f:/classify/"+cityname+"/");
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
}
