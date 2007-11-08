package reaping;

import java.util.EventListener;

public interface ProcessorThreadStateChangedListener extends EventListener {
	public abstract void processorThreadstateChanged(ProcessorThreadStateChangedEvent e);
}
