package reaping;

import java.awt.AWTEvent;

public class ProcessorMessageEvent extends AWTEvent {
	static final long serialVersionUID=1L;
	Processor processor;
	String message;
	public ProcessorMessageEvent(Processor processor,String message)
	{
		super(processor,0);
		this.processor=processor;
		this.message=message;
	}
}

