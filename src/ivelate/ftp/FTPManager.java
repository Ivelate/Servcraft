package ivelate.ftp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
/**
 * Handles connections between this program and a FTP server.
 * @author Ivelate
 * @since 8:56 - 13/03/2013
 *
 */
public class FTPManager 
{
	private String host;
	private String user;
	private String pass;
	private String rutaBase;
	private FTPClient myClient=new FTPClient();
	private boolean connected=false;
	public FTPManager(String host,String user,String pass,String rutaBase)
	{
		this.host=host;
		this.user=user;
		this.pass=pass;
		this.rutaBase=rutaBase.endsWith("/") ? rutaBase: rutaBase+'/';
	}
	/**
	 * Connects to server
	 */
	public boolean connect()
	{
		try 
		{
			System.out.println("Connecting "+this.host);
			this.myClient.connect(this.host);
			// *** No se para que sirve esto pero si no no chuta, ignoremoslo con felicidad y imaginación *** \\
			this.myClient.setFileType(FTPClient.BINARY_FILE_TYPE);
			this.myClient.enterLocalPassiveMode();
			this.myClient.setBufferSize(0);
			// ***                                  Fin del Deja-vu 									  *** \\
			System.out.println("Logging in");
			return this.connected=this.myClient.login(this.user, this.pass);
		} 
		catch (IOException e) 
		{
			System.out.println("Error connecting ftp server: "+e.getMessage());
		}
		return this.connected=false;
	}
	/**
	 * 
	 */
	public boolean logout()
	{
		try 
		{
			return this.connected=(!this.myClient.logout());
		} 
		catch (IOException e){}
		return false;
	}
	public boolean uploadFile(File f,String ruta)
	{
		if(!this.connected){ System.out.println("Error uploading file: Not connected to server"); return false;}
		FileInputStream fis=null;
		boolean resultado=false;
		try 
		{
			fis = new FileInputStream(f);
			//System.out.println("Uploading "+f.getName()+" ("+f.length()+" bytes)");
			resultado=this.myClient.storeFile(this.rutaBase+ruta+f.getName(), fis);
			//System.out.println(this.rutaBase+ruta+f.getName());
			fis.close();
			//if(resultado) System.out.println(f.getName()+" uploaded succesfully"); else System.out.println(this.myClient.getReplyCode());
			return resultado;
		} 
		catch (IOException e) 
		{
			System.out.println("Error uploading file: "+e.getMessage());
		} 
		finally
		{
			if(fis!=null)
			{
				try {fis.close();} 
				catch (IOException e) {	}
			}
		}
		return false;
	}
	public boolean uploadDir(File f,String ruta)
	{
		if(!this.connected){ System.out.println("Error uploading file: Not connected to server"); return false;}
		if(!f.isDirectory()){System.out.println("Error uploading file: route provided is not a valid directory"); return false;}
		if(ruta.charAt(ruta.length()-1)!='/'){System.out.println("Error uploading file: route provided is not a valid directory"); return false;}
		return uploadDirRec(f,ruta);
	}
	private boolean uploadDirRec(File f,String ruta)
	{
		File[] listFiles=f.listFiles();
		boolean exito=true;
		for(File fil:listFiles)
		{
			if(fil.isDirectory())
			{
				try
				{
					if(!this.myClient.changeWorkingDirectory(ruta+fil.getName()))
					{
						this.myClient.makeDirectory(ruta+fil.getName());
					}
					if(!uploadDirRec(fil,ruta+fil.getName())) exito=false;
				}
				catch(IOException e){exito=false;}
			}
			else
			{
				uploadFile(fil,ruta);
			}
		}
		
		return exito;
	}
	public boolean downloadFile(File f,String ruta)
	{
		if(!this.connected){ System.out.println("Error downloading file: Not connected to server"); return false;}
		if(!f.isDirectory()){System.out.println("Error downloading file: Destiny file is not a Directory");return false;}
		FileOutputStream fos=null;
		boolean resultado=false;
		try 
		{
			String fileName=getName(ruta);
			fos = new FileOutputStream(new File(f,fileName));
			//System.out.println("Downloading "+fileName);
			resultado=this.myClient.retrieveFile(this.rutaBase+ruta, fos);
			fos.close();
			//if(resultado) System.out.println(fileName+" downloaded succesfully"); else System.out.println(this.myClient.getReplyCode());
			return resultado;
		} 
		catch (IOException e) 
		{
			System.out.println("Error downloading file: "+e.getMessage());
		} 
		finally
		{
			if(fos!=null)
			{
				try {fos.close();} 
				catch (IOException e) {	}
			}
		}
		return false;
	}
	public boolean downloadDir(File f,String ruta)
	{
		if(!this.connected){ System.out.println("Error downloading file: Not connected to server"); return false;}
		if(!f.isDirectory()){System.out.println("Error downloading file: route provided is not a valid directory"); return false;}
		if(ruta.charAt(ruta.length()-1)!='/'){System.out.println("Error downloading file: route provided is not a valid directory"); return false;}
		return downloadDirRec(f,ruta);
	}
	private boolean downloadDirRec(File f,String ruta)
	{
		File[] listFiles=f.listFiles();
		boolean exito=true;
		for(File fil:listFiles)
		{
			if(fil.isDirectory())
			{
				if(!uploadDirRec(fil,ruta+fil.getName())) exito=false;
			}
			else
			{
				uploadFile(fil,ruta);
			}
		}
		
		return exito;
	}
	public boolean exists(String nombre)
	{
		if(!this.connected){ System.out.println("Error checking file: Not connected to server"); return false;}
		try 
		{
			for(FTPFile f:this.myClient.listFiles(this.rutaBase))
			{
				if(f.isFile()&&f.getName().equals(nombre))
				{
					return true;
				}
			}
			return false;
		} 
		catch (IOException e) 
		{
			return false;
		}
	}
	public boolean rename(String from,String to)
	{
		if(!this.connected){ System.out.println("Error renaming file: Not connected to server"); return false;}
		try {
			return this.myClient.rename(this.rutaBase+from,this.rutaBase+to);
		} catch (IOException e) {
			return false;
		}
	}
	public boolean delete(String name)
	{
		if(!this.connected){ System.out.println("Error deleting file: Not connected to server"); return false;}
		try
		{
			return this.myClient.deleteFile(this.rutaBase+name);
		}
		catch(IOException e){return false;}
	}
	private String getName(String ruta)
	{
		int index=-1;
		String devolver=ruta;
		for(int i=ruta.length()-1;i>=0;i--)
		{
			if(ruta.charAt(i)=='/')
			{
				index=i; break;
			}
		}
		if(index!=-1)
		{
			devolver=ruta.substring(index+1);
		}
		return devolver;
	}
}
