package com.netease.backend.coordinator.processor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.netease.backend.coordinator.log.LogManager;
import com.netease.backend.coordinator.transaction.Action;
import com.netease.backend.coordinator.transaction.Transaction;
import com.netease.backend.tcc.CoordinatorException;
import com.netease.backend.tcc.Procedure;

public class ExpireProcessor {
	
	private BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	private ExecutorService threadPool = new ThreadPoolExecutor(0, 100,
            60L, TimeUnit.SECONDS, queue); 
	private Map<Long, Transaction> failTable = new ConcurrentHashMap<Long, Transaction>();
	private TccProcessor tccProcessor = null;
	private LogManager logManager = null;
	
	
	public void process(Transaction tx) {
		threadPool.execute(new Task(tx));
	}
	
	private class Task implements Runnable {
		
		private Transaction tx;
		
		Task(Transaction tx) {
			this.tx = tx;
		}

		@Override
		public void run() {
			List<Procedure> procList = tx.getExpireList();
			try {
				if (procList == null || !logManager.checkExpired(tx.getUUID()))
					return;
				for (Procedure proc : procList) {
					if (proc.getMethod() == null)
						proc.setMethod(ServiceTask.EXPIRED);
				}
				logManager.logBegin(tx, Action.EXPIRED, true);
				tccProcessor.perform(tx.getUUID(), procList);
				logManager.logFinish(tx, Action.EXPIRED);
			} catch (CoordinatorException e) {
				failTable.put(tx.getUUID(), tx);
			} 
		}
	}

}
