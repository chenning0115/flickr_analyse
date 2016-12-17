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

	//���Ա����
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
	
	//��ȡ����ĵ������
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
			if(deltx*deltx+delty*delty>radius) continue;//���г���ɸѡ������������·���ݾ��Ѿ������˰뾶����ô�Զ������õ�
			else if(dataSupporter.GetDistance(p1, p2)<=radius)
				{
					set.add(tempnode);
					//iterator.remove();
				}
		}
		curnode.pts = set.size();
		if(curnode.pts<minpts) {return null;}//���û�дﵽminpts�ı�׼���ͷ��ؿ�
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
	
	
	//�����û����øþ����㷨
	public void ExecuteDbscan()
	{
		//�����ܹ����ڶ�̬ɾ����Dbnode����
		LinkedList<Dbnode> list_dbnodes = new LinkedList<>();
		for(int i = 0;i<dataSupporter.points.length;i++)
		{
			Dbnode tempnode = new Dbnode(i,0, -2, dataSupporter.points[i]);
			list_dbnodes.add(tempnode);
		}
		//��ʼ��ÿһ�����ĵ���뼯�Ϸ��࣬�ڹ����л����ɾ����Ҫ�Ѿ��γ��ܶ�������ϵ�ĵ㣨���ڷǺ��ĵ㽫corenode��ֵΪ-1��
		boolean check = false;//���ڼ��list_dbnodes���Ƿ��Ѿ������ں��ĵ��ˣ�true����false������
		HashMap<Integer, LinkedList<Dbnode>> map_result = new HashMap<>();//�洢���еķ������ս��
		do{
			check = false;
			//ѭ��list_dbnodes��֮���Դ�ͷ��ʼ����ɨ������Ϊ�ں�����ɢʱ���list_nodesɾ������ڵ����ֹ��ɻ���
			Iterator<Dbnode> list_iterator = list_dbnodes.iterator();
			Dbnode tempndoe2 = null;
			LinkedList<Dbnode> list_curclass = new LinkedList<>();//�洢���κ��ĵ��Ӧ�����й����㣬�����γɴ������еĵ㼯��
			LinkedList<Dbnode> queue = new LinkedList<>();//����
			while(list_iterator.hasNext())
			{
				tempndoe2 = list_iterator.next();
				if(tempndoe2.corenode!=-1)
				{
					HashSet<Dbnode> set = GetNearDbnode(list_dbnodes, tempndoe2);
					if(set == null) {tempndoe2.corenode=-1;}
					else {
						check = true;//˵����ͨ���ҵ��˺��ĵ������ѭ����
						tempndoe2.corenode = 1;
						tempndoe2.classtype = classnum;
						//��ȡ����һ�����ĵ㣬��ʼ���ö��н�����ɢ
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
				map_result.put(classnum, list_curclass);//�ҵ��˸��������е�
				System.out.println("finished scan the class "+classnum);
				System.out.println("size="+list_curclass.size());
				classnum++;
			}
			if(!check)//����Ѿ������ں��ĵ�����ôʣ��Ķ�������������
			{
				map_result.put(-2, list_dbnodes);
			}
		}while(check);
		getresult(map_result);
	}
	
	
	
	
	
	
	
	
	
	public static class Dbnode
	{
		public int pointindex = 0;
		public int corenode = 0;//0����δ�жϣ�1�����Ǻ��ĵ㣬-1�����Ǻ��ĵ�
		public int classtype = 0;//-1���������㣬0����0���࣬1 ����1����...
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
