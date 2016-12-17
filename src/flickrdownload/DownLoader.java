package flickrdownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.rmi.CORBA.Util;


public class DownLoader implements Runnable {

	public Date startdate;
	public Date enddate;
	public DownLoader(Date i_startdate,Date i_enddate)
	{
		this.startdate = i_startdate;
		this.enddate = i_enddate;
	}
	
	public void startdownload()throws Exception
	{
		SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMddHHmmss");
		String cur_path = Utils.BasePath+format2.format(startdate)+"_"+format2.format(enddate)+"_1"+".txt";
		
		System.out.println("Get the sumpage of "+cur_path);
		//System.out.println(format1.format(startdate));
		PageDownLoader d = new PageDownLoader(1, 500, format1.format(startdate), format1.format(enddate),cur_path);
		d.run();
		//Utils.writer.println(cur_path);
        //Utils.writer.flush();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(cur_path))));
		PhotoPage page = PhotoPage.ParseJson(reader.readLine());
		int sumpage = page.sumpage;
		int perage = page.photonum;
		for(int i = 2;i<=sumpage;i++)
		{
			cur_path = Utils.BasePath+format2.format(startdate)+"_"+format2.format(enddate)+"_"+i+".txt";
			System.out.println("start to download +"+cur_path);
			PageDownLoader tempd = new PageDownLoader(i,perage,format1.format(startdate), format1.format(enddate),cur_path);
			tempd.run();
			//Utils.savepaths.put(cur_path);
			Utils.writer.println(cur_path);
            Utils.writer.flush();
			System.out.println("finish to download "+cur_path);
		}
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			startdownload();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
	}

	

}
