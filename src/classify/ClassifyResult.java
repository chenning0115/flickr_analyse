package classify;

import recognize.BaiduPOIBaseInfo;

public class ClassifyResult {

	public DataSupporter dataSupporter;
	public int classnum = 0;//类别数目
	public int[] typesign;//聚类结果，值为类别标志。与datasupporter中原始点数据一一对应
	public FlickrPoint[] centerpoints;//聚类中心对应的datasupporter中原始点数据的索引
	
	public BaiduPOIBaseInfo[] centerinfo = null;//对应识别结果
	
	public ClassifyResult(DataSupporter _dDataSupporter,int _classnum,int[] _typesign,FlickrPoint[] _centerpoints)
	{
		this.dataSupporter = _dDataSupporter;
		this.classnum = _classnum;
		this.typesign = _typesign;
		this.centerpoints = _centerpoints;
	}
	
	public void setcenterinfo(BaiduPOIBaseInfo[] _centerinfo)
	{
		this.centerinfo = _centerinfo;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
	}

}
