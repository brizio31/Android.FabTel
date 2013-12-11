package it.cloudhome.android.fabtel;

public class ClsContatto {

	private String nome;
	private String interno;
	private String stato;
	private String chat_address;
	private int id;
	
	public ClsContatto(String nome, String interno, String stato, String chat_address) {
		// TODO Auto-generated constructor stub
		super();
		this.nome=nome;
		this.interno=interno;
		this.stato=stato;
		this.chat_address=chat_address;
	}
	public ClsContatto(String nome, String interno, String chat_address) {
		// TODO Auto-generated constructor stub
		super();
		this.nome=nome;
		this.interno=interno;
		this.stato="";
		this.chat_address=chat_address;
	}

	public String getNome(){
		return this.nome;
	}
	
	public int getId(){return id;}
	public String getInterno(){ return this.interno;}
	public String getStato(){ return this.stato;}
	public String getChatAddress(){ return this.chat_address;}
	
	public void setId(int id){this.id=id;}
	public void setInterno(String interno){this.interno=interno;}
	public void setNome(String nome){this.nome=nome;}
	public void setChatAddress(String chat_address){this.chat_address=chat_address;}
	public void setStato(String stato){this.stato=stato;}
	
	public int getImage_old(){
        /*
		if (this.stato.equalsIgnoreCase("ONLINE"))
			return R.drawable.ico_profilo;
		if (this.stato.equalsIgnoreCase("BUSY"))
			return R.drawable.ico_profilo_busy;
		if (this.stato.equalsIgnoreCase("DND"))
			return R.drawable.ico_profilo_dnd;
		if (this.stato.startsWith("DIVERT"))
			return R.drawable.ico_profilo_out;
		return R.drawable.ico_profilo_off;
		*/
        return R.drawable.ico_profilo;
	}
	public int getImage(){
		return R.drawable.ico_profilo;
	}
	public int getPhoneImage()
	{
		//Nuova Funzione Non esegue pi� il controllo sullo stato dell'interno
		return R.drawable.call_user;
	}
	public int getPhoneImage_old(){
        /*
		if (this.stato.equalsIgnoreCase("ONLINE"))
			return R.drawable.call_user;
		if (this.stato.equalsIgnoreCase("BUSY"))
			return R.drawable.call_user_off;
			*/
		return R.drawable.call_user;
	}
}
