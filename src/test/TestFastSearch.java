package test;

import classify.DataSupporter;
import classify.FastSearch2;
import classify.FastSearch3;
import classify.FastSearchCluste;
import flickrshape.ShapeFileWriter;
import recognize.RecognizeByCenter;
import statistic.HeatStatistic;

public class TestFastSearch {

	public static void main(String[] args) {
		// TODO Auto-generated0 method stub
		//test fastsearch and recognize
				try{
					DataSupporter ds = new DataSupporter("beijing",true);
					FastSearch2 fastSearch = new FastSearch2(ds, 5e-6);
					fastSearch.excuteFastSearchCluster("f:/flickrtest/fastsearch_data.csv");
					System.out.println("successfully classify!\nstart to recognize...");
					System.out.println("�����Ŀ="+fastSearch.classifyResult.classnum);
					RecognizeByCenter recognizeByCenter = new RecognizeByCenter(fastSearch.classifyResult);
					recognizeByCenter.Recognize(1000);//Ĭ��Ϊ1000�����ڣ����Ǹ�С�������뾶
					
					ShapeFileWriter shapeFileWriter = new ShapeFileWriter(fastSearch.classifyResult, "f:/flickrtest","fastsearch");
					HeatStatistic heatStatistic = new HeatStatistic(fastSearch.classifyResult);
					heatStatistic.writedatatoexcel("f:/flickrtest/fastsearch_heat.csv");
					heatStatistic.writetop_n_timestatistic("f:/flickrtest", 5);
					shapeFileWriter.Write();
					
				}catch(Exception e)
				{
					e.printStackTrace();
				}
	}

	
}
