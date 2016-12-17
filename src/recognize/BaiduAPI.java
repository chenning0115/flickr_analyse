package recognize;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.Thread.State;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.Response;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.xml.resolver.apps.resolver;
import org.apache.xml.resolver.helpers.PublicId;
import org.bson.util.StringRangeSet;
import org.eclipse.ui.part.PageSite;
import org.json.JSONArray;
import org.json.JSONObject;
import flickrdownload.*;
/*
 * Baidu map api place poi 检索，当前只支持圆形检索
 */
public class BaiduAPI {

	private String str_query = "景区$机场$车站";
	private int scope = 2;
	private int codetype = 1;
	private int page_size = 10;
	//private int num = 0;	
	//private double radius = 2000;
	private String outputtype = "json";
	private String baseurl = "http://api.map.baidu.com/place/v2/search";
	
	private String geturl(Map<String, String> param)
	{
		String url = baseurl+"?ak="+Utils.baiduapikey;
		for (String key : param.keySet()) {
			String value = param.get(key);
			url=url+"&"+key+"="+value;
		}
		return url;
	}
	public String radiusquery(double lon,double lat,int _pagenum,double radius)
	{
		CloseableHttpResponse response=null;
		try{
		HashMap<String, String> map = new HashMap<>();
		map.put("output",this.outputtype);
		map.put("query",URLEncoder.encode(this.str_query, "UTF-8"));
		map.put("page_size",String.valueOf(page_size));
		map.put("scope",String.valueOf(scope));
		map.put("page_num",String.valueOf(_pagenum));
		map.put("page_size",String.valueOf(page_size));	
		map.put("location",String.valueOf(lat)+","+String.valueOf(lon));
		map.put("coord_type", String.valueOf(codetype));
		map.put("filter",URLEncoder.encode("industry_type:life|sort_name:comment_num|sort_rule:2","UTF-8"));
		map.put("radius", String.valueOf(radius));
		String url = geturl(map);
		System.out.println(url);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		
		HttpGet httpget = new HttpGet(url);
		response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		InputStream ins = entity.getContent();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins,"UTF-8"));
		String line = null;
		StringBuilder builder = new StringBuilder();
		while((line=reader.readLine())!=null)
		{
			builder.append(line);
		}
		//System.out.println(builder.toString());
		return builder.toString();
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}finally {
			try {
				response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public BaiduPOIBaseInfo ParsePOIJson(JSONObject jo)
	{
		String name = jo.getString("name");
		JSONObject jo_location = jo.getJSONObject("location");
		JSONObject jo_detail = jo.getJSONObject("detail_info");
		double lng = jo_location.getDouble("lng");
		double lat = jo_location.getDouble("lat");
		int image_num = 0; if(jo_detail.has("image_num")) image_num = jo_detail.getInt("image_num");
		int comment_num = 0;if(jo_detail.has("comment_num")) comment_num = jo_detail.getInt("comment_num");
		int distance = jo_detail.getInt("distance");
		String tag = jo_detail.getString("tag");
		return new BaiduPOIBaseInfo(name, lng, lat, image_num, comment_num, distance, tag);
		
		
	}
	//获取给定(lon,lat)的一定参数内所有的poi信息（不仅仅是第一页）
	public BaiduPOIBaseInfo[] Querybyradius(double lon,double lat,double radius)
	{
		int pagenum = 0;
		int pagetotal = 0;
		int count = 0;
		BaiduPOIBaseInfo[] pois = null;
		do
		{
			String str_result = radiusquery(lon, lat, pagenum,radius);
			if(str_result!=null)
			{
				JSONObject jo = new JSONObject(str_result);
				int page_status = jo.getInt("status");
				String page_msg = jo.getString("message");
				//System.out.println("status"+page_status+" msg="+page_msg);
				if(page_status==0)
				{
					int temp_total = jo.getInt("total");
					if(pagetotal==0)
					{
						pagetotal = temp_total;
						pois = new BaiduPOIBaseInfo[pagetotal];
					}
					else if(pagetotal!=temp_total) {System.out.println("The total num has changed!");return null;}
					JSONArray joArray = jo.getJSONArray("results");
					for(int i = 0;i<joArray.length();i++)
					{
						BaiduPOIBaseInfo temppoi = ParsePOIJson(joArray.getJSONObject(i));
						pois[count] = temppoi;
						count++;
					}
					
				}

			}
			
			pagenum++;
			
		}while(count<pagetotal);
		return pois;
	}
	//给定一个poi数组和比较函数，找出该poi数组中最优的poi
	public BaiduPOIBaseInfo getmaxwellpoi(Comparator<BaiduPOIBaseInfo> comparator,BaiduPOIBaseInfo[] pois)
	{
		if(pois==null||pois.length==0) return null;
		if(comparator==null) comparator = new DefaultPOIComparator();
		BaiduPOIBaseInfo maxwellpoi = pois[0];
		for(int i=1;i<pois.length;i++)
		{
			if(comparator.compare(maxwellpoi, pois[i])<0) maxwellpoi = pois[i];
		}
		
		return maxwellpoi;
	}
	
	public class DefaultPOIComparator implements Comparator<BaiduPOIBaseInfo>
	{

		@Override
		public int compare(BaiduPOIBaseInfo o1, BaiduPOIBaseInfo o2) {
			// TODO Auto-generated method stub
			int imagenum = o1.imagenum-o2.imagenum;
			int commentnum = o1.comment_num - o2.comment_num;
			int sum = imagenum + commentnum;
			
			return sum;
		}
		
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		BaiduAPI placeapi = new BaiduAPI();
		BaiduPOIBaseInfo[] pois = placeapi.Querybyradius(116.318427,39.99907,1000);
		System.out.println("pois="+pois.length);
		System.out.println(placeapi.getmaxwellpoi(null, pois).toString());
	}

}
