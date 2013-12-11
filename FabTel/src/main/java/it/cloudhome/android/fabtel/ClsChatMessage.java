package it.cloudhome.android.fabtel;

import java.sql.Timestamp;
import java.util.Date;

public class ClsChatMessage {

	private String _from,_to,_chatMessage,_timestamp;
	private int id;
	
	public ClsChatMessage(String sFrom, String sTo, String sMessage) {
		// TODO Auto-generated constructor stub
		super();
		this._from=sFrom;
		this._to=sTo;
		this._chatMessage=sMessage;
		Date now = new Date();
		Long ts =now.getTime();
		this._timestamp = ts.toString();
	}

	public ClsChatMessage(String sFrom, String sTo, String sMessage,String sTimeStamp) {
		// TODO Auto-generated constructor stub
		super();
		this._from=sFrom;
		this._to=sTo;
		this._chatMessage=sMessage;
		this._timestamp = sTimeStamp;
	}
	
	public int getId(){return id;}
	public String getFrom() {return this._from;}
	public String getTo(){ return this._to;}
	public String getMessage(){ return this._chatMessage;}
	public String getTimeStamp(){ return this._timestamp;}
	@SuppressWarnings("deprecation")
	public String getDateTime()
	{ 
		Timestamp ts=new Timestamp(Long.valueOf(this._timestamp));
		String tmpStr=String.valueOf(ts.getDate())+"/"+String.valueOf(ts.getMonth()+1)+"/"+
						String.valueOf(1900+ts.getYear())+" "+String.valueOf(ts.getHours())+":";
		String min="0"+String.valueOf(ts.getMinutes()).trim();
		min=min.substring(min.length()-2, min.length());
		tmpStr += min;
		min="0"+String.valueOf(ts.getSeconds()).trim();
		min=min.substring(min.length()-2, min.length());
		tmpStr += ":"+ min;
		return tmpStr;
	}
	
	public void setId(int id){this.id=id;}
	public void setFrom(String sFrom){this._from=sFrom;}
	public void setTo(String sTo){this._to=sTo;}
	public void setMessage(String message){this._chatMessage=message;}
	public void setTimeStamp(String ts){this._timestamp=ts;}
	
}
