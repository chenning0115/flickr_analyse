package recognize;

import flickrdownload.Utils;

public class BaiduPOIBaseInfo {

	//public String id; //对应于聚类过程中的ID （如果有的话）
	public String poi_name;
	public double poi_lon;
	public double poi_lat;
	//public double distancetoID;
	public int imagenum = 0;
	public int comment_num = 0;
	public int distance;
	public String tag;
	
	public BaiduPOIBaseInfo(String _poiname,double _poi_lon,double _poi_lat,int _iamgenum,int _comment_num,int _distance,String tag)
	{
		this.poi_name = _poiname;
		this.poi_lat = _poi_lat;
		this.poi_lon = _poi_lon;
		this.imagenum = _iamgenum;
		this.comment_num = _comment_num;
		this.distance = _distance;
		this.tag = tag;
		
	}
	
	public String toString()
	{
		return poi_name+",distance="+distance;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
