/**
 * 
 */
package reaping;

import java.util.EventListener;

/**
 * @author justin
 *
 */
public interface ProcessorMessageListener extends EventListener {
	public abstract void messageSent(ProcessorMessageEvent e);
}

