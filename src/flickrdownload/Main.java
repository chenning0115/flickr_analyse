package flickrdownload;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import javax.rmi.CORBA.Util;

public class Main {

	
	//�����߳���
	public class DownLoadThread implements Runnable
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			try{
				ExecutorService download = Executors.newFixedThreadPool(500);
				Calendar c = Calendar.getInstance();
				Date startdate = Utils.startdate;
				Date enddate;
				while (true)
				{
					c.setTime(startdate);
					c.add(c.MINUTE,Utils.inter_hour);
					//c.add(c.DAY_OF_MONTH, 1);
					enddate = c.getTime();
					download.execute(new DownLoader(startdate, enddate));
					startdate = enddate;
					if(startdate.compareTo(Utils.enddate)>=0) break;
				}
			}catch(Exception e)
			{
				
			}
		}
		
	}
	
	public class WriteToDB implements Runnable
	{
		@Override
		public void run()
		{
			ExecutorService threadpool = Executors.newFixedThreadPool(20);
			while(true)
			{
				//threadpool.execute(new PerservanceThread());
				break;
			}
		}
	}
	
	
	public void RunTask()
	{
		new Thread(new DownLoadThread()).start();
		//new Thread(new WriteToDB()).start();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try{
			Utils.BasePath = "g:/flickr_beijing/";
			Utils.inter_hour = 20;
			//DataBase.CreateFlickrTable();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			//Utils.savepaths = new ArrayBlockingQueue<>(1000);
			Utils.startdate =  format.parse("2005-01-01 00:00:00");
			Utils.enddate =  format.parse("2016-01-01 20:00:00");
			//Utils.linkedList = new LinkedList<String>();
			new Main().RunTask();
			
		}catch(Exception e)
		{
			
		}
		
	}

}
