package reaping;

public class ProcessorMessageListenerAdapter implements
		ProcessorMessageListener {
	public void messageSent(ProcessorMessageEvent e) 
	{
		System.out.println(e.message);
		
	}
}
