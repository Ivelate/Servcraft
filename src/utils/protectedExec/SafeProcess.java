package utils.protectedExec;

public class SafeProcess 
{
	private Process proc;
	private StreamGobbler[] threads=new StreamGobbler[2];
	public SafeProcess(Process proc,boolean verbose)
	{
		this.proc=proc;
		 // any error message?
        StreamGobbler errorGobbler = new 
            StreamGobbler(proc.getErrorStream(), "ERROR",verbose);            
        
        // any output?
        StreamGobbler outputGobbler = new 
            StreamGobbler(proc.getInputStream(), "OUTPUT",verbose);
        threads[0]=errorGobbler;
        threads[1]=outputGobbler;
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
	}
	public int waitFor() throws InterruptedException
	{
		int toReturn=this.proc.waitFor();
		this.threads[0].interrupt();
		this.threads[1].interrupt();
		return toReturn;
	}
}
