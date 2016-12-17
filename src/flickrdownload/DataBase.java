package flickrdownload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

public class DataBase {

	 private static BasicDataSource dataSource = null;
	public static String tablename = "flickr_beijing_2015";
	public static String table_provincename = "province";
	public static String table_roadname = "road_beijing_new";
	//get the postgresql connection
	public static Connection POST_GetPostgresqlConnection()throws Exception
	{
		Class.forName( "org.postgresql.Driver" ).newInstance();
        String url = "jdbc:postgresql://localhost:5432/postgres" ;
        Connection con = DriverManager.getConnection(url, "postgres" , "admin" );
        return con;
	}
	
	public static synchronized Connection GetPostgresqlConnection()throws  SQLException { 
        if (dataSource == null) { 
            init(); 
        } 
        Connection conn = null; 
        if (dataSource != null) { 
            conn =dataSource.getConnection(); 
        } 
        return conn; 
    } 
	public static void init() {

	       if (dataSource != null) { 
	            try { 
	               dataSource.close(); 
	            } catch(Exception e) { 
	               // 
	            } 
	            dataSource =null; 
	        }

	       try { 
	           Properties p= new Properties(); 
	           p.setProperty("driverClassName","org.postgresql.Driver"); 
	           p.setProperty("url","jdbc:postgresql://localhost:5432/flickr"); 
	           p.setProperty("password", "admin"); 
	           p.setProperty("username", "postgres"); 
	           p.setProperty("maxActive", "500"); 
	           p.setProperty("maxIdle", "490"); 
	           p.setProperty("maxWait", "500"); 
	           p.setProperty("removeAbandoned", "false"); 
	           p.setProperty("removeAbandonedTimeout", "120"); 
	           p.setProperty("testOnBorrow", "true"); 
	           p.setProperty("logAbandoned", "true");

	           dataSource = (BasicDataSource) BasicDataSourceFactory.createDataSource(p);

	       } catch (Exception e) { 
	            // 
	        } 
	    }
	//创建表
	public static void CreateFlickrTable()
	{
		String sql = "CREATE TABLE "+tablename+" ("
  +"id VARCHAR(12) NOT NULL,"
  +"owner VARCHAR(20) NULL,"
  +"title TEXT NULL,"
  +"geom geometry(Point,4326),"
  +"description TEXT NULL,"
  +"dateupload VARCHAR(20) NOT NULL,"
  +"datetaken timestamp NOT NULL,"
  +"ownername VARCHAR(100) NULL,"
  +"accuracy INT NOT NULL DEFAULT 1,"
  +"tags TEXT NULL,"
  +"secret VARCHAR(30) NULL,"
  +"server INT NULL,"
  +"farm INT NULL,"
  +"ispublic INT NULL,"
  +"isfriend INT NULL,"
  +"isfamily INT NULL,"
  +"safe INT NULL,"
  +"o_width INT NULL,"
  +"o_height INT NULL,"
  +"views INT NULL,"
  +"place_id VARCHAR(50) NULL,"
  +"woeid VARCHAR(10) NULL,"
  +"geo_is_family INT NULL,"
  +"geo_is_friend INT NULL,"
  +"geo_is_contact INT NULL,"
  +"geo_is_public INT NULL,"
  +"PRIMARY KEY (id));";
		try{
			Connection con = GetPostgresqlConnection();
			Statement statement = con.createStatement();
			statement.execute(sql);
			statement.close();
			con.close();
			System.out.println("create table successfully!");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		
	}
	
	/** 
     * @param bytes 
     * @return 
     */  
    public static String decode(final String source)throws Exception {  
        return  new String(Base64.decodeBase64(source.getBytes("UTF-8")));  
    }  
  
    /** 
     * 二进制数据编码为BASE64字符串 
     * 
     * @param bytes 
     * @return 
     * @throws Exception 
     */  
    public static String encode(final String source)throws Exception {  
        return new String(Base64.encodeBase64(source.getBytes("GBK")));  
    }  
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CreateFlickrTable();
		
	}

}
