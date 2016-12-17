package recognize;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.geotools.filter.expression.ThisPropertyAccessorFactory;
import org.geotools.gml3.bindings.DoubleListBinding;
import org.geotools.xml.xsi.XSISimpleTypes.Int;
import org.h2.util.IntIntHashMap;

import classify.ClassifyResult;
import classify.DataSupporter;
import classify.FastSearchCluste;
import classify.FlickrPoint;
import classify.FastSearch2.calculatedensityclass;
import flickrdownload.Utils;
import flickrshape.ShapeFileWriter;
import thredds.cataloggen.inserter.LatestCompleteProxyDsHandler;

public class RecognizeByCenter {

	private ClassifyResult classifyResult = null;

	
	public RecognizeByCenter(ClassifyResult _cClassifyResult)
	{
		this.classifyResult = _cClassifyResult;
	}
	private double getmindis(FlickrPoint point)
	{
		FlickrPoint[] centers = classifyResult.centerpoints;
		double mindis = Double.MAX_VALUE;
		for(int i = 0;i<classifyResult.classnum;i++)
		{
			double tempdis = classifyResult.dataSupporter.GetDistance(point, centers[i]);
			if(tempdis<mindis) tempdis = mindis;
		}
		return mindis;
	}
	
	public class recongeachThread implements Runnable
	{
		BaiduAPI baiduAPI = new BaiduAPI();
		BaiduPOIBaseInfo[] centerinfo;
		int i;
		double lon;
		double lat;
		double tempdis;
		public recongeachThread(BaiduPOIBaseInfo[] _centerinfo,int _i,double _lon,double _lat,double _tempdis) {
			// TODO Auto-generated constructor stub
			this.centerinfo = _centerinfo;
			this.i = _i;
			this.lon = _lon;
			this.lat = _lat;
			this.tempdis = _tempdis;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			BaiduPOIBaseInfo tempinfo = baiduAPI.getmaxwellpoi(null,baiduAPI.Querybyradius(lon,lat,tempdis));
			if(tempinfo!=null) centerinfo[i] = tempinfo;
			else{
				centerinfo[i] = tempinfo = new BaiduPOIBaseInfo("notrec", 0, 0, 0, 0, 0,"");
			}
			System.out.println(tempinfo.poi_name);
		}
		
	}
	//ʶ�����е���𵽵����ĸ����㣬�õ������������Ϣ
	public ClassifyResult Recognize(double defaultradius)
	{
		FlickrPoint[] centers = this.classifyResult.centerpoints;
		int classnum = this.classifyResult.classnum;
		BaiduPOIBaseInfo[] centerinfo = new BaiduPOIBaseInfo[classnum];
		ExecutorService threadpool = Executors.newFixedThreadPool(1);
		//BaiduAPI baiduAPI = new BaiduAPI();
		for(int i = 0;i<classnum;i++)
		{
			FlickrPoint curcenter = centers[i];
			double tempdis = Math.min(getmindis(curcenter),defaultradius);
			//FlickrPoint correctpoint = BaiduUtils.bd_encrypt(curcenter);
			Thread thread  = new Thread(new recongeachThread(centerinfo, i, curcenter.lon, curcenter.lat, tempdis));
			threadpool.execute(thread);
		}
		try{
			threadpool.shutdown();
			threadpool.awaitTermination(30, TimeUnit.MINUTES);
			System.out.println("all is recongnized!");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		this.classifyResult.setcenterinfo(centerinfo);

		return this.classifyResult;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		
	}

}
