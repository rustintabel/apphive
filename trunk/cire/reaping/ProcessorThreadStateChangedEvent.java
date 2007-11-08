package reaping;

import java.awt.AWTEvent;

public class ProcessorThreadStateChangedEvent extends AWTEvent {

	
	static final long serialVersionUID=1L;
	Processor processor;
	ProcessorThread.ProcessorThreadState state;
	
	public ProcessorThreadStateChangedEvent(Processor processor,ProcessorThread.ProcessorThreadState state)
	{
		super(processor,0);
		this.processor=processor;
		this.state=state;
	}
}


