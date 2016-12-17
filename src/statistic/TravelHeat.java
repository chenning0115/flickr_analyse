package statistic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.internal.cde.DtActionArg;
import org.eclipse.ui.internal.intro.IIntroConstants;

import com.ibm.icu.text.SimpleDateFormat;

import flickrdownload.DataBase;
import thredds.cataloggen.config.ResultService;


public class TravelHeat {

	public final static int temp_heat  = 0;
	public final static int temp_warm = 1;
	public final static int temp_cool = 2;
	public final static int temp_cold = 3;
	public final static int des_sun = 0;
	public final static int des_rain = 1;
	public final static int des_snow = 2;
	public static class Entity
	{
		public String id;
		public Date datetaken;
		public int classindex;
		public int temp;
		public int des;
		
		public Entity(String _id,Date _datetaken,int _classindex,int _temp,int _des)
		{
			this.id = _id;
			this.datetaken = _datetaken;
			this.classindex = _classindex;
			this.temp = _temp;
			this.des = _des;
		}
	}
	public static int gettempsign(int temp)
	{
		if(temp>30) return temp_heat;
		if(temp>18) return temp_warm;
		if(temp>5) return temp_cool;
		return temp_cold;
	}
	public static int getstasign(String sta)
	{
		if(sta.contains("雪")) return des_snow;
		if(sta.contains("雨")) return des_rain;
		return des_sun;
	}
	public  ArrayList<Entity> datalist;
	public HashMap<Integer, Integer> map_classindex;
	public HashMap<Integer, Integer> remap_classindex;
	
	public void getdata(String classname)
	{
		int num=0;
		try{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Connection connection = DataBase.GetPostgresqlConnection();
			Statement statement = connection.createStatement();
			String sql = "select id,datetaken,classindex,temp_avg,statement from "+classname;
			ResultSet set = statement.executeQuery(sql);
			while(set.next())
			{
				int classindex = set.getInt("classindex");
				if(classindex<0) continue;
				String _id = set.getString("id");
				Date _Date = format.parse(set.getString("datetaken"));
				int _temp_avg = set.getInt("temp_avg");
				String _statement = set.getString("statement");
				Entity entity = new Entity(_id, _Date, classindex,gettempsign(_temp_avg),getstasign(_statement));
				datalist.add(entity);
				if(!map_classindex.containsKey(classindex))
				{
					map_classindex.put(classindex, num);
					remap_classindex.put(num, classindex);
					num++;
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
//	函数
//	获取季度
	public int getquarter(Date date)
	{
		int mon = date.getMonth()+1;
		if(mon<=3) return 1;
		if(mon<=6) return 2;
		if(mon<=9) return 3;
		return 4;
	}
	public int getgongzuoriorzhoumo(Date date)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int weekindex = calendar.get(Calendar.DAY_OF_WEEK);
		if(weekindex==Calendar.SUNDAY || weekindex==Calendar.SATURDAY)
			return 1;
		return 0;
	}
	public int gettianneishiduan(Date date)
	{
		int hour = date.getHours();
		if(hour>=6&&hour<12) return 0;
		if(hour>=12&&hour<18) return 1;
		return 2;
	}
	//工作日上，工作日下，工作日晚，周末上，周末下，周末晚
	public int getgongzuorizhoumoandtianneiindex(int gongzuori,int tiannei)
	{
		if(gongzuori==0&&tiannei==0) return 0;
		if(gongzuori==0&&tiannei==1) return 1;
		if(gongzuori==0&&tiannei==2) return 2;
		if(gongzuori==1&&tiannei==0) return 3;
		if(gongzuori==1&&tiannei==1) return 4;
		if(gongzuori==1&&tiannei==2) return 6;
		return 0;
		
	}
	
	int classnum = map_classindex.size();
	public int[] heat1 = new int[classnum];
	public int[][] heat2 = new int[classnum][4];
	public int[][] heat3 = new int[classnum][12];
	public int[][] heat4 = new int[classnum][2];
	public int[][] heat5 = new int[classnum][2];
	public int[][] heat6 = new int[classnum][4];
	public int[][] heat7 = new int[classnum][6];//工作日上，工作日下，工作日晚，周末上，周末下，周末晚
//	public int[][] heat8 = new int[classnum][4];
	public int[][] heat9 = new int[classnum][4];
	public int[][] heat10 = new int[classnum][3];
	public int[][] heat11 = new int[classnum][3];
	public int[][] heat12 = new int[classnum][3];
	public int[][] heat13 = new int[classnum][3];
	public int[][] heat14 = new int[classnum][3];
	public int[][] heat15 = new int[classnum][3];
	public int[][] heat16 = new int[classnum][3];
	public int[][] heat17 = new int[classnum][4];
	public int[][] heat18 = new int[classnum][4];
	public int[][] heat19 = new int[classnum][4];
	public int[][] heat20 = new int[classnum][4];
	
	public void processEntity(Entity e)
	{
		int classindex = map_classindex.get(e.classindex);
		int quarterindex = getquarter(e.datetaken)-1;
		int monthindex = e.datetaken.getMonth();
		int danwangjiindex = 0;
		Date start = new Date(e.datetaken.getYear(), 2, 16);
		Date end = new Date(e.datetaken.getYear(), 10, 14);
		if (e.datetaken.after(start) && e.datetaken.before(end)) danwangjiindex = 1;
		int gongzuoriorzhoumoindex = getgongzuoriorzhoumo(e.datetaken);
		int tianneishiduan = gettianneishiduan(e.datetaken);
		
		heat1[classindex]+=1;
		heat2[classindex][quarterindex]+=1;
		heat3[classindex][monthindex]+=1;
		heat4[classindex][danwangjiindex]+=1;
		heat5[classindex][gongzuoriorzhoumoindex]+=1;
		heat6[classindex][tianneishiduan]+=1;
		heat7[classindex][getgongzuorizhoumoandtianneiindex(gongzuoriorzhoumoindex, tianneishiduan)]+=1;
		heat9[classindex][e.temp]+=1;
		heat10[classindex][e.des]+=1;
		switch (e.temp) {
		case TravelHeat.temp_warm:
			heat11[classindex][e.des]+=1;
			break;
		case TravelHeat.temp_heat:
			heat12[classindex][e.des]+=1;
			break;
		case TravelHeat.temp_cool:
			heat13[classindex][e.des]+=1;
			break;
		case TravelHeat.temp_cold:
			heat14[classindex][e.des]+=1;
			break;
		default:
			break;
		}
		switch (danwangjiindex) {
		case 0:
			heat15[classindex][e.des]+=1;
			break;
		case 1:
			heat16[classindex][e.des]+=1;
			break;
		default:
			break;
		}
		switch (gongzuoriorzhoumoindex) {
		case 0:
			heat17[classindex][e.des]+=1;
			break;
		case 1:
			heat18[classindex][e.des]+=1;
			switch (tianneishiduan) {
			case 0:
				heat19[classindex][e.des]+=1;
				break;
			case 1:
				heat20[classindex][e.des]+=1;
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
		
	}
	
 
	private void output1(String path,String colnames,int[][] data)
	{
		int row = data.length;
		int col = data[0].length;
		try{
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(path))));
//			writer.print("classindex");
//			for(int i=0;i<col;i++)
//			{
//				writer.print(","+i);
//			}
			writer.println("景点名称,"+colnames);
//			writer.println("");
			for(int i=0;i<row;i++)
			{
				writer.print(""+remap_classindex.get(i));
				for(int j=0;j<col;j++)
				{
					writer.print(","+data[i][j]);
				}
				writer.println("");
			}
			writer.flush();
			writer.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.out.println("output "+path+" success");
		
	}
	public void outputresult(String path)
	{
		File file = new File(path);
		if(!file.exists()){
			file.mkdir();
		}
		String temppath = "";
		temppath = path+"/2.csv";
		output1(temppath,"一季度,二季度,三季度,四季度" ,heat2);
		temppath = path+"/3.csv";
		output1(temppath,"1月,2月,3月,4月,5月,6月,7月,8月,9月,10月,11月,12月", heat3);
		temppath = path+"/4.csv";
		output1(temppath,"淡季,旺季", heat4);
		temppath = path+"/5.csv";
		output1(temppath, "工作日,周末",heat5);
		temppath = path+"/6.csv";
		output1(temppath,"上午,下午,白天,晚上", heat6);
		temppath = path+"/7.csv";
		output1(temppath,"工作日上午,工作日下午,工作日晚上,周末上午,周末下午,周末晚上", heat7);
		temppath = path+"/9.csv";
		output1(temppath,"炎热,温暖,温凉,寒冷", heat9);
		temppath = path+"/10.csv";
		output1(temppath,"晴,云,雨,雪", heat10);
		temppath = path+"/11.csv";
		output1(temppath,"温暖且天晴,温暖且下雨,温暖且下雪", heat11);
		temppath = path+"/12.csv";
		output1(temppath,"炎热且天晴,炎热且下雨,炎热且下雪", heat12);
		temppath = path+"/13.csv";
		output1(temppath,"温凉且天晴,温凉且下雨,温凉且下雪", heat13);
		temppath = path+"/14.csv";
		output1(temppath,"寒冷且天晴,寒冷且下雨,寒冷且下雪", heat14);
		temppath = path+"/15.csv";
		output1(temppath, "淡季且天晴,淡季且下雨,淡季且下雪",heat15);
		temppath = path+"/16.csv";
		output1(temppath,"旺季且天晴,旺季且下雨,旺季且下雪", heat16);
		temppath = path+"/17.csv";
		output1(temppath,"工作日且天晴,工作日且下雨,工作日且下雪", heat17);
		temppath = path+"/18.csv";
		output1(temppath,"周末且天晴,周末且下雨,周末且下雪", heat18);
		temppath = path+"/19.csv";
		output1(temppath,"周末上午且天晴,周末上午且下雨,周末上午且下雪", heat19);
		temppath = path+"/20.csv";
		output1(temppath,"周末下午且天晴,周末下午且下雨,周末下午且下雪", heat20);
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
