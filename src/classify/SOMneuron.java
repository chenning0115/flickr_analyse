package classify;

public class SOMneuron {

	FlickrPoint[] points;
	int k;
	int t = 0;//迭代次数
	int max_t = 20;//最大迭代次数
	double r;//当前权值调整瑜半径
	FlickrPoint[] o_neuron;
	double min_study_rate;
	injustfunction injustfun = new defaultimplementoffun(0.01,0.5);
	int[] result;
	public SOMneuron(FlickrPoint[] points,int k,int max_t,double min_study_rate,FlickrPoint[] defaultweight)
	{
		this.points = points;
		this.k = k;
		this.max_t = max_t;
		this.min_study_rate = min_study_rate;
		this.o_neuron = defaultweight;
		this.result = new int[points.length];
	}
	
	private double calculatedistancesquare(FlickrPoint p1,FlickrPoint p2)
	{
		double la = p1.lat-p2.lat;
		double lo = p1.lon-p2.lon;
		return la*la+lo*lo;
	}
	
	private boolean teststudy_rate()
	{
		if(injustfun.injuststudy_rate(t, 0)<=min_study_rate) return true;
		return false;
	}
	private void SOM_each(FlickrPoint cur_Point,int index)
	{
		double mindis = Double.MAX_VALUE;
		int winindex = -1;
		for(int i = 0;i<o_neuron.length;i++)
		{
			double temp_dis = calculatedistancesquare(cur_Point, o_neuron[i]);
			if(temp_dis<mindis) 
			{
				mindis = temp_dis;
				winindex = i;
			}
		}
		result[index] = winindex;
		//循环判断需要调整权值的神经元，并进行调整
		for(int i = 0;i<o_neuron.length;i++)
		{
			double temp_dis = calculatedistancesquare(o_neuron[winindex], o_neuron[i]);
			if(temp_dis<=r)
			{
				double temp_studyrate = injustfun.injuststudy_rate(t,temp_dis);
				o_neuron[i].lon=o_neuron[i].lon+temp_studyrate*(cur_Point.lon-o_neuron[i].lon);
				o_neuron[i].lat=o_neuron[i].lat+temp_studyrate*(cur_Point.lat-o_neuron[i].lat);
			}
		}
	}
	
	
	public void ExecuteSOM()
	{
		while(t<max_t)
		{
			System.out.println("start to som "+t+" time");
			for(int i = 0 ;i<points.length;i++)
			{
				SOM_each(points[i],i);
			}
			injustfun.injustr(t);
			t++;
			if(teststudy_rate()) break;
		}
	}
	
	public void toshp(String path)
	{
		int[] result1 = new int[k];
		for(int i = 0;i<k;i++) result1[i]=i;
		new ToSHP(o_neuron, result1,k, path+"_somneuron_center_"+o_neuron.length+"_").Toshapefile();
		new ToSHP(points, result, k, path+"_somneuron_class_"+o_neuron.length+"_").Toshapefile();
	}
	
	public void setinjustf(injustfunction injust)
	{
		this.injustfun = injust;
	}
	
	public interface injustfunction
	{
		public double injustr(int t);//权值调整函数
		public double injuststudy_rate(int t,double dis);//学习率计算函数
	}
	
	public class defaultimplementoffun implements injustfunction
	{

		double c1;//初始调整域半径
		double c2;//初始学习率
		public defaultimplementoffun(double c1,double c2) {
			// TODO Auto-generated constructor stub
			this.c1 = c1;
			this.c2 = c2;
		}
		@Override
		public double injustr(int t) {
			// TODO Auto-generated method stub
			return c1*(1d-((double)t)/((double)max_t));
		}

		@Override
		public double injuststudy_rate(int t, double dis) {
			// TODO Auto-generated method stub
			return c2*(1d-((double)t)/((double)max_t));
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			FlickrPoint[] points = ClassifyUtils.getFlickrPointsfromdatabase("beijing");
			double t2 = 0.000086;
			double t1 = 2*t2;
			Canopy canopy = new Canopy(points, t1, t2);
			FlickrPoint[] cur_centers = canopy.ExecuteCanopy();
			SOMneuron som = new SOMneuron(points, cur_centers.length, 10000, 0.0001, cur_centers);
			som.ExecuteSOM();
			som.toshp("f:/classify/beijing/10000times");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
		
	}

}
