package flickrdownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import au.com.objectix.jgridshift.Util;

public class PerservanceThread implements Runnable {


	public String filepath = "";
	public PerservanceThread(String pathe) {
	
		this.filepath = pathe;
	}
	@Override
	public void run()
	{
		try{
			
			System.out.println("start to transfer"+filepath);
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filepath))));
			PhotoPage page = PhotoPage.ParseJson(reader.readLine());
			Connection connection = DataBase.GetPostgresqlConnection();
			page.WriteToDataBase(connection, DataBase.tablename);
			connection.close();
			System.out.println("finish "+ filepath);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DataBase.CreateFlickrTable();
		ExecutorService threadpool = Executors.newFixedThreadPool(500);
		BufferedReader reader = null;
		try {
			 //reader = new BufferedReader(new FileReader(new File("f:/flickrmetadata.txt")));
			File datadir = new File(Utils.BasePath);
			String[] names = datadir.list();
			for(int i=0;i<names.length;i++ )
			{
				String temppath = Utils.BasePath+names[i];
				System.out.println(temppath);
				PerservanceThread thread = new PerservanceThread(temppath);
				threadpool.execute(thread);
			}
			/*String path = null;
			while((path=reader.readLine())!=null)
			{
				PerservanceThread thread = new PerservanceThread(path);
				threadpool.execute(thread);
			}*/
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		
	}

}
