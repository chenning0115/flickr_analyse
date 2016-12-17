package flickrdownload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import javax.rmi.CORBA.Util;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class PageDownLoader implements Runnable {

	public int perpage = 500;
	public int pagenumber = 0;
	public String startdatetime = "2015-06-01 00:00:00";
	public String enddatetime = "2015-06-01 02:00:00";
	public String path;
	
	public PageDownLoader(int i_pagenumber,int i_perpage,String i_startdatetime,String i_enddatetime,String i_path) {
		// TODO Auto-generated constructor stub
		this.pagenumber = i_pagenumber;
		this.perpage = i_perpage;
		this.startdatetime = i_startdatetime;
		this.enddatetime = i_enddatetime;
		this.path = i_path;
	}
	@Override
	public void run()
	{
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try{
			HttpPost httpPost = new HttpPost("https://api.flickr.com/services/rest");
            List <NameValuePair> nvps = new ArrayList <NameValuePair>();
            //nvps.add(new BasicNameValuePair("api_sig", "2a7c813f9db426f8747fbd7068429b66"));
            nvps.add(new BasicNameValuePair("page",""+pagenumber));
            nvps.add(new BasicNameValuePair("per_page", ""+perpage));
            nvps.add(new BasicNameValuePair("bbox", "115.421681,39.411044,117.526946,41.083001"))	;
            nvps.add(new BasicNameValuePair("accuracy", "1"));
            //nvps.add(new BasicNameValuePair("sort", "date-taken-desc"));
            nvps.add(new BasicNameValuePair("min_taken_date",startdatetime));
            nvps.add(new BasicNameValuePair("max_taken_date",enddatetime));
            nvps.add(new BasicNameValuePair("safe_search", "1"));
            nvps.add(new BasicNameValuePair("extras","date_upload,description,date_taken,owner_name,geo,tags,o_dims,views"));
            nvps.add(new BasicNameValuePair("format","json"));
            nvps.add(new BasicNameValuePair("nojsoncallback","1"));
            nvps.add(new BasicNameValuePair("ticket_number","3"));
            nvps.add(new BasicNameValuePair("method", "flickr.photos.search"));
            nvps.add(new BasicNameValuePair("src", "js"));
            nvps.add(new BasicNameValuePair("api_key", "348bd334430e96ad15b80b5ed871162a"));
            //nvps.add(new BasicNameValuePair("auth_hash", "e5d0ddffab9f88c88a098b7cb15297a7"));
            //nvps.add(new BasicNameValuePair("auth_token", ""));
            //nvps.add(new BasicNameValuePair("radius", "20"));
            //nvps.add(new BasicNameValuePair("has_geo", "1"));
            nvps.add(new BasicNameValuePair("cb", "1437805339231"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));
            //System.out.println("start to download the page"+pagenumber);
            CloseableHttpResponse response2 = httpclient.execute(httpPost);
            try {
                //System.out.println(response2.getStatusLine()+"finish downloading "+pagenumber+"!");
                HttpEntity entity2 = response2.getEntity();
                InputStream ins = entity2.getContent();

                
                FileOutputStream fout = new FileOutputStream(new File(path));
                byte[] data = new byte[2048];
                int readindex = 0;
                while((readindex = ins.read(data, 0, 2048))!=-1)
                {
                	fout.write(data,0,readindex);
                }
                fout.flush();
                fout.close();
                
                EntityUtils.consume(entity2);
            } finally {
                response2.close();
            }
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}finally {
            try {
				httpclient.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
		
	}

}
