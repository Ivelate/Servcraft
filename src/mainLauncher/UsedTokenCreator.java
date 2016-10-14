package mainLauncher;

import java.io.File;
import java.io.IOException;
import java.util.Formatter;

public class UsedTokenCreator 
{
	public static final char RESERVED='0';
	public static final char PREPARED='1';
	public static final char OFFLINE='2';
	public static final String TOKEN_NAME="usedby.txt";
	public static final String LAST_MOUNTED_FILENAME="lastmounted.txt";
	
	private File workingDir;
	private String myIp;
	private File myToken;
	
	public UsedTokenCreator(File workingDir,String myIp)
	{
		this.workingDir=workingDir;
		this.myIp=myIp;
		this.myToken=new File(workingDir,TOKEN_NAME);
	}
	private Formatter setupFormatter() throws IOException
	{
		if(this.myToken.exists()){this.myToken.delete();}
		this.myToken.createNewFile();
		Formatter fo=new Formatter(this.myToken);
		return fo; 
	}
	public File createReservedToken() throws IOException
	{
		Formatter fo=setupFormatter();
		fo.format("%c %s\n",RESERVED,this.myIp);
		fo.flush();fo.close();
		return this.myToken;
	}
	public File createPreparedToken() throws IOException
	{
		Formatter fo=setupFormatter();
		fo.format("%c %s\n",PREPARED,this.myIp);
		fo.flush();fo.close();
		return this.myToken;
	}
	public File createOfflineToken() throws IOException
	{
		Formatter fo=setupFormatter();
		fo.format("%c %s\n",OFFLINE,this.myIp);
		fo.flush();fo.close();
		return this.myToken;
	}
	public File createLastUsed(String who) throws IOException
	{
		File myUsedInfo=new File(this.workingDir,LAST_MOUNTED_FILENAME);
		if(myUsedInfo.exists()){myUsedInfo.delete();}
		myUsedInfo.createNewFile();
		Formatter fo=new Formatter(myUsedInfo);
		fo.format("%s\n",this.myIp);
		fo.flush();fo.close();
		return myUsedInfo;
	}
	public boolean deleteToken()
	{
		if(!this.myToken.exists()){return this.myToken.delete();}
		return false;
	}
}
