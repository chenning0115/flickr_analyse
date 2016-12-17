package test;

import classify.DataSupporter;
import classify.FastSearch3;
import classify.Kmeans2;
import flickrshape.ShapeFileWriter;

public class TestKmeans {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataSupporter ds = new DataSupporter("beijing",true);
		Kmeans2 kmeans2 = new Kmeans2(ds, 500,4e-4,2e-4);
		kmeans2.ExecuteClassify();
		
		System.out.println("successfully classify!\nstart to recognize...");
		System.out.println("类别数目="+kmeans2.classifyResult.classnum);
		//RecognizeByCenter recognizeByCenter = new RecognizeByCenter(fastSearch2.classifyResult);
		//recognizeByCenter.Recognize(1000);//默认为1000米以内，除非更小的搜索半径
		ShapeFileWriter shapeFileWriter = new ShapeFileWriter(kmeans2.classifyResult, "f:/flickrtest","kmeans");
		shapeFileWriter.Write();
	}

}
