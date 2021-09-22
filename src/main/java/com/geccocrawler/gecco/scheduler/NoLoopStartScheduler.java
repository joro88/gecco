package com.geccocrawler.gecco.scheduler;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.geccocrawler.gecco.request.HttpRequest;

/**
 * 不需要循环抓取的start队列
 * 
 * @author huchengyi
 *
 */
public class NoLoopStartScheduler implements Scheduler {
	
	protected ConcurrentLinkedQueue<HttpRequest> queue;
	
	public NoLoopStartScheduler() {
		queue = new ConcurrentLinkedQueue<HttpRequest>();
	}

	@Override
	public HttpRequest out() {
		HttpRequest request = queue.poll();
		return request;
	}

	@Override
	public boolean into(HttpRequest request) {
		queue.offer(request);
        return true;
	}

}
