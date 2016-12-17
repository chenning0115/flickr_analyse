package classify;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import flickrdownload.DataBase;

public class Dbscan {

	public static class dbnode
	{
		int index;
		boolean visited = false;
		int sign = -1;
		
		public dbnode()
		{
			
		}
		public dbnode(int ori_index)
		{
			this.index = ori_index;
		}
	}
	
	//data region
	
	private Map<String, dbnode> map;
	private FlickrPoint[] points;
	private int k = 0;
	
	private double r = 0d;
	private int minpts = 20;
	
	private String databasename = "flickr_data";
	
	public Dbscan(FlickrPoint[] ori_points,double ori_r,int ori_minpts,String db_table_name)
	{
		this.points = ori_points;
		this.r = ori_r;
		this.minpts = ori_minpts;
		this.databasename = db_table_name;
		map = new HashMap<String, Dbscan.dbnode>();
		for(int i = 0;i<points.length;i++)
		{
			map.put(points[i].ID,new dbnode(i));
		}
	}
	
	/*
	 * there are many reduplicated ids(as many photos have the same location),
	 * but they have ditinct ids.
	 * 
	 * In order to improve the performace sugggest the database table contains less items.
	 */
	private LinkedList<String> getIDsin_r(String id)throws Exception
	{
		LinkedList<String> list = new LinkedList<>();
		Connection connection = DataBase.GetPostgresqlConnection();
		Statement statement = connection.createStatement();
		String sql = "select id from "+databasename+" as table_a where"
				+ "(sign = 'beijing' and  (select ST_Distance(table_a.geom,table_b.geom) from "+databasename+" as table_b where id = '"+id+"')<"+r+");";
		ResultSet rs = statement.executeQuery(sql);
		while(rs.next())
		{
			list.add(rs.getString(1));
		}                                                
		statement.close();connection.close();
		return list;
	}
	
	public void ExecuteDbscan()throws Exception
	{
		Set<Map.Entry<String, dbnode>> set = map.entrySet();
		Iterator<Map.Entry<String, dbnode>> iterator = set.iterator();
		while(iterator.hasNext())
		{
			System.out.println("The classnumber is "+k);
			Map.Entry<String, dbnode> entry = iterator.next();
			if(!entry.getValue().visited)
			{
				entry.getValue().visited = true;
				LinkedList<String> tempids = getIDsin_r(entry.getKey());
				int count = tempids.size();
				if(count < minpts) //temporary to be considered the noise point and set the sign = -2;
				{
					entry.getValue().sign = -2;
					continue;
				}
				else{// represent this point is one of the prime point
					entry.getValue().sign = k;//first set the class nubmer
					LinkedList<String> cluster = new LinkedList<>();
					cluster.add(entry.getKey());
					for (String tempid : tempids) {
						dbnode node = map.get(tempid);
						if(node.sign<0)//not classify or noise point
						{
							node.sign = k;
							if(!node.visited) cluster.add(tempid); //cluster not visited
						}
						
					}
					//对于首次得到的邻域内的点逐个进行判断并迭代
					while(!cluster.isEmpty())
					{
						System.out.println(cluster.size());
						String id2 = cluster.removeFirst();
						dbnode node2 = map.get(id2);
						if(!node2.visited)
						{
							node2.visited=true;
							LinkedList<String> templist2 = getIDsin_r(id2);
							if(templist2.size()>=minpts)
							{
								for (String neiborid2 : templist2) {
									dbnode neibornode2 = map.get(neiborid2);
									if(neibornode2.sign<0)
									{
										neibornode2.sign=k;
										if(!neibornode2.visited) cluster.addLast(neiborid2);
									}
								}
							}
						}
					}
					
					k++;
					
				}
			}
			
		}
	}
	
	private FlickrPoint[] calculatenewcenter(int[] result)
	{
		//not consider the overflow problem
		
		double[] lonsum = new double[k];
		double[] latsum = new double[k];
		int[] count = new int[k];
		for(int i = 0;i<k;i++) {lonsum[i]=0d;latsum[i]=0d;count[i]=0;}
		int length = points.length;
		for(int i=0;i<length;i++)
		{
			if(result[i]==-2) continue;
			lonsum[result[i]]+=points[i].lon;
			latsum[result[i]]+=points[i].lat;
			count[result[i]]++;
		}
		//calculate new center
		LinkedList<FlickrPoint> cur_centers_list = new LinkedList<FlickrPoint>();
		int realclassnumber = 0;
		for(int i = 0;i<k;i++)
		{
			if(count[i]!=0)
			{
				cur_centers_list.add(new FlickrPoint(""+realclassnumber,lonsum[i]/count[i], latsum[i]/count[i],null));
				realclassnumber++;
			}
		}
		//赋予新的类别数目
		this.k = realclassnumber;
		return cur_centers_list.toArray(new FlickrPoint[]{});
	}
	
	public void toshp(String path)
	{
		int[] result = new int[points.length];
		for(int i = 0;i<points.length;i++)
		{
			result[i] = map.get(points[i].ID).sign;
		}
		new ToSHP(points, result, k, path+"_dbscan_class_").Toshapefile();
		FlickrPoint[] centers = calculatenewcenter(result);
		int[] classsign = new int[k];
		for(int i = 0;i<k;i++)
		{
			classsign[i] = i;
		}
		new ToSHP(centers,classsign,k,path+"dbscan_center_").Toshapefile();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			FlickrPoint[] points = ClassifyUtils.getFlickrPointsfromdatabase("beijing");
			Dbscan dbscan = new Dbscan(points, 0.0000001, 5,"flickr_data");
			dbscan.ExecuteDbscan();
			dbscan.toshp("f:/classify/beijing/");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
