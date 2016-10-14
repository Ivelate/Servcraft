package utils.protectedExec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StreamGobbler extends Thread
{
    private InputStream is;
    private String type;
    private boolean verbose;
    StreamGobbler(InputStream is, String type,boolean verbose)
    {
        this.is = is;
        this.type = type;
        this.verbose=verbose;
    }
    
    public void run()
    {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line=null;
            while ( (line = br.readLine()) != null)
                if(verbose){System.out.println(type + ">" + line);}    
            } catch (IOException ioe)
              {
                ioe.printStackTrace();  
              }
    }
}