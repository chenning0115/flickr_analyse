package classify;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

public class ToSHP {

	
	FlickrPoint[] points;
	int[] result;
	//FlickrPoint[] centers;
	int k;
	String path;
	
	public ToSHP(FlickrPoint[] ori_points,int[] ori_result,int ori_k,String ori_path)
	{
		this.points = ori_points;
		this.result = ori_result;
		this.k = ori_k;
		//this.centers = ori_centers;
		this.path = ori_path;
	}
	
	private ShapefileDataStore getSHPFileDataStore(File file,SimpleFeatureType type)throws Exception
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
	private void writetoshp(ArrayList<SimpleFeature> features,String path,SimpleFeatureType type)throws Exception
	{
		ShapefileDataStore dataStore = getSHPFileDataStore(new File(path),type);
	    String typename = dataStore.getTypeNames()[0];
	    SimpleFeatureSource featureSource = dataStore.getFeatureSource(typename);
	    Transaction transaction = new DefaultTransaction("create");
	    if(featureSource instanceof SimpleFeatureStore)
	    {
	    	SimpleFeatureStore store = (SimpleFeatureStore)featureSource;
	    	SimpleFeatureCollection collection = new ListFeatureCollection(type,features);
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
	    	//System.exit(0);
	    	
	    }
	    else {
	    	System.out.println(typename+"does not support read/write access");
	    	System.exit(1);
	    }
	}
	
	public void Toshapefile()
	{
		SimpleFeatureType type;
		String filepath_class = path+"_"+k+"_.shp";
		
		try{
			SimpleFeatureTypeBuilder typebuilder = new SimpleFeatureTypeBuilder();
			typebuilder.setName("flickr");
			CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
			typebuilder.setCRS(crs);
			typebuilder.add("the_geom",Point.class);
			typebuilder.add("id",String.class);
			typebuilder.add("class",Integer.class);
			typebuilder.setDefaultGeometry("the_geom");
			type = typebuilder.buildFeatureType();
	        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
	        WKTReader reader = new WKTReader(geometryFactory);
	        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(type);
	       
	        
	        ArrayList<SimpleFeature> features_class = new ArrayList<SimpleFeature>();
	        for(int i = 0;i<points.length;i++)
	        {
	        	Point p = (Point) reader.read("POINT("+points[i].lon+" "+points[i].lat+")");
	        	featureBuilder.add(p);
	        	featureBuilder.add(points[i].ID);
	        	featureBuilder.add(result[i]);
	        	features_class.add(featureBuilder.buildFeature(null));
	        }
	       // System.out.println("build class features successfullly!");
	        writetoshp(features_class, filepath_class, type);
			//System.out.println("class features write to shp success");

	        
		}catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
