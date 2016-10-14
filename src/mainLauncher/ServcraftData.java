package mainLauncher;

public class ServcraftData 
{
	public static final byte LAUNCHER_ROUTE_POS=		0;
	public static final byte HAMACHI_IP_POS=			1;
	public static final byte FTP_HOST_POS=				2;
	public static final byte FTP_CLIENT_POS=			3;
	public static final byte FTP_PASS_POS=				4;
	public static final byte FTP_ROUTE_POS=				5;
	public static final byte LOCAL_SERVER_ROUTE_POS=	6;
	
	public static final int MAX_SIZE=6;
	
	private String[] dataTable=new String[MAX_SIZE];
	private int size=0;
	public ServcraftData()
	{
		
	}
	public void add(String s) throws FullListException
	{
		if(this.size>=this.dataTable.length) throw new FullListException();
		this.dataTable[this.size]=s;
		this.size++;
	}
	public String getLauncherRoute()
	{
		return this.dataTable[LAUNCHER_ROUTE_POS];
	}
	public void setLauncherRoute(String s)
	{
		this.dataTable[LAUNCHER_ROUTE_POS]=s;
	}
	public String getHamachiIP()
	{
		return this.dataTable[HAMACHI_IP_POS];
	}
	public void setHamachiIP(String s)
	{
		this.dataTable[HAMACHI_IP_POS]=s;
	}
	public String getFTPHost()
	{
		return this.dataTable[FTP_HOST_POS];
	}
	public void setFTPHost(String s)
	{
		this.dataTable[FTP_HOST_POS]=s;
	}
	public String getFTPClient()
	{
		return this.dataTable[FTP_CLIENT_POS];
	}
	public void setFTPClient(String s)
	{
		this.dataTable[FTP_CLIENT_POS]=s;
	}
	public String getFTPPass()
	{
		return this.dataTable[FTP_PASS_POS];
	}
	public void setFTPPass(String s)
	{
		this.dataTable[FTP_PASS_POS]=s;
	}
	public String getFTPRoute()
	{
		return this.dataTable[FTP_ROUTE_POS];
	}
	public void setFTPRoute(String s)
	{
		this.dataTable[FTP_ROUTE_POS]=s;
	}
	public String getLocalServerRoute()
	{
		return this.dataTable[LOCAL_SERVER_ROUTE_POS];
	}
	public void setLocalServerRoute(String s)
	{
		this.dataTable[LOCAL_SERVER_ROUTE_POS]=s;
	}
	public void printAllData()
	{
		for(int i=0;i<this.dataTable.length;i++)
		{
			System.out.println(this.dataTable[i]);
		}
		
	}
	
}
