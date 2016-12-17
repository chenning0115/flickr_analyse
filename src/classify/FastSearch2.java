package classify;


import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.Signature;
import java.security.KeyStore.PrivateKeyEntry;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.chrono.Era;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.RequestingUserName;

import org.apache.commons.collections.list.SynchronizedList;
import org.bridj.ann.DisableDirect;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.geotools.gml3.bindings.DoubleListBinding;
import org.hsqldb.store.ReusableObjectCache;

import com.ibm.icu.lang.UCharacter.EastAsianWidth;

import classify.FastSearchCluste.Node;
import classify.FastSearchCluste.calculatedensityclass;
import flickrdownload.DataBase;
import flickrdownload.PerservanceThread;
import flickrdownload.Utils;
import flickrshape.ShapeFileWriter;
import net.miginfocom.layout.ComponentWrapper;
import opendap.dap.test.expr_test;
import ucar.nc2.ft.point.remote.PointDatasetRemote;


public class FastSearch2 {

	private DataSupporter dataSupporter;
	private FlickrPoint[] points;
	public ClassifyResult classifyResult = null;
	//ArrayList<Node> list;
	java.util.List<Node> list;
	private Node[] nodes;
	int numclass = 0;
	//private String databasename;
	private double r = 0;
	private int minptsasnoise = 5;
	private double disinterval = 1e-15;//用于判断两个double类型的distance之间是否相差显著
	
	
	int maxpts = Integer.MIN_VALUE;
	int minpts = Integer.MAX_VALUE;
	double maxdis =Double.MIN_VALUE;
	double mindis = Double.MAX_VALUE;
	
	
	
	public FastSearch2(DataSupporter _datasupporter,double _r)
	{
		this.dataSupporter = _datasupporter;
		this.points = dataSupporter.points;
		this.r = _r;
		//this.databasename = _databasename;
		//this.minptsasnoise = _minptsasnoise;
	}
	static class Node
	{
		public int pointindex;
		public FlickrPoint flickrPoint;
		public int density_num;
		public double distance;
		public double tempval;//用于在计算distance时，临时存储于要求点间的距离，从而加入堆中
		public boolean checkinter = false;//在计算distance时用于记录是否进行过加入道路数据后的distance修正
		public int sign = 0;//0 represents not being classified , 1 represents has been classified, 2 represents center point,-1 represents noise point.
		public int classtype = -1; //-1 represents no class; -2 represents noise point
		public Node mindisnode = null;//note the mindistance and large density point reference
	}
	
	private int GetCountOfDisWithIn_r(int index)
	{
		int count = 0;
		count = dataSupporter.GetNearIds(index, this.r).size();
		System.out.println("count="+count);
		return count;
	}
	
	//多线程计算每个点的密度
	public class calculatedensityclass implements Runnable
	{

		private int i =0;
		public calculatedensityclass(int _i) {
			// TODO Auto-generated constructor stub
			this.i = _i;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("start calculate density of point "+i);
			Node curnode = new Node();
			curnode.flickrPoint = points[i];
			curnode.density_num = GetCountOfDisWithIn_r(i);
			curnode.pointindex = i;
			list.add(curnode);
			System.out.println("finish calculate density of point "+i);
		}
		
	}
	
	//计算基本的两点之间的距离
	private double calbasedis(FlickrPoint p1,FlickrPoint p2)
	{
		double deltx = p1.lon - p2.lon;
		double delty = p1.lat - p2.lat;
		return deltx*deltx + delty*delty;
	}
	
	
	//使用惰性计算的思想，尽量减少对道路数据的计算，本方法采用小根堆完成
	private double getmaxmindis(java.util.List<Node> list, int index)
	{
		Node curnode = list.get(index);
		PriorityQueue<Node> heap = new PriorityQueue<>(new Comparator<Node>() {

			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				if(o1.tempval>o2.tempval) return 1;
				else if(o1.tempval<o2.tempval) return -1;
				return 0;
			}
		});
		for(int i = 0;i<index;i++)
		{
			Node tempnode = list.get(i);
			tempnode.tempval = calbasedis(tempnode.flickrPoint,curnode.flickrPoint);
			heap.add(tempnode);
		}
		while(true)
		{
			Node minnode = heap.poll();
			if(minnode.checkinter) 
			{
				curnode.distance = minnode.tempval;
				curnode.mindisnode = minnode;
				break;
			}
			else{
				double tempdis = dataSupporter.GetDistance(curnode.pointindex,minnode.pointindex);
				if(Math.abs(tempdis-minnode.tempval)<disinterval) 
				{
					curnode.distance = minnode.tempval;
					curnode.mindisnode = minnode;
					break;
				}
				else
				{
					curnode.tempval = tempdis;
					heap.add(curnode);
				}
			}
		}
		return curnode.distance;
	}
	
	
	public java.util.List<Node> calculatepeak(String internaldatastoragepath)
	{
		 list = Collections.synchronizedList(new ArrayList<Node>());
		if(points==null || points.length==0)
		{
			System.out.println("The points array is null!");
			return null;
		}
		ExecutorService threadpool = Executors.newFixedThreadPool(Utils.Threadnum);
		for(int i = 0;i<points.length;i++)
		{
			Thread thread  = new Thread(new calculatedensityclass(i));
			threadpool.execute(thread);
		}
		try{
			threadpool.shutdown();
			threadpool.awaitTermination(30, TimeUnit.MINUTES);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("calculate density successfully..start to sort by density");
		
		Collections.sort(list,new Comparator<Node>() {//从大到小排序

			@Override
			public int compare(Node o1, Node o2) {
				// TODO Auto-generated method stub
				//System.out.println(o1+" "+o2);
				if (o1.density_num > o2.density_num) return -1;
				if(o1.density_num < o2.density_num) return 1;
				return 0;
			}
			
		});
		System.out.println("sort by density successfully..");
		//calculate the min distance with the node which has the large density then it
		double tempmax = Double.MIN_VALUE;
		int notemaxindex = 0;
		for(int i = 1;i<list.size();i++)
		{
			double tempdis = getmaxmindis(list, i);
			if(tempdis>tempmax) 
			{
				tempmax = tempdis;
				notemaxindex = i;
			}
		}
		list.get(0).distance = tempmax;
		list.get(0).mindisnode = list.get(notemaxindex);
		System.out.println("calculate distance successfully..");
		
		
		while(!outputmatrixdata(internaldatastoragepath))
		{
			try {
				System.out.println("输入任何字符重新输出矩阵数据!");
				System.in.read();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("output matrix(pointswithin_r,distance) successfully..");

		double readin =0;
		try{
			System.out.println("please input and times(归一化后的成绩):");
			BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in));
			readin = Double.parseDouble(reader.readLine());
			System.out.println("please input the minptsnoise:");
			minptsasnoise = Integer.parseInt(reader.readLine());
			
		}catch(Exception e){e.printStackTrace();}
		//according the user's input data, find the center of the inputdata for the cluster.
		//先将噪音点抛出，然后选择类中心，只要distance超过一定阈值的就列为类中心，因为在计算distance过程中，已经用到了密度概念
		//最后，根据类中心按照由高级到低级的扩散原理，利用递归对其他未归类的点进行归类。
		numclass = 0;
		double minmaxdis = maxdis - mindis;
		int minmaxpts = maxpts - minpts;
		for(int i = 0;i<list.size();i++)
		{
			Node curnode2 = list.get(i);
			//find the center point
			if(curnode2.density_num<minptsasnoise){//find the noise point
				curnode2.sign=-1;
				curnode2.classtype=-2;
			}
			else if((((curnode2.distance-mindis)/minmaxdis*100)*(((double)curnode2.density_num-minpts)/minmaxpts*100))>=readin)
				{
					curnode2.sign=2;
					curnode2.classtype=numclass;
					numclass++;
				}
			
		}
		System.out.println("类中心寻找结束，准备递归归类。。");
		//调用递归函数逐个寻找类型
		for(int i = 0;i<list.size();i++)
		{
			Node node3 = list.get(i);
			if(node3.sign==0) findclasstype(node3);
			System.out.println("递归归类"+i+"结束");
		}
		return list;
		
	}
	
	//给定一个节点，递归的找出其应该所属的类型
	private int findclasstype(Node node)
	{
		if(node.mindisnode.sign>0) {node.sign=1;node.classtype=node.mindisnode.classtype;return node.classtype;}
		int classtype = findclasstype(node.mindisnode);
		node.sign = 1;
		node.classtype = classtype;
		return classtype;
	}
	
	
	//输出二维坐标数据，从而用于人工进行判断应当是多少类比较合适
	public boolean outputmatrixdata(String path) 
	{
		try{
			FileOutputStream fStream = new FileOutputStream(new File(path));
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(fStream));
			maxpts = Integer.MIN_VALUE;
			minpts = Integer.MAX_VALUE;
			maxdis =Double.MIN_VALUE;
			mindis = Double.MAX_VALUE;
			
			for(int i = 0;i<list.size();i++)
			{
				Node tempnode = list.get(i);
				if(tempnode.density_num>maxpts) maxpts = tempnode.density_num;
				if(tempnode.density_num<minpts) minpts = tempnode.density_num;
				if(tempnode.distance>maxdis) maxdis = tempnode.distance;
				if(tempnode.distance<mindis) mindis = tempnode.distance;
			}
			for(int i = 0;i<list.size();i++)
			{
				Node tempnode = list.get(i);
				double a1 = ((double)tempnode.density_num-minpts)/(maxpts-minpts)*100;
				double a2 = (tempnode.distance-mindis)/(maxdis-mindis)*100;
				writer.println(tempnode.density_num+","+tempnode.distance+","+a1+","+a2+","+a1*a2
				+","+(1.0d/(1.0d+Math.exp(-1*a1*a2))));
			}
			writer.flush();
			writer.close();
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	
	public void excuteFastSearchCluster(String internaldatastoragepath)
	{
		calculatepeak(internaldatastoragepath);
		FlickrPoint[] 	centers = new FlickrPoint[numclass];
		int[] result = new int[points.length];
		for(int i = 0;i<list.size();i++)
		{
			Node tempnode = list.get(i);
			result[tempnode.pointindex]=tempnode.classtype;
			if(tempnode.sign==2&&tempnode.classtype>=0&&tempnode.classtype<numclass)
			{
				centers[tempnode.classtype]=tempnode.flickrPoint;
			}
		}
		this.classifyResult = new ClassifyResult(this.dataSupporter,this.numclass,result,centers);
	}
	
	public void toshp(String _basepath)
	{
		//new ToSHP(dataSupporter.points,classifyResult.typesign, numclass, path+"_fastsearchcluster_class_new").Toshapefile();
		//new ToSHP(classifyResult.centerpoints,null, numclass,path+"_fastsearchcluster_center_new").Toshapefile();
		new ShapeFileWriter(this.classifyResult,_basepath,"fastsearch_r"+r+"_noise"+minptsasnoise);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stu
		
	}


}
