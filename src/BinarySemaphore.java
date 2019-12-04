// Yaðmur Kahyaoðlu // 2015400057 // kahyaoglu.yagmur@gmail.com

public class BinarySemaphore { // used for mutual exclusion

	boolean value;

	public BinarySemaphore(boolean initValue) {
		value = initValue;
	}

	public synchronized void P() { // atomic operation // blocking
		while (value == false)
			try {
				this.wait(); // add process to the queue of blocked processes
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		value = false;
	}

	public synchronized void V() { // atomic operation // non-blocking
		value = true;
		notify(); // wake up a process from the queue
	}

}