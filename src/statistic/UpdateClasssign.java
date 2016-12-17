package statistic;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;


import flickrdownload.DataBase;

public class UpdateClasssign {

	String filepath = "updateclasssign.txt";
	String tablename = "classsign";
	
	public void update1(int des,ArrayList<Integer> list,Statement sta)throws Exception
	{
		for(int i=0;i<list.size();i++)
		{
			String sql = "update "+tablename+" set classindex="+des+" where classindex="+list.get(i)+";";
			sta.executeUpdate(sql);
		}
	}
	public void updateall()
	{
		try{
			Connection connection = DataBase.GetPostgresqlConnection();
			Statement statement = connection.createStatement();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			String line = "";
			while((line=reader.readLine())!=null)
			{
				String[] ss = line.split(",");
				int des = Integer.parseInt(ss[0]);
				ArrayList<Integer> list = new ArrayList<>();
				String[] ss1 = ss[1].split(" ");
				for(int i=0;i<ss1.length;i++)
				{
					list.add(Integer.parseInt(ss1[i]));
				}
				update1(des, list, statement);
				System.out.println("update "+line);
			}
			statement.close();
			connection.close();
			System.out.println("update all success");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
