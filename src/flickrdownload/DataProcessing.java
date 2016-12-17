package flickrdownload;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntToDoubleFunction;

import org.geotools.filter.SQLEncoderException;

import com.mysql.jdbc.Statement;
import com.vividsolutions.jts.geom.Point;

import classify.FastSearchCluste.calculatedensityclass;

/*
 * ����Ԥ����
 * Ŀǰ����ɾ��ͬһ���û���ͬһ���ص㣨һ���������ڣ�ͬһʱ�䣨��һ��ʱ����ڣ�����Ķ����Ƭ
 * ֻ����������Ϣ��ȫ�����Ƭ
 */
public class DataProcessing {

	private double r = 6e-4;//���뻺����
	private long inter_time = 3*60*60*1000;//ʱ����(����)
	//ArrayList<String> deleteids = new ArrayList<>();
	
	private static class iteminfo
	{
		public String id;
		public Date datetaken;
		public double lon;
		public double lat;
		public String text;
	}
	private boolean deleteitemsbyids(ArrayList<String> id_set,Connection con,java.sql.Statement statement)
	{
		if(id_set.size()==0) return true;
		String str_ids = "(";
		for (String idString : id_set) {
			str_ids=str_ids+"'"+idString+"',";
		}
		String ids = str_ids.substring(0,str_ids.length()-1);
		ids+=")";
		String sql = "delete from "+DataBase.tablename+" where id in "+ids+";";
		//System.out.println(sql);
		try{
			 statement.execute(sql);
			System.out.println("ɾ��"+id_set);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}
	/*
	 * ���ԣ�list�Ѿ�����datetaken�ź����ˣ��ӵ�һ����ʼ�ٶ�Ϊ���ĵ㣬�ڸ�������
	 * �ڰ�List˳�����ڴ˻������ڲ���ʱ����С�ڸ�������ĵ�item����һ��itemһ��Ҫ����һ����Ȼ����һ����Ϊ���ĵ㣬
	 * ������ˡ�
	 */
	private ArrayList<String> processeachownerdata(ArrayList<iteminfo> list)
	{
		ArrayList<String> deleteids = new ArrayList<>();
		if(list==null || list.size()==0) return deleteids;
		//iteminfo curitem;
		int curcenter = 0;
		int notdelete = 0;
		for(int i =0;i<list.size();i++)
		{
			notdelete = i;
			iteminfo curitem = list.get(i);
			for(int j =i+1;j<list.size();j++)
			{	
				iteminfo tempitem = list.get(j);
				if(tempitem.datetaken.getTime()-curitem.datetaken.getTime()<inter_time
						&&Math.abs(tempitem.lat-curitem.lat)<r
						&&Math.abs(tempitem.lon-curitem.lon)<r)
				{
					//deleteids.add(tempitem.id);
					if(tempitem.text.length()>list.get(notdelete).text.length())
					{
						deleteids.add(list.get(notdelete).id);
						notdelete = j;
					}
					else{
						deleteids.add(tempitem.id);
					}
				}
				else{
					i=j-1;break;
				}
			}
		}
		return deleteids;
	}
	
	public class ownerprocessing implements Runnable
	{
		String owner = "";
		int count_i = 0;
		java.sql.Statement statement;
		Connection con;
		public ownerprocessing(String _owner,int _count_i) {
			// TODO Auto-generated constructor stub
			this.owner = _owner;
			this.count_i = _count_i;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				con = DataBase.GetPostgresqlConnection();
				statement = con.createStatement();
				String sql_own = "select id,datetaken,ST_AsText(geom),title,tags,description from "
						+DataBase.tablename + " where owner like '"+this.owner+"' order by datetaken;";
				//System.out.println(sql_own);
				ResultSet rs_owner = statement.executeQuery(sql_own);
				ArrayList<iteminfo> items_owner  =new ArrayList<>();
				while(rs_owner.next())
				{
					iteminfo item = new iteminfo();
					item.id = rs_owner.getString(1);
					item.datetaken = rs_owner.getDate(2);
					Point p = Utils.parsepointfromwkt(rs_owner.getString(3));
					item.lon = p.getX();
					item.lat = p.getY();
					item.text = rs_owner.getString(4)+rs_owner.getString(5)+rs_owner.getString(6);
					items_owner.add(item);
				}
				//����
				ArrayList<String> deleteids = processeachownerdata(items_owner);
				deleteitemsbyids(deleteids,con,statement);
				statement.close();con.close();
				System.out.println("��ɴ����"+count_i);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void dataprocess()
	{
		//��ȡ��ͬ���û���
		Connection con = null;
		java.sql.Statement statement = null;
		try{
			String sql ="select distinct(owner) from "
					+DataBase.tablename+";";
			System.out.println(sql);
			con = DataBase.GetPostgresqlConnection();
			statement = con.createStatement();
			ResultSet rs = statement.executeQuery(sql);
			ArrayList<String> distinctowner = new ArrayList<>();
			while(rs.next())
			{
				distinctowner.add(rs.getString(1));
			}
			statement.close();
			con.close();
			//����ÿһ���û�����������Ԥ����
			ExecutorService threadpool = Executors.newFixedThreadPool(500);
			int count_i = 0;
			for (String str_owner : distinctowner) {
				System.out.println("��ʼ�����"+count_i+" ��"+distinctowner.size()+"��");
				Thread thread  = new Thread(new ownerprocessing(str_owner,count_i));
				threadpool.execute(thread);
				count_i++;
			}
			try{
				threadpool.shutdown();
				threadpool.awaitTermination(20, TimeUnit.MINUTES);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}finally{
			try {
				statement.close();
				con.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new DataProcessing().dataprocess();
	}

}
