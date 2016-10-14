package mainLauncher;

import ivelate.ftp.FTPManager;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.security.GeneralSecurityException;
import java.util.Formatter;
import java.util.NoSuchElementException;
import java.util.Scanner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

import utils.cript.EncriptationManager;
import utils.protectedExec.SafeProcess;
import utils.zip.CompressionManager;

public class Servcraft 
{	
	private static final String VERSION="1.0.5";
	private static final int TERRARIA_STEAM_ID=105600;
	public static final int RETRYES_UNTIL_DISCONNECT=4;
	private static final String SERVER_ZIP_NAME="world.zip";
	private static final String SERVER_MAP_NAME="servcraft";
	private static final String LAST_VERSION_FILENAME="lastver.txt";
	private static String SYSTEM_SEPARATOR;
	private static String LINE_SEPARATOR;
	public Servcraft(String[] args) throws Exception
	{
		if(args.length>0)
		{
			System.out.println("Servcraft updated to version "+VERSION);
			System.out.println();
		}
		System.out.println("Welcome to Servcraft");
		System.out.println();
		System.out.println("Checking if Servcraft is installed...");
		JFileChooser fr = new JFileChooser();
	    FileSystemView fw = fr.getFileSystemView();
	    File documents=fw.getDefaultDirectory();
		System.out.println("My documents route located: "+documents.getAbsolutePath());
		SYSTEM_SEPARATOR=FileSystems.getDefault().getSeparator();
		LINE_SEPARATOR=System.getProperty("line.separator");
		File terrariaFolder=new File(documents.getAbsolutePath()+SYSTEM_SEPARATOR+"My Games"+SYSTEM_SEPARATOR+"Terraria");
		if(!terrariaFolder.exists()||!terrariaFolder.isDirectory())
		{
			System.out.println("Fatal error: Terraria is not installed in the computer");
			enterAndHalt(1);
		}
		System.out.println("Terraria folder located: "+terrariaFolder.getAbsolutePath());
		File properties=new File(terrariaFolder.getAbsolutePath()+SYSTEM_SEPARATOR+"Servcraft.INI");
		if(!properties.exists())
		{
			System.out.println("Servcraft is not installed. Installing...");
			System.out.println();
			install(properties);
		}
		else
		{
			System.out.println("Servcraft file found! Starting program...");
			ServcraftData sd=readIni(properties);
			if(sd!=null)
			{
				init(sd,properties);
			}
			else
			{
				properties.delete();
				install(properties);
			}
		}
	}
	private static void init(ServcraftData sd,File properties) 
	{
		System.out.println();
		System.out.println("SERVCRAFT v"+VERSION);
		System.out.println("Select action: 1- Look for server");
		System.out.println("               2- Check if server is online");
		System.out.println("               3- Launch game");
		System.out.println("               4- Reinstall Servcraft");
		System.out.println("               5- Check for updates");
		System.out.println("               6- Close program");
		int resp=askNum(1,6);
		switch(resp)
		{
		case 1:
			serverInit(sd,properties);
			break;
		case 2:
			serverCheck(sd,properties);
			break;
		case 3:
			gameInit(sd,false);
			break;
		case 4:
			reinstall(properties,sd);
			break;
		case 5:
			checkForUpdates(sd,properties);
			break;
		case 6:
			System.exit(0);
		}
		System.out.println();
		System.out.println("All done. Closing program...");
		System.exit(0);
	}
	private static void serverInit(ServcraftData sd,File prop)
	{
		System.out.println();
		File workDir=prop.getParentFile();
		System.out.println("Connecting to FTP server...");
		FTPManager connectionManager= new FTPManager(sd.getFTPHost(),sd.getFTPClient(),sd.getFTPPass(),"public_html/"+sd.getFTPRoute());
		connectFTPManager(connectionManager);
		System.out.println("Connected succesfully to "+sd.getFTPHost()+" as user "+sd.getFTPClient());
		UsedTokenCreator tokenFactory=new UsedTokenCreator(workDir,sd.getHamachiIP());
		File versionFile;
		connectionManager.downloadFile(workDir, LAST_VERSION_FILENAME);
		try {
			Scanner versionScanner=new Scanner(versionFile=new File(workDir,LAST_VERSION_FILENAME));
			if(!isSupported(versionScanner.next()))
			{
				versionFile.delete();
				versionScanner.close();
				System.out.println("Your client version is deprecated! Please upgrade it to continue (Option 5 in menu)");
				enterAndHalt(0);
			}
			versionFile.delete();
			versionScanner.close();
			
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(connectionManager.exists(UsedTokenCreator.TOKEN_NAME))
		{
			connectionManager.downloadFile(workDir, UsedTokenCreator.TOKEN_NAME);
			try {
				Scanner s=new Scanner(new File(workDir,UsedTokenCreator.TOKEN_NAME));
				switch(s.next())
				{
				case UsedTokenCreator.OFFLINE+"":
					if(s.next().equals(sd.getHamachiIP()))
					{
						System.out.println("Server prepared. Setting online state to PREPARED");
						connectionManager.uploadFile(tokenFactory.createPreparedToken(),"");
						tokenFactory.deleteToken();
						System.out.println("You are (already) the owner of the server. Starting game...");
						connectionManager.logout();
						gameInit(sd,true);
						uploadServer(connectionManager,tokenFactory,workDir);
					}
					else
					{
						System.out.println("Server is currently being uploaded and offline. Try again in a couple of minutes");
						enterAndHalt(0);
					}
					break;
				case UsedTokenCreator.PREPARED+"":
					String ip;
					if((ip=s.next()).equals(sd.getHamachiIP()))
					{
						System.out.println("You are (already) the owner of the server. Starting game...");
						connectionManager.logout();
						gameInit(sd,true);
						uploadServer(connectionManager,tokenFactory,workDir);
					}
					else
					{
						System.out.println("Server online! Connect to "+ip);
						System.out.printf("Starting game. ");
						gameInit(sd,false);
						enterAndHalt(0);
					}
					break;
				case UsedTokenCreator.RESERVED+"":
					if(s.next().equals(sd.getHamachiIP()))
					{
						connectionManager.delete(UsedTokenCreator.TOKEN_NAME);
						connectionManager.logout();
						serverInit(sd,prop);
						return;
					}
					else
					{
						System.out.println("Server is currently being mounted. Try again in a couple of seconds, or a minute!");
						enterAndHalt(0);
					}
					break;
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			(new File(workDir,UsedTokenCreator.TOKEN_NAME)).delete();
		}
		else
		{
			try
			{
				connectionManager.downloadFile(workDir,UsedTokenCreator.LAST_MOUNTED_FILENAME);
				File lmf;
				Scanner s=new Scanner(lmf=new File(workDir,UsedTokenCreator.LAST_MOUNTED_FILENAME));
				if(s.next().equals(sd.getHamachiIP()))
				{
					System.out.println("You are the last one who mounted the server. Mounting without download...");
				}
				else
				{
					System.out.println("Server doesn't exists. Creating one.");
					System.out.println("Setting online state to RESERVED");
					connectionManager.uploadFile(tokenFactory.createReservedToken(),"");
					tokenFactory.deleteToken();
					//Reserved space, now download server
					System.out.println("Downloading server... (This may take some time!)");
					connectionManager.downloadFile(workDir, SERVER_ZIP_NAME);
					System.out.println("Done. Unzipping server...");
					File zippedserver=new File(workDir,SERVER_ZIP_NAME);
					CompressionManager.unzip(zippedserver, new File(workDir,"Worlds"));
					zippedserver.delete();
					File lm;
					connectionManager.uploadFile(lm=tokenFactory.createLastUsed(sd.getHamachiIP()),"");
					lm.delete();
				}
				lmf.delete();
				System.out.println("Server prepared. Setting online state to PREPARED");
				connectionManager.uploadFile(tokenFactory.createPreparedToken(),"");
				tokenFactory.deleteToken();
				System.out.println("Done. Opening game. Start server in map "+SERVER_MAP_NAME);
				connectionManager.logout();
				gameInit(sd,true);
				uploadServer(connectionManager,tokenFactory,workDir);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.out.println("Error executing server. Contact system admin.");
			}
			finally
			{
				connectionManager.downloadFile(workDir, UsedTokenCreator.TOKEN_NAME);
				try {
					Scanner s=new Scanner(new File(workDir,UsedTokenCreator.TOKEN_NAME));
					s.next();
					if(s.next().equals(sd.getHamachiIP()))
					{
						System.out.println("Deleting reserved token...");
						connectionManager.delete(UsedTokenCreator.TOKEN_NAME);
					}
				}catch (FileNotFoundException e) {
				}catch (NoSuchElementException e){}
				tokenFactory.deleteToken();
			}
		}
	}
	private static void uploadServer(FTPManager connectionManager,UsedTokenCreator tokenFactory,File workDir) throws IOException
	{
		System.out.println("Game closed. Setting online state to OFFLINE");
		connectFTPManager(connectionManager);
		connectionManager.uploadFile(tokenFactory.createOfflineToken(),"");
		tokenFactory.deleteToken();
		System.out.println("Done. Zipping server...");
		File tmp=new File(workDir,"tmp"); tmp.mkdir();
		File toZip=new File(workDir,"Worlds"+SYSTEM_SEPARATOR+SERVER_MAP_NAME+".wld");
		copyFileUsingChannel(toZip,new File(tmp,SERVER_MAP_NAME+".wld"));
		File zipped;
		CompressionManager.zip(tmp,zipped=new File(workDir,SERVER_ZIP_NAME));
		tmp.delete();
		System.out.println("Uploading server... (This may take some time!)");
		if(connectionManager.exists("old"+SERVER_ZIP_NAME))
		{
			connectionManager.delete("old"+SERVER_ZIP_NAME);
		}
		connectionManager.rename(SERVER_ZIP_NAME, "old"+SERVER_ZIP_NAME);
		connectionManager.uploadFile(zipped, "");
		zipped.delete();
		System.out.println("Done! Setting online state to FREE");
		connectionManager.delete(UsedTokenCreator.TOKEN_NAME);
		System.out.println();
		System.out.println("Server uploaded. Finishing...");
	}
	private static void serverCheck(ServcraftData sd,File prop)
	{
			System.out.println();
			File workDir=prop.getParentFile();
			System.out.println("Connecting to FTP server...");
			FTPManager connectionManager= new FTPManager(sd.getFTPHost(),sd.getFTPClient(),sd.getFTPPass(),"public_html/"+sd.getFTPRoute());
			connectFTPManager(connectionManager);
			System.out.println("Connected succesfully to "+sd.getFTPHost()+" as user "+sd.getFTPClient());
			File versionFile;
			connectionManager.downloadFile(workDir, LAST_VERSION_FILENAME);
			try {
				Scanner versionScanner=new Scanner(versionFile=new File(workDir,LAST_VERSION_FILENAME));
				if(!isSupported(versionScanner.next()))
				{
					versionFile.delete();
					versionScanner.close();
					System.out.println("Your client version is deprecated! Please upgrade it to continue (Option 4 in menu)");
					enterAndHalt(0);
				}
				versionFile.delete();
				versionScanner.close();
				
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(connectionManager.exists(UsedTokenCreator.TOKEN_NAME))
			{
				connectionManager.downloadFile(workDir, UsedTokenCreator.TOKEN_NAME);
				try {
					Scanner s=new Scanner(new File(workDir,UsedTokenCreator.TOKEN_NAME));
					switch(s.next())
					{
					case UsedTokenCreator.OFFLINE+"":
						System.out.println("Server is currently being uploaded and offline. (IP "+s.next()+")");
						enterAndHalt(0);
						break;
					case UsedTokenCreator.PREPARED+"":
						System.out.println("Server online! (IP "+s.next()+")");
						System.out.println("Do you want to open the game and play?");
						if(askYN())
						{
							System.out.printf("Starting game. ");
							gameInit(sd,false);
							enterAndHalt(0);
						}
						//If not, just exit
						System.exit(0);
						break;
					case UsedTokenCreator.RESERVED+"":
						System.out.println("Server is currently being mounted. (IP "+s.next()+")");
						enterAndHalt(0);
						break;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				(new File(workDir,UsedTokenCreator.TOKEN_NAME)).delete();
			}
			else
			{
				System.out.println("No server mounted!");
				enterAndHalt(0);
			}
	}
	private static void enterAndHalt(int errorCode) 
	{
		System.out.println("Press enter to exit program");
		Scanner s=new Scanner(System.in);
		s.nextLine();
		System.exit(errorCode);
	}
	private static boolean isSupported(String supportedVer) 
	{
		String[] sVer=supportedVer.split("\\.");
		String[] cVer=VERSION.split("\\.");
		for(int i=0;i<sVer.length;i++)
		{
			if(cVer.length<=i){return false;}
			int svp=Integer.parseInt(sVer[i]);
			int cvp=Integer.parseInt(cVer[i]);
			if(svp>cvp){return false;}
			else if(svp<cvp){return true;}
		}
		return true;
	}
	private static void gameInit(ServcraftData sd,boolean wait)
	{
		try 
		{
			if(sd.getLauncherRoute().equals("STEAM"))
			{
				Desktop.getDesktop().browse(new URI("steam://run/"+TERRARIA_STEAM_ID));
				if(wait)
				{
					System.out.println("Manual sync required! When game closes, press ENTER");
					Scanner s=new Scanner(System.in);
					s.nextLine();
				}
			}
			else
			{
				SafeProcess p=new SafeProcess(Runtime.getRuntime().exec(sd.getLauncherRoute(),null,(new File(sd.getLauncherRoute())).getParentFile()),false);
				if(wait)
				{
					p.waitFor();
				}
			}
		} catch (IOException e) 
		{
			System.out.println("Error executing launcher. Reinstalling Servcraft if this error happens again is recommended");
			System.out.println("Servcraft will now close");
			enterAndHalt(1);
		} 
		catch (URISyntaxException e)
		{
			System.out.println("Error with the Steam execution. Please login with a account wich had purchased Terraria");
			enterAndHalt(1);
		} 
		catch (InterruptedException e) 
		{
			System.out.println("Game stopped unexpectedly");
			enterAndHalt(1);
		}
	}
	private static void checkForUpdates(ServcraftData sd,File prop)
	{
		File appFolder = new File("");
		System.out.println(appFolder.getAbsolutePath());
		System.out.println("Connecting to FTP server...");
		FTPManager connectionManager= new FTPManager(sd.getFTPHost(),sd.getFTPClient(),sd.getFTPPass(),"public_html/"+sd.getFTPRoute());
		connectFTPManager(connectionManager);
		System.out.println("Connected succesfully to "+sd.getFTPHost()+" as user "+sd.getFTPClient());
		System.out.println("Checking for updates...");
		connectionManager.downloadFile(prop.getParentFile(),"version.txt");
		try 
		{
			File f;
			Scanner s=new Scanner((f=new File(prop.getParentFile(),"version.txt")));
			String ver=s.next();
			f.delete();
			if(!ver.equals(VERSION))
			{
				System.out.println("New version found! "+ver);
				System.out.println("Download new version?");
				connectionManager.logout();
				if(askYN())
				{
					connectFTPManager(connectionManager);
					File localFolder=new File(appFolder.getAbsolutePath());
					connectionManager.downloadFile(localFolder,"servcraftupdate.exe");
					File tmpbat;
					Formatter fo=new Formatter(tmpbat=new File(appFolder.getAbsolutePath(),"tmp.bat"));
					fo.format("%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n%s\n"
							,"@echo off"
							,"goto :foo2"
							,":foo"
							,"cls"
							,"echo Final update stage!"
							,"echo Waiting to the main program to end to continue"
							,"ping 192.0.2.2 -n 1 -w 1000 > nul"
							,":foo2"
							,"del servcraft.exe"
							,"if exist servcraft.exe goto :foo"
							,"rename servcraftupdate.exe servcraft.exe"
							,"start \"\" servcraft.exe -u"
							,"del tmp.bat"
							,"exit"
							);
					fo.close();
					try 
					{
						new SafeProcess(Runtime.getRuntime().exec(tmpbat.getAbsolutePath(),null,new File(appFolder.getAbsolutePath())),false);
						System.exit(0);
					} 
					catch (IOException e) {e.printStackTrace();}
				}
				else
				{
					init(sd,prop);
				}
			}
			else
			{
				System.out.println("You are currently running the last client version");
				System.out.println();
				init(sd,prop);
			}
		} catch (FileNotFoundException e) {}
	}
	private static void reinstall(File f,ServcraftData sd)
	{
			Scanner entrada=new Scanner(System.in);
			String linea="";
			System.out.println("Reinstalling. Press ENTER to conserve the actual data");
			System.out.println("Insert your launcher .exe route. If you have Terraria on Steam, write STEAM: (Current: "+sd.getLauncherRoute()+")");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setLauncherRoute(linea);
			System.out.println("Insert your hamachi IP: (Current: "+sd.getHamachiIP()+")");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setHamachiIP(linea);
			System.out.println("Insert the FTP host name: (Current: "+sd.getFTPHost()+")");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setFTPHost(linea);
			System.out.println("Insert the FTP client name you are going to use: (Current: "+sd.getFTPClient()+")");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setFTPClient(linea);
			System.out.println("Insert the FTP client pass: ");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setFTPPass(linea);
			System.out.println("Insert the FTP servcraft dedicated route: (Current: "+sd.getFTPRoute()+")");
			if(!(linea=entrada.nextLine()).isEmpty()) sd.setFTPRoute(linea);
			if(!(sd.getLauncherRoute().endsWith(".exe")||sd.getLauncherRoute().equals("STEAM"))){System.out.println("Error installing: Invalid minecraft or no-ip executables"); enterAndHalt(0);}
			try
			{
				f.createNewFile();
				Formatter writer=new Formatter(f);
				writer.format("# Servcraft properties #%s",LINE_SEPARATOR);
				writer.format("LAUNCHER_ROUTE=%s%s", sd.getLauncherRoute(),LINE_SEPARATOR);
				writer.format("HAMACHI_IP=%s%s", sd.getHamachiIP(),LINE_SEPARATOR);
				writer.format(LINE_SEPARATOR);
				writer.format("# FTP properties       #%s",LINE_SEPARATOR);
				writer.format("FTP_HOSTNAME=%s%s", sd.getFTPHost(),LINE_SEPARATOR);
				writer.format("FTP_CLIENT=%s%s", sd.getFTPClient(),LINE_SEPARATOR);
				writer.format("#Note: Client pass is encrypted%s",LINE_SEPARATOR);
				writer.format("FTP_CLIENT_PASS=%s%s", EncriptationManager.encrypt(sd.getFTPPass(),EncriptationManager.DEFAULT_KEY),LINE_SEPARATOR);
				writer.format("SERVERFILE_FTP_PATH=%s%s", sd.getFTPRoute(),LINE_SEPARATOR);
				writer.close();
				System.out.println("Servcraft installed succesfully!");
				init(sd,f);
			}
			catch(IOException e)
			{
				System.out.println("FATAL ERROR: Can`t install servcraft: "+e.getMessage());
				enterAndHalt(1);
			} catch (GeneralSecurityException e) {
				System.out.println("FATAL ERROR: Can`t install servcraft (Error encripting FTP pass): "+e.getMessage());
				enterAndHalt(1);
			}
			
	}
	private static void install(File f)
	{
		ServcraftData respuestas=new ServcraftData();
		try{
		Scanner entrada=new Scanner(System.in);
		System.out.println("Insert your terraria launcher .exe route (Complete). If you have Terraria on Steam, write STEAM: ");
		respuestas.add(entrada.nextLine());
		System.out.println("Insert your hamachi IP: ");
		respuestas.add(entrada.nextLine());
		System.out.println("Insert the FTP host name: ");
		respuestas.add(entrada.nextLine());
		System.out.println("Insert the FTP client name you are going to use: ");
		respuestas.add(entrada.nextLine());
		System.out.println("Insert the FTP client pass: ");
		respuestas.add(entrada.nextLine());
		System.out.println("Insert the FTP servcraft dedicated route: ");
		respuestas.add(entrada.nextLine());
		}
		catch(FullListException e){System.out.println("FATAL ERROR: CODE CORRUPTED"); enterAndHalt(1);} 
		if(!(respuestas.getLauncherRoute().endsWith(".exe")||respuestas.getLauncherRoute().equals("STEAM"))){System.out.println("Error installing: Invalid minecraft or no-ip executables"); enterAndHalt(0);}
		try
		{
			f.createNewFile();
			Formatter writer=new Formatter(f);
			writer.format("# Servcraft properties #%s",LINE_SEPARATOR);
			writer.format("LAUNCHER_ROUTE=%s%s", respuestas.getLauncherRoute(),LINE_SEPARATOR);
			writer.format("HAMACHI_IP=%s%s", respuestas.getHamachiIP(),LINE_SEPARATOR);
			writer.format(LINE_SEPARATOR);
			writer.format("# FTP properties       #%s",LINE_SEPARATOR);
			writer.format("FTP_HOSTNAME=%s%s", respuestas.getFTPHost(),LINE_SEPARATOR);
			writer.format("FTP_CLIENT=%s%s", respuestas.getFTPClient(),LINE_SEPARATOR);
			writer.format("#Note: Client pass is encrypted%s",LINE_SEPARATOR);
			writer.format("FTP_CLIENT_PASS=%s%s", EncriptationManager.encrypt(respuestas.getFTPPass(),EncriptationManager.DEFAULT_KEY),LINE_SEPARATOR);
			writer.format("SERVERFILE_FTP_PATH=%s%s", respuestas.getFTPRoute(),LINE_SEPARATOR);
			writer.close();
			System.out.println("Servcraft installed succesfully!");
			init(respuestas,f);
		}
		catch(IOException e)
		{
			System.out.println("FATAL ERROR: Can`t install servcraft: "+e.getMessage());
			enterAndHalt(1);
		}
		catch (GeneralSecurityException e) {
			System.out.println("Error installing: Can't encrypt given ftp pass");
			enterAndHalt(1);
		}
		
	}
	/**
	 * String[0]=Ruta Launcher
	 * String[1]=Ruta No-ip
	 * String[2]=FTP host
	 * String[3]=FTP client 
	 * String[4]=FTP pass
	 * String[5]=Ruta servidor (FTP)
	 * String[6]=Ruta servidor (Local)
	 */
	private static ServcraftData readIni(File f)
	{
		ServcraftData sd=new ServcraftData();
		try
		{
			String linea="";
			Scanner s=new Scanner(f);
			while(s.hasNextLine())
			{
				linea=s.nextLine();
				if(!(linea.startsWith("#")||linea.isEmpty()))
				{
					linea=linea.substring(linea.indexOf("=")+1).trim();
					sd.add(linea);
				}
			}
			sd.setFTPPass(EncriptationManager.decrypt(sd.getFTPPass(), EncriptationManager.DEFAULT_KEY));
		}
		catch(FileNotFoundException e)
		{
			System.out.println("Error: Can't read file");
			enterAndHalt(1);
		}
		catch(FullListException e)
		{
			System.out.println("Error with INI servcraft file. Reinstall needed");
			return null;
		} catch (GeneralSecurityException e) {
			System.out.println("Error: Can't decrypt FTP pass. Reinstall needed");
			return null;
		}
		return sd;
	}
	private static int askNum(int min,int max)
	{
		Scanner s=new Scanner(System.in);
		while(true)
		{
			System.out.println("Insert a value between "+min+" and "+max);
			String resp=s.next()+s.nextLine();
			try
			{
				int res=Integer.parseInt(resp);
				if(res<min||res>max){throw new java.lang.NumberFormatException();}
				return res;
			}
			catch(java.lang.NumberFormatException e){}
		}
	}
	private static void copyFileUsingChannel(File source, File dest) throws IOException {
	    FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    try {
	        sourceChannel = new FileInputStream(source).getChannel();
	        destChannel = new FileOutputStream(dest).getChannel();
	        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	       }finally{
	           sourceChannel.close();
	           destChannel.close();
	       }
	}
	private static void connectFTPManager(FTPManager connectionManager)
	{
		int retryes=0;
		while(!connectionManager.connect())
		{
			System.out.println("Can't log in!");
			retryes++;
			if(retryes>=RETRYES_UNTIL_DISCONNECT)
			{
				System.out.println("Impossible to log in. Try again later or check if the values in the installation are correct");
				enterAndHalt(1);
			}
			else
			{
				System.out.println("Retrying in "+retryes+" sec");
				try 
				{
					Thread.sleep(retryes*1000);
				} catch (InterruptedException e) {}
			}
		}
	}
	private static boolean askYN()
	{
		System.out.println("Insert 'y' or 'n' to continue");
		Scanner s=new Scanner(System.in);
		while(true)
		{
			String ans=s.next();
			if(ans.equals("y")){return true;}
			else if(ans.equals("n")){return false;}
			else
			{
				System.out.println("Please enter a valid answer (y,n)");
				s.nextLine();
			}
		}
	}
}
