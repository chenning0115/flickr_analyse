package test;

import classify.DataSupporter;
import classify.Dbscan2;
import classify.FastSearch2;
import flickrshape.ShapeFileWriter;
import recognize.RecognizeByCenter;

public class DbscanTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		try{
			DataSupporter ds = new DataSupporter("beijing",false);
			//FastSearch2 fastSearchCluste = new FastSearch2(ds, 0.00001);
			Dbscan2 dbscan2 = new Dbscan2(ds,0.000005, 20);
			dbscan2.ExecuteDbscan();
			System.out.println("successfully classify!\nstart to recognize...");
			System.out.println("�����Ŀ="+dbscan2.classifyResult.classnum);
			//RecognizeByCenter recognizeByCenter = new RecognizeByCenter(dbscan2.classifyResult);
			//recognizeByCenter.Recognize(1000);//Ĭ��Ϊ1000�����ڣ����Ǹ�С�������뾶
			ShapeFileWriter shapeFileWriter = new ShapeFileWriter(dbscan2.classifyResult, "f:/flickrtest","dbscan");
			shapeFileWriter.Write();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
