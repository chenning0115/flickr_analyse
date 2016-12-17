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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.print.attribute.standard.RequestingUserName;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.geotools.geometry.iso.util.elem2D.Node2D;
import org.hsqldb.store.ReusableObjectCache;

import com.ibm.icu.lang.UCharacter.EastAsianWidth;

import flickrdownload.DataBase;
import flickrdownload.PerservanceThread;
import flickrshape.ShapeFileWriter;
import net.miginfocom.layout.ComponentWrapper;
import opendap.dap.test.expr_test;
import ucar.nc2.ft.point.remote.PointDatasetRemote;

public class FastSearchCluste {

	private DataSupporter dataSupporter;
	private FlickrPoint[] points;
	public ClassifyResult classifyResult = null;
	ArrayList<Node> list;
	private Node[] nodes;
	int numclass = 0;
	//private String databasename;
	private double r = 0;
	private int minptsasnoise = 5;
	
	
	
	public FastSearchCluste(DataSupporter _datasupporter,double _r)
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
	
	
	public ArrayList<Node> calculatepeak(String internaldatastoragepath)
	{
		 list = new ArrayList<>();
		if(points==null || points.length==0)
		{
			System.out.println("The points array is null!");
			return null;
		}
		ExecutorService threadpool = Executors.newFixedThreadPool(500);
		for(int i = 0;i<points.length;i++)
		{
			Thread thread  = new Thread(new calculatedensityclass(i));
			threadpool.execute(thread);
		}
		try{
			threadpool.shutdown();
			threadpool.awaitTermination(20, TimeUnit.MINUTES);
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
		//calculate the min distance with the node which has the large density then it.
		double tempmax = Double.MIN_VALUE;
		for(int i = 1;i<list.size();i++)
		{
			Node node1 = list.get(i);
			double tempmin = Double.MAX_VALUE;
			for(int j = 0;j<i;j++)
			{
				Node node2 = list.get(j);
				double tempdis = dataSupporter.GetDistance(node1.pointindex, node2.pointindex);
				if(tempdis<tempmin) {tempmin = tempdis;node1.mindisnode=node2;}
			}
			node1.distance = tempmin;
			//System.out.print(tempmin);
			if(tempmin>tempmax) {tempmax = tempmin;list.get(0).mindisnode=node1;}
		}
		list.get(0).distance = tempmax;
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
			System.out.println("please input the density_num and distance's times:");
			BufferedReader reader =  new BufferedReader(new InputStreamReader(System.in));
			readin = Double.parseDouble(reader.readLine());
			System.out.println("please input the minptsnoise:");
			minptsasnoise = Integer.parseInt(reader.readLine());
			
		}catch(Exception e){e.printStackTrace();}
		//according the user's input data, find the center of the inputdata for the cluster.
		numclass = 0;
		for(int i = 0;i<list.size();i++)
		{
			Node curnode2 = list.get(i);
			//find the center point
			if(curnode2.density_num*curnode2.distance>readin)
			{
				curnode2.sign=2;
				curnode2.classtype=numclass;
				numclass++;
			}
			else if(curnode2.density_num<minptsasnoise){//find the noise point
				curnode2.sign=-1;
				curnode2.classtype=-2;
			}
		}
		
		//调用递归函数逐个寻找类型
		for(int i = 0;i<list.size();i++)
		{
			Node node3 = list.get(i);
			if(node3.sign==0) findclasstype(node3);
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
			for(int i = 0;i<list.size();i++)
			{
				writer.println(list.get(i).density_num+","+list.get(i).distance+","+list.get(i).mindisnode.pointindex);
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
		try{
			//FlickrPoint[] points = ClassifyUtils.getFlickrPointsfromdatabase("beijing");
			DataSupporter ds = new DataSupporter("beijing",true);
			FastSearchCluste fastSearchCluste = new FastSearchCluste(ds, 0.00005);
			fastSearchCluste.excuteFastSearchCluster("f:/classify/fastsearchnew.csv");
			fastSearchCluste.toshp("f:/classify/");
			System.out.println("successfully..");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}

