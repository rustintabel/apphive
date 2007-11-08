/**
 * 
 */
package reaping;


/**
 * @author justin
 *
 */
public class ProcessorThread extends Thread {
	Processor processor;
	String userName;
	int setID=0;
	boolean mode=true;
	public static enum ProcessorThreadState 
	{
		STOPPED,
		PROCESSING,
		FINISHEDPROCESSINGUSERS,
		FINISHEDPROCESSINGUSER,
		FINISHEDPROCESSINGSETS,
		FINISHEDPROCESSINGSET,
		FINISHEDPROCESSINGRECOMMENDATIONS
		};
	public ProcessorThreadState currentState=ProcessorThreadState.STOPPED;
	Thread t;
	/**
	 * 
	 */
	public ProcessorThread(Processor processor,String userName,int setID,boolean mode) {
		this.mode=mode;
		this.processor= processor;
		this.userName=userName;
		this.setID=setID;
		t=new Thread(this,"Process Thread");
		t.start();
	}
	
	public void run() 
	{
		try{
			if(mode)
			{
				processor.process(userName);
			}
			else
			{
				processor.getBookmarksForSet(setID);
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	

	/**
	 * @param arg0
	 */
	public ProcessorThread(Runnable arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 */
	public ProcessorThread(String arg0) {
		super(arg0);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessorThread(ThreadGroup arg0, Runnable arg1) {
		super(arg0, arg1);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessorThread(ThreadGroup arg0, String arg1) {
		super(arg0, arg1);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 */
	public ProcessorThread(Runnable arg0, String arg1) {
		super(arg0, arg1);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 */
	public ProcessorThread(ThreadGroup arg0, Runnable arg1, String arg2) {
		super(arg0, arg1, arg2);
		
	}

	/**
	 * @param arg0
	 * @param arg1
	 * @param arg2
	 * @param arg3
	 */
	public ProcessorThread(ThreadGroup arg0, Runnable arg1, String arg2,
			long arg3) {
		super(arg0, arg1, arg2, arg3);
		
	}

}
