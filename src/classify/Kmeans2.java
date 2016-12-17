package classify;

import java.util.*;

import flickrshape.ShapeFileWriter;


public class Kmeans2 {

	private DataSupporter dataSupporter;
	public ClassifyResult classifyResult = null;
	private final FlickrPoint[] points;
	private int[] result;
	private FlickrPoint[] cur_centers;
	private FlickrPoint[] ori_centers;
	int k = 10;
	int maxnum = 30;
	private double interval = 4e-6;//about 10.8m*10.8m in the earth
	private boolean IsCanopy = false;
	double T1;
	double T2;
	public Kmeans2(FlickrPoint[] ori_points,int input_k,int max)
	{
		this.points = ori_points;
		this.k = input_k;
		//initial
		result = new int[points.length];
		cur_centers = new FlickrPoint[k];
		ori_centers = new FlickrPoint[k];
		this.maxnum = max;
	}
	public Kmeans2(FlickrPoint[] ori_points,int input_k,double interval,int max)
	{
		this.points = ori_points;
		this.k = input_k;
		//initial
		result = new int[points.length];
		cur_centers = new FlickrPoint[k];
		ori_centers = new FlickrPoint[k];
		this.maxnum = max;
		this.interval = interval;
		
	}
	
	public Kmeans2(DataSupporter dataSupporters,int max,double t1,double t2)
	{
		this.dataSupporter = dataSupporters;
		this.points = this.dataSupporter.points;
		//this.k = input_k;
		//initial
		result = new int[points.length];
		cur_centers = new FlickrPoint[k];
		ori_centers = new FlickrPoint[k];
		this.maxnum = max;
		this.T1 = t1;this.T2 = t2;
		this.IsCanopy = true;
	}
	
	private void getrandomcenter()
	{
		HashSet<Integer> randomnum = new HashSet<>();
		//ClassifyUtils.randomSet(0, points.length-1, k,randomnum);
		randomnum.add(37);
		randomnum.add(251);
		int i = 0;
		System.out.println("set="+randomnum.toString());
		for (Integer num : randomnum) {
			cur_centers[i] = new FlickrPoint(""+i,points[num].lon,points[num].lat,points[num].datetaken);
			i++;
		}
	}
	
	private double calculatedistancesquare(FlickrPoint p1,FlickrPoint p2)
	{
		return dataSupporter.GetDistance(p1, p2);
	}
	private void mindistanceclassify()
	{
		int length = points.length;
		for(int i = 0;i<length;i++)
		{
			FlickrPoint temppoint = points[i];
			int temp_index = -1;
			double cur_mindissquare = Double.MAX_VALUE;
			for(int j = 0;j<k;j++)
			{
				double tempdis;
				if((tempdis=calculatedistancesquare(temppoint,cur_centers[j]))<cur_mindissquare)
				{
					temp_index = j;cur_mindissquare = tempdis;
				}
			}
			result[i]=temp_index;
		}
	}
	private void calculatenewcenter()
	{
		//not consider the overflow problem
		double[] lonsum = new double[k];
		double[] latsum = new double[k];
		int[] count = new int[k];
		for(int i = 0;i<k;i++) {lonsum[i]=0d;latsum[i]=0d;count[i]=0;}
		int length = points.length;
		for(int i=0;i<length;i++)
		{
			lonsum[result[i]]+=points[i].lon;
			latsum[result[i]]+=points[i].lat;
			count[result[i]]++;
		}
		//calculate new center
		ori_centers = cur_centers;
		//cur_centers = new FlickrPoint[k];
		LinkedList<FlickrPoint> cur_centers_list = new LinkedList<FlickrPoint>();
		int realclassnumber = 0;
		for(int i = 0;i<k;i++)
		{
			if(count[i]!=0)
			{
				cur_centers_list.add(new FlickrPoint(""+realclassnumber,lonsum[i]/count[i], latsum[i]/count[i],null));
				realclassnumber++;
			}
		}
		//赋予新的类别数目
		this.k = realclassnumber;
		cur_centers = cur_centers_list.toArray(new FlickrPoint[]{});
	}
	private boolean checkcenter()
	{
		//首先判断新的类中心中是否存在不能作为中心的点，即上次聚类过程中没有任何一个点属于该类，原因是由于此中心与某个中心一致
		if(cur_centers.length!=ori_centers.length) return false;
		
		double dissquare = 0;
		for(int i = 0;i<k;i++)
		{
			dissquare+=(calculatedistancesquare(ori_centers[i], cur_centers[i]));
		}
		if(dissquare>=0&&dissquare<interval) {System.out.println("dissquare is min enough!");return true;}
		else {
			return false;
		}
		
	}
	
	public int ExecuteClassify()
	{
		if(!IsCanopy)getrandomcenter();
		else{
			//DataSupporter dataSupporter = new DataSupporter(cityname, false);
			Canopy canopy = new Canopy(dataSupporter,T1,T2);
			this.cur_centers = canopy.ExecuteCanopy();
			this.k = cur_centers.length;
			System.out.println("canopy success k = "+k );
		}
		int number = 1;
		
		do{
			System.out.println("start to dismeans the "+number+" time");
			mindistanceclassify();
			calculatenewcenter();
			if(checkcenter()||number>maxnum) break;
			number++;
		}while(true);
		System.out.println("finish k-means classify: k = "+k);
		this.classifyResult = new ClassifyResult(this.dataSupporter, this.k, result, cur_centers);
		return k;
	}
	
	 //calculate the average distance of a cluster
	   private  double calculateaveragedistance(FlickrPoint[] data,FlickrPoint center)
	   {
		   double sum = 0d;
		   for(int i = 0;i<data.length;i++)
		   {
			   sum+=calculatedistancesquare(data[i], center);
		   }
		   return sum/data.length;
	   }
	   
	   public double assesskmeans()
	   {
		   double[] averagedis = new double[k];
		   ArrayList<LinkedList<FlickrPoint>> lists = new ArrayList<>();
		   for(int i = 0;i<k;i++)
		   {
			   lists.add(new LinkedList<>());
		   }
		   for(int i = 0;i<points.length;i++)
		   {
			   lists.get(result[i]).add(points[i]);
		   }
		   for(int i = 0;i<k;i++)
		   {
			   FlickrPoint[] classpoints = lists.get(i).toArray(new FlickrPoint[]{new FlickrPoint("0", 0d, 0d,null)});
			   if(classpoints!=null)
			   averagedis[i]=calculateaveragedistance(classpoints,cur_centers[i]);
			   else{
				   averagedis[i] = 0d;
			   }
		   }
		   //calculate intactcenter
		   double intactsumlo = 0d,intactsumla = 0d;
		   for(int i = 0;i<k;i++)
		   {
			   intactsumla+=cur_centers[i].lat;
			   intactsumlo+=cur_centers[i].lon;
		   }
		   FlickrPoint intactcenter = new FlickrPoint("center",intactsumlo/k, intactsumla/k,null);
		   double intactcenterdis = calculateaveragedistance(cur_centers, intactcenter);
		   double innercentersum = 0d;
		   for(int i = 0;i<k;i++)
		   {
			   innercentersum+=averagedis[i];
		   }
		   return intactcenterdis/innercentersum;
	   }
	
	public void kmeanstoshp(String _basepath)
	{
		new ShapeFileWriter(this.classifyResult,_basepath,"kmeansandcanopy_"+k);
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try {
			FlickrPoint[] points = ClassifyUtils.getFlickrPointsfromdatabase("beijing");
			
			//PrintWriter w = new PrintWriter(new File("f:/classify/beijing/kmeans1.csv"));
			double t2 = 0.000086;
			double t1 = 2*t2;
			for(int i = 0;i<1 ;i++)
			{
				
				Kmeans kmeans = new Kmeans(points,10000,t1,t2);
				int k =kmeans.ExecuteClassify();
				kmeans.kmeanstoshp("f:/classify/beijing/10000");
				System.out.println("k-means:t1 = "+t1+"t2 = "+t2+" assess = "+kmeans.assesskmeans());
				//System.out.println("k-means:t1 = "+t1+"t2 = "+t2);
				//w.println(k+","+t1+","+t2+","+kmeans.assesskmeans());
				t2+=0.00000002;
				t1 = 2*t2;
			}
			//w.flush();
			//w.close();
		} catch (Exception e) {
			// TODO Auto-generated catch blockt
			e.printStackTrace();
		}
		
	}

}
