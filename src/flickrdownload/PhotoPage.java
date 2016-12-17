package flickrdownload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoPage {

	int pagenumber = 0;
	int photonum = 0;
	public int sumpage = 0;
	
	FlickPhoto[] photoarray;
	
	public static PhotoPage ParseJson(String str_source)
	{
		if(str_source==null) return null;
		JSONObject jo = new JSONObject(str_source);
		
		JSONObject jo_photos = jo.getJSONObject("photos");
		PhotoPage photopage = new PhotoPage();
		photopage.pagenumber = jo_photos.getInt("page");
		JSONArray joArray  =  jo_photos.getJSONArray("photo");
		photopage.photonum = joArray.length();
		photopage.sumpage = jo_photos.getInt("pages");
		photopage.photoarray = new FlickPhoto[photopage.photonum];
		if(photopage.photonum!=joArray.length())
		{
			System.out.println("*****************\npagenumber="+photopage.pagenumber+"has wrong arraynum,that photonum not equals joarray.length()\n*****************\n");
			return null;
		}
		for(int i=0;i<joArray.length();i++)
		{
			photopage.photoarray[i] = FlickPhoto.ParseJson(joArray.getJSONObject(i));
		}
		return photopage;
		
	}
	
	public void WriteToFile(String filepath) throws FileNotFoundException
	{
		FileOutputStream fout = new FileOutputStream(new File(filepath));
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(fout));
		writer.println("pagenumber = "+this.pagenumber);
		writer.println("photonum = "+ this.photonum);
		
		for(int i = 0;i< this.photoarray.length;i++)
		{
			writer.println("-------------------------------");
			Map<String, Object> map = photoarray[i].photoinfo;
			writer.print("["+i +"]");
			for (String key : map.keySet()) 
			{
				writer.print(key+":"+map.get(key).toString()+"  ");
				/*if(key.equals("id")|| key.equals("datetaken"))
				{
					System.out.print(" "+map.get(key));
				}*/
			}
			writer.println("");
		}
		writer.flush();
		writer.close();
	}
	
	//将本对象数据写入到数据库中
	public void WriteToDataBase(Connection con,String TableName)
	{
		for(int i =0;i<photoarray.length;i++)
		{
			String sql = "";
			try{
			Map<String, Object> m = photoarray[i].photoinfo;
			sql = "insert into "+ TableName+"(id,owner,title,geom,description,dateupload,"
					+ "datetaken,ownername,accuracy,tags,secret,server,farm,ispublic,isfriend,"
					+ "isfamily,safe,o_width,o_height,views,place_id,woeid,geo_is_family,geo_is_friend,"
					+ "geo_is_contact,"
					+ "geo_is_public)"
					+ " values("
					+ "'"+m.get("id")+"',"
					+"'"+DataBase.encode(m.get("owner").toString())+"',"
					+"'"+DataBase.encode(m.get("title").toString())+" ',"
					+"st_geomfromText('POINT("+m.get("longitude")+" "+m.get("latitude")+")',4326),"
					+"'"+ DataBase.encode(m.get("description").toString())+"',"
					+ "'"+m.get("dateupload")+"',"
					+"'"+m.get("datetaken")+"',"
					+ "'"+DataBase.encode(m.get("ownername").toString())+"',"
					+m.get("accuracy")+","
					+"'"+DataBase.encode(m.get("tags").toString())+" ',"
					+"'"+m.get("secret")+"',"
					+m.get("server")+","
					+m.get("farm")+","
					+m.get("ispublic")+","
					+m.get("isfriend")+","
					+m.get("isfamily")+","
					+m.get("safe")+","
					+m.get("o_width")+","
					+m.get("o_height")+","
					+m.get("views")+","
					+"'"+m.get("place_id")+"',"
					+"'"+m.get("woeid")+"',"
					+m.get("geo_is_family")+","
					+m.get("geo_is_friedn")+","
					+m.get("geo_is_contact")+","
					+m.get("geo_is_public")
					+ ");"; 
				
				Statement statement = con.createStatement();
				statement.execute(sql);
				statement.close();
			}catch(Exception e)
			{
				//System.out.println(sql);
				//System.out.println("wrong sql!");
			}
		}
		
		
	}
	
	
	
	
	public static class FlickPhoto
	{
		public Map<String, Object> photoinfo = new HashMap<String,Object>();
		
		public static FlickPhoto ParseJson(String str_source)
		{
			if(str_source==null) return null;
			JSONObject jo = new JSONObject(str_source);
			FlickPhoto photo = new FlickPhoto();
			Iterator<String> keys = jo.keys();
			while(keys.hasNext())
			{
				String key = keys.next();
				if(!key.equals("description"))
				photo.photoinfo.put(key, jo.get(key));
				else {
					JSONObject content_o = jo.getJSONObject(key);
					photo.photoinfo.put(key, content_o.get("_content"));
				}
			}
			return photo;
		}
		
		public static FlickPhoto ParseJson(JSONObject jo)
		{
			if(jo==null) return null;
			FlickPhoto photo = new FlickPhoto();
			Iterator<String> keys = jo.keys();
			while(keys.hasNext())
			{
				String key = keys.next();
				photo.photoinfo.put(key, jo.get(key));
			}
			return photo;
		}
	}
	
	
}
