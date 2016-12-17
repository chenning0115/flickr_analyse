package flickrdownload;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.geotools.xml.xLink.XLinkSchema.From;

import com.mysql.jdbc.Connection;

public class ClassifyByLocation {

	
	private ArrayList<String> location = new ArrayList<>();
	
	
	
	private void GetLocations()throws Exception
	{
		java.sql.Connection con = DataBase.GetPostgresqlConnection();
		Statement statement = con.createStatement();
		String sql = "select name from "+DataBase.table_provincename+" ;";
		ResultSet rs = statement.executeQuery(sql);
		while(rs.next())
		{
			location.add(rs.getString(1));
		}
		statement.close();
		con.close();
	}
	
	public class UpdateThread implements Runnable
	{

		String cur_location ="±±¾©";
		public UpdateThread(String cur_loaction) {
			// TODO Auto-generated constructor stub
			this.cur_location = cur_loaction;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try {
				java.sql.Connection connection = DataBase.GetPostgresqlConnection();
				Statement statement = connection.createStatement();
				String sql = "update "+DataBase.tablename+" as t1"
						+ " set sign = '"+cur_location+"' "
								+ "where((select ST_Covers("+DataBase.table_provincename+".geom,t1.geom) from "+DataBase.table_provincename
										+ " where "+DataBase.table_provincename+".name_1 = '"+cur_location+"')='t')";
				System.out.println("SQL:"+sql);
				
				statement.execute(sql);
				statement.close();
				connection.close();
				System.out.println("finish updating "+ cur_location);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public void Classify()
	{
		try {
			//GetLocations();
			location.add("beijing");
			//location.add("guangdong");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ExecutorService threadpool = Executors.newFixedThreadPool(30);
		for (String string : location) {
			System.out.println("start to update "+string);
			Runnable thread = new UpdateThread(string);
			threadpool.execute(thread);
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		new ClassifyByLocation().Classify();
	}

}
