package flickrshape;


/*
 * 本类用于将数据库中的原始点数据读取出来并转化为shp文件
 * 
 * 对于分析来讲用处并不大
 * 
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.*;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

import flickrdownload.DataBase;



import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class ToShapeFile implements Runnable {

	String startdate;
	String enddate;
	String path;
	SimpleFeatureType type;
	
	public ToShapeFile(String s,String e,String path)
	{
		this.startdate = s;
		this.enddate = e;
		this.path = path;
		try{
			SimpleFeatureTypeBuilder typebuilder = new SimpleFeatureTypeBuilder();
			typebuilder.setName("flickr");
			
			CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
			typebuilder.setCRS(crs);
			typebuilder.add("the_geom",Point.class);
			typebuilder.add("id",String.class);
			typebuilder.add("owner",String.class);
			typebuilder.add("sign",String.class);
			typebuilder.setDefaultGeometry("the_geom");
			type = typebuilder.buildFeatureType();
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public ResultSet ExecuteQuery(Statement statement)throws SQLException
	{
		String sql = "select ST_AsText(geom),id,owner,sign from "+DataBase.tablename
				+ " where "
				+ "sign ='beijing' and "
				+ " "+DataBase.tablename+".datetaken between '"
				+startdate+ "' and '"
				+enddate+ "';";
		System.out.println(sql);
		ResultSet rSet = statement.executeQuery(sql);
		return rSet;
		
	}
	
	public ShapefileDataStore getSHPFileDataStore(File file)throws Exception
	{
		/*
		 * 
         * Get an output file name and create the new shapefile
         */
        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put("url", file.toURI().toURL());
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);

        /*
         * TYPE is used as a template to describe the file contents
         */
        newDataStore.createSchema(type);
        return newDataStore;
	}
	
	public List<SimpleFeature> BuildFeatures(ResultSet rs)throws Exception
	{
		
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
        WKTReader reader = new WKTReader(geometryFactory);

        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
        while(rs.next())
        {
        	Point p = (Point)reader.read(rs.getString(1));
        	featureBuilder.add(p);
        	//System.out.println("x="+p.getX()+" y="+p.getY());
        	featureBuilder.add(rs.getString(2));
        	featureBuilder.add(DataBase.decode(rs.getString(3)));
        	featureBuilder.add(rs.getString(4));
        	features.add(featureBuilder.buildFeature(null));
        }
        return features;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try{
			Connection con = DataBase.GetPostgresqlConnection();
			Statement statement = con.createStatement();
			ResultSet rs = ExecuteQuery(statement);
			System.out.println("Query successfully!");
		    List<SimpleFeature> featuress = BuildFeatures(rs);
		    System.out.println("BuildFeatures Successfully!");
		   /* for (SimpleFeature simpleFeature : featuress) {
				for (Object value : simpleFeature.getAttributes()) {
					System.out.print(value);
				}
				System.out.println("");
			}*/
		    ShapefileDataStore dataStore = getSHPFileDataStore(new File(path));
		    String typename = dataStore.getTypeNames()[0];
		    SimpleFeatureSource featureSource = dataStore.getFeatureSource(typename);
		    Transaction transaction = new DefaultTransaction("create");
		    if(featureSource instanceof SimpleFeatureStore)
		    {
		    	SimpleFeatureStore store = (SimpleFeatureStore)featureSource;
		    	SimpleFeatureCollection collection = new ListFeatureCollection(type,featuress);
		    	try{
		    		store.addFeatures(collection);
		    		transaction.commit();
		    	}
		    	catch(Exception e)
		    	{
		    		e.printStackTrace();
		    		transaction.rollback();
		    	}
		    	finally{
		    		transaction.close();
		    	}
		    	System.out.println("To shp successfully! now exit!");
		    	System.exit(0);
		    	
		    }
		    else {
		    	System.out.println(typename+"does not support read/write access");
		    	System.exit(1);
		    }
		}catch(Exception exception )
		{
			exception.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		try{
			//new ToShapeFile("2015-06-01 00:00:00", "2015-07-01 00:00:00","f:/shp/Jun.shp").run();
			new ToShapeFile("2005-01-01 00:00:00", "2016-01-01 00:00:00","f:/flickrtest/beijing2005_2016_processed.shp").run();
			//new Thread(shp_6).start();
			//new Thread(shp_7).start();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}


}
