package it.cloudhome.android.fabtel;

import java.io.*;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.os.AsyncTask;
import android.util.Log;


public class ClsUtility {


    private String strUrl,strUsr,strPwd,strResult;

	/*****************************************
	 * 
	 *           Funzioni WEB
	 * 
	 *****************************************/
	
    //Get Web Page content
    public String GetWebPage(String indirizzo)
    {
    	return GetWebPage(indirizzo, "", "");
    }
    //Get Web Page content with basic Authentication (GET method)
    public String GetWebPage(String indirizzo,String username, String password)
    {


    	String result ="";
        strUrl=indirizzo;
        strUsr=username;

        try
        {
            HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 20000);
            HttpConnectionParams.setSoTimeout(httpParams, 30000);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
            HttpContext localContext = new BasicHttpContext();

            //Se Specificata la username effettuo autenticazione
            if (username.length()>0)
                httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
                        new UsernamePasswordCredentials(username,password));

            HttpGet httpGet = new HttpGet(indirizzo);
            HttpResponse response = httpClient.execute(httpGet, localContext);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            response.getEntity().getContent()
                    )
            );

            String line = null;
            while ((line = reader.readLine()) != null){
                result += line + "\n";
            }
        } catch (Exception e)
        {
            Log.e("Error", "GetWebPage: "+ e.getMessage());
            result=String.format("ERROR - Unable to open requested Page %s",e.getMessage());
        }

        return result;
    }

    private class CaricaPagina extends AsyncTask<String, Void, String>
    {

        @Override
        protected String doInBackground(String... strings) {


            return null;
        }
    }
    public String PostWebPage(String indirizzo,String parametri){
    	return PostWebPage(indirizzo, parametri, "", "");
    }
    /************************************
     * PostWebPage
     * @param indirizzo
     * @param parametri
     * @param username
     * @param password
     * @return
     *************************************/
    public String PostWebPage(String indirizzo,String parametri, String username, String password)
    {
    	String result="";

    	    // Create a new HttpClient and Post Header
    		HttpParams httpParams = new BasicHttpParams();
        	DefaultHttpClient httpClient = new DefaultHttpClient(httpParams);
        	//HttpContext localContext = new BasicHttpContext();
        	
        	//Se Specificata la username effettuo autenticazione
        	if (username.length()>0)
        		httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1),
        					new UsernamePasswordCredentials(username,password));

        	HttpPost httppost = new HttpPost(indirizzo);

    	    try {
    	    	String[] aParametri=parametri.split("&");
    	        // Add your data
    	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(aParametri.length);
    	    	for (String parametro : aParametri) {
        	        String[] aFields=parametro.split("=");
        	        nameValuePairs.add(new BasicNameValuePair(aFields[0], aFields[1]));
				}    	        
    	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

    	        // Execute HTTP Post Request
    	        HttpResponse response = httpClient.execute(httppost);
    	        	
    	        //Converto la richiesta in stringa
    	        BufferedReader reader = new BufferedReader(
                	    new InputStreamReader(response.getEntity().getContent())
                	  );
                	 
                	String line = null;
                	while ((line = reader.readLine()) != null){
                	  result += line + "\n";
                	}   		
    	        
    	    } catch (ClientProtocolException e) {
    	        // TODO Auto-generated catch block
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
    	    }    	
    	return result;
    }

    
    
	/*****************************************
	 * 
	 *           Funzioni XML
	 * 
	 *****************************************/
    public String XMLGetFirstNodeValue(String strXML, String nodeName){

    	//strXML="<FMPCommand><command>PlaceCall</command><parameter name=\"called\">169</parameter><parameter name=\"myphone\">3387169051</parameter><parameter name=\"internal\">true</parameter><user>171</user><password>fabrizio171</password><PBXCode>FKVS3075385272</PBXCode></FMPCommand>";
    	DocumentBuilderFactory factory;
    	DocumentBuilder builder;
    	InputStream is;
    	Document dom;
    	String result="";
	    try {
	        factory = DocumentBuilderFactory.newInstance();
	        is = new ByteArrayInputStream(strXML.getBytes());
	        builder = factory.newDocumentBuilder();

	        dom = builder.parse(is);

	        NodeList nodeList = dom.getElementsByTagName(nodeName);


	        //for (int i = 0; i < nodeList.getLength(); i++) {
        	//Node node = nodeList.item(i);
        		Node node = nodeList.item(0);
	        	
	        	Log.d("Event", node.getNodeName());
	        	Log.d("Event",node.getTextContent());
	        	result=node.getTextContent();
	        	//break;
	     }
	     	//}
    	 catch(Exception e)
    	 {
    		 Log.e("Error", e.getMessage());
    	 }
    	 return result;
    }  
    public String XMLGetFirstNodeAttrValue(String strXML, String nodeName,String attrName,String attrValue){

    	//strXML="<FMPCommand><command>PlaceCall</command><parameter name=\"called\">169</parameter><parameter name=\"myphone\">3387169051</parameter><parameter name=\"internal\">true</parameter><user>171</user><password>fabrizio171</password><PBXCode>FKVS3075385272</PBXCode></FMPCommand>";
    	DocumentBuilderFactory factory;
    	DocumentBuilder builder;
    	InputStream is;
    	Document dom;
    	String result="";
	    try {
	        factory = DocumentBuilderFactory.newInstance();
	        is = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
	        builder = factory.newDocumentBuilder();

	        dom = builder.parse(is);

	        NodeList nodeList = dom.getElementsByTagName(nodeName);
        	Node node = nodeList.item(0);
        	
        	Log.d("Event", node.getNodeName());
        	NamedNodeMap namedNodeMap=node.getAttributes();
        	for (int j = 0; j < namedNodeMap.getLength(); j++) {
                Attr attr = (Attr)namedNodeMap.item(j);
				if(attr.getName()==attrName){
	                Log.d("Event",attr.getValue());
	                if(attr.getValue()==attrValue){
	                	Log.d("Event",node.getTextContent());
		            	result=node.getTextContent();
		            	break;
	                }
				}
			}
	     	}
    	 catch(Exception e)
    	 {
    		 Log.e("Error", e.getMessage());
    	 }
    	 return result;
    }
    public ArrayList<String> XMLGetAllNodeValue(String strXML, String nodeName){

    	//strXML="<FMPCommand><command>PlaceCall</command><parameter name=\"called\">169</parameter><parameter name=\"myphone\">3387169051</parameter><parameter name=\"internal\">true</parameter><user>171</user><password>fabrizio171</password><PBXCode>FKVS3075385272</PBXCode></FMPCommand>";
    	DocumentBuilderFactory factory;
    	DocumentBuilder builder;
    	InputStream is;
    	Document dom;
    	ArrayList<String> resultAll=new ArrayList<String>();
    	String result;
	    try {
	        factory = DocumentBuilderFactory.newInstance();
	        is = new ByteArrayInputStream(strXML.getBytes());
	        builder = factory.newDocumentBuilder();

	        dom = builder.parse(is);

	        NodeList nodeList = dom.getElementsByTagName(nodeName);


	        for (int i = 0; i < nodeList.getLength(); i++) {
	        	Node node = nodeList.item(i);
        		//Node node = nodeList.item(0);
	        	
	        	Log.d("Event", node.getNodeName());
	        	Log.d("Event",node.getTextContent());
	        	result=node.getTextContent();
	        	resultAll.add(result);
	        }
	        	//break;
	     }
	     	//}
    	 catch(Exception e)
    	 {
    		 Log.e("Error", e.getMessage());
    	 }
    	 return resultAll;
    }  
}
