package classify;

import recognize.BaiduPOIBaseInfo;

public class ClassifyResult {

	public DataSupporter dataSupporter;
	public int classnum = 0;//�����Ŀ
	public int[] typesign;//��������ֵΪ����־����datasupporter��ԭʼ������һһ��Ӧ
	public FlickrPoint[] centerpoints;//�������Ķ�Ӧ��datasupporter��ԭʼ�����ݵ�����
	
	public BaiduPOIBaseInfo[] centerinfo = null;//��Ӧʶ����
	
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
