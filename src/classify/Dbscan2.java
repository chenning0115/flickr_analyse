                                                                                                              package classify;

import java.security.KeyStore.PrivateKeyEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.batik.transcoder.TranscodingHints;
import org.geotools.xml.xsi.XSISimpleTypes.Int;
import org.hsqldb.types.BlobDataID;
import org.omg.CORBA.PUBLIC_MEMBER;

import com.mongodb.util.Hash;

import classify.Dbscan.dbnode;

public class Dbscan2 {

	//类成员变量
	private DataSupporter dataSupporter;
	public  ClassifyResult classifyResult;
	
	private int minpts = 10;
	private double radius = 1e-5;
	public int classnum = 0;
	
	public Dbscan2(DataSupporter _dDataSupporter,double _radius,int _minpts)
	{
		this.radius = _radius;
		this.minpts = _minpts;
		this.dataSupporter = _dDataSupporter;
		
	}
	
	//获取最近的点的引用
	private HashSet<Dbnode> GetNearDbnode(LinkedList<Dbnode> list,Dbnode curnode)
	{
		HashSet<Dbnode> set = new HashSet<>();
		Iterator<Dbnode> iterator = list.iterator();
		while(iterator.hasNext())
		{
			Dbnode tempnode = iterator.next();
			FlickrPoint p1 = curnode.point;
			FlickrPoint p2 = tempnode.point;
			double deltx = p1.lon-p2.lon;
			double delty = p1.lat - p2.lat;
			if(deltx*deltx+delty*delty>radius) continue;//进行初次筛选，如果不加入道路数据就已经超过了半径，那么自动跳过该点
			else if(dataSupporter.GetDistance(p1, p2)<=radius)
				{
					set.add(tempnode);
					//iterator.remove();
				}
		}
		curnode.pts = set.size();
		if(curnode.pts<minpts) {return null;}//如果没有达到minpts的标准，就返回空
		else{
			for (Dbnode dbnode : set) {
	 			list.remove(dbnode);
			}
			return set;
		}
		
	}
	
	private void getresult(HashMap<Integer,LinkedList<Dbnode>> map)
	{
		int[] classtypearray = new int[dataSupporter.points.length];
		FlickrPoint[] centers = new FlickrPoint[classnum];
		for ( Entry<Integer, LinkedList<Dbnode>> entry : map.entrySet()) {
			int classtype = entry.getKey();
			int maxpts = Integer.MIN_VALUE;
			Dbnode maxnode = null;
			for (Dbnode tempnode : entry.getValue()) {
				classtypearray[tempnode.pointindex] = tempnode.classtype;
				if(tempnode.pts>maxpts)
				{
					maxpts = tempnode.pts;
					maxnode = tempnode;
				}
			}
			if(maxnode!=null&&maxnode.classtype>=0)centers[maxnode.classtype] = maxnode.point;
		}
		classifyResult = new ClassifyResult(dataSupporter, classnum, classtypearray, centers);
	}
	
	
	//用于用户调用该聚类算法
	public void ExecuteDbscan()
	{
		//创建能够用于动态删除的Dbnode序列
		LinkedList<Dbnode> list_dbnodes = new LinkedList<>();
		for(int i = 0;i<dataSupporter.points.length;i++)
		{
			Dbnode tempnode = new Dbnode(i,0, -2, dataSupporter.points[i]);
			list_dbnodes.add(tempnode);
		}
		//开始对每一个核心点加入集合分类，在过程中会逐个删除需要已经形成密度相连关系的点（对于非核心点将corenode赋值为-1）
		boolean check = false;//用于检查list_dbnodes中是否已经不存在核心点了，true存在false不存在
		HashMap<Integer, LinkedList<Dbnode>> map_result = new HashMap<>();//存储所有的分类最终结果
		do{
			check = false;
			//循环list_dbnodes，之所以从头开始重新扫描是因为在后面扩散时会对list_nodes删除多个节点而防止造成混乱
			Iterator<Dbnode> list_iterator = list_dbnodes.iterator();
			Dbnode tempndoe2 = null;
			LinkedList<Dbnode> list_curclass = new LinkedList<>();//存储本次核心点对应的所有关联点，最终形成此类所有的点集合
			LinkedList<Dbnode> queue = new LinkedList<>();//队列
			while(list_iterator.hasNext())
			{
				tempndoe2 = list_iterator.next();
				if(tempndoe2.corenode!=-1)
				{
					HashSet<Dbnode> set = GetNearDbnode(list_dbnodes, tempndoe2);
					if(set == null) {tempndoe2.corenode=-1;}
					else {
						check = true;//说明是通过找到了核心点而跳出循环的
						tempndoe2.corenode = 1;
						tempndoe2.classtype = classnum;
						//获取到了一个核心点，开始利用队列进行扩散
						list_curclass.add(tempndoe2);
						list_dbnodes.remove(tempndoe2);
						queue.addAll(set);
						Dbnode tempndoe3 = null;
						while(queue.size()!=0)
						{
							tempndoe3 = queue.poll();
							HashSet<Dbnode> tempset = GetNearDbnode(list_dbnodes, tempndoe3);
							tempndoe3.classtype = classnum;
							if(tempset==null)
							{
								tempndoe3.corenode = -1;
							}else{
								tempndoe3.corenode=1;
								queue.addAll(tempset);
							}
							list_curclass.add(tempndoe3);
						}
						break;
					}
				}
			}
			if(list_curclass.size()>0)
			{
				map_result.put(classnum, list_curclass);//找到了该类别的所有点
				System.out.println("finished scan the class "+classnum);
				System.out.println("size="+list_curclass.size());
				classnum++;
			}
			if(!check)//如果已经不存在核心点了那么剩余的东西都是噪音点
			{
				map_result.put(-2, list_dbnodes);
			}
		}while(check);
		getresult(map_result);
	}
	
	
	
	
	
	
	
	
	
	public static class Dbnode
	{
		public int pointindex = 0;
		public int corenode = 0;//0代表未判断，1代表是核心点，-1代表不是核心点
		public int classtype = 0;//-1代表噪音点，0代表0号类，1 代表1号类...
		public int pts = 0;
		public FlickrPoint point = null;
		
		public Dbnode()
		{
			
		}
		public Dbnode(int _pointindex,int _corenode,int _classtype,FlickrPoint _point)
		{
			this.pointindex = _pointindex;
			this.corenode = _corenode;
			this.classtype = _classtype;
			this.point = _point;
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
