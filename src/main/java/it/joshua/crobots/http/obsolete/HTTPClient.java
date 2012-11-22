/*
 * Created on 21-lug-2006
 *
 */
package it.joshua.crobots.http.obsolete;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author mcamangi
 *
 */
@Deprecated
public class HTTPClient {

	private Credentials creds ;
	private HttpClient client ;
	private HttpMethod method ;
	//private String responseBody = null;
	
	public String user, pwd;
	private static final Logger httplogger = Logger.getLogger(org.apache.commons.httpclient.HttpClient.class.getName());
	
	public void connect(String url) {

	 	int retry = 0;
	 	boolean good_retry = false;

	 	creds = new UsernamePasswordCredentials(user, pwd);
        //create a singular HttpClient object
        client = new HttpClient();
        
        //establish a connection within 5 seconds
        client.getHttpConnectionManager().
            getParams().setConnectionTimeout(5000);
        //      set the default credentials
        client.getState().setCredentials(AuthScope.ANY, creds);

        method = null;

        //create a method object
        method = new GetMethod(url);
        method.setFollowRedirects(true);
        do {
	        try{
	            client.executeMethod(method);
	            good_retry = true;
	            //responseBody = method.getResponseBodyAsString();
	        } catch (HttpException he) {
	        	//httplogger.error("Http error connecting to '" + url + "'");
	        	httplogger.warning("Attempt " + retry + " : " + he.getMessage());
	            //System.exit(-4);
	        } catch (IOException ioe) {
	        	//httplogger.error("Unable to connect to '" + url + "'");
	        	httplogger.warning("Attempt " + retry + " : " + ioe.getMessage());
	            //System.exit(-3);
	        }
        } while(!good_retry && (retry++ < 2));
	}
	
	public void close() {
		method.releaseConnection();
	}
	
	public String doQuery(String url) throws IOException {
		String responseBody = null;
		
		connect(url);
		responseBody = method.getResponseBodyAsString();
		close();
		return responseBody;
	}

	public String[] doQueryStream(String url) {
		
		InputStream responseBody = null;
		String[] lines = new String[32];
		int n = 0;
		
		connect(url);
		try
		{
			responseBody = method.getResponseBodyAsStream();
			if (responseBody != null) {
				BufferedReader input = new BufferedReader( new InputStreamReader(responseBody));
				try {
					lines[n++] = input.readLine();
					while(input.ready()) {
						lines[n++] = input.readLine();
					}
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					httplogger.severe(e1.getMessage());
				}
				finally
				{
					input.close();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			httplogger.severe(e.getMessage());
		}
		close();			
		return lines;
	}

	public HTTPClient(String username, String password) {
		user = username;
		pwd  = password;
		httplogger.setLevel(Level.WARNING);
	}
}
