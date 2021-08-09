package com.geccocrawler.gecco.monitor;

import com.geccocrawler.gecco.GeccoFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DownloadMonitor {
	
	private static Log log = LogFactory.getLog(DownloadMonitor.class);
	
	protected static Map<String, DownloadStatistics> statistics = new ConcurrentHashMap<String, DownloadStatistics>();
	
	//不公平重入锁,用来控制host的创建
	private static Lock lock = new ReentrantLock();
	
	public static Set<String> getHosts() {
		return statistics.keySet();
	}
	
	public static Map<String, DownloadStatistics> getStatistics() {
		return statistics;
	}
	
	/**
	 * 双重检查机制锁
	 * 
	 * @param host host
	 * @return DownloadStatistics
	 */
	public static DownloadStatistics getStatistics(String host, GeccoFactory factory) {
		DownloadStatistics downloadStatistics = statistics.get(host);
		if(downloadStatistics != null) {
			return downloadStatistics;
		}
		lock.lock();
		try{
			downloadStatistics = statistics.get(host);
			if(downloadStatistics == null) {
				downloadStatistics = factory.createDownloadStatistics(host, null); // TODO
				statistics.put(host, downloadStatistics);
			}
		} finally {
			lock.unlock();
		}
		return downloadStatistics;
	}
	
	protected static String getHost(String url) {
		try {
			URL requestUrl = new URL(url);
			String host = requestUrl.getHost();
			return host;
		} catch (MalformedURLException e) {
			log.error(e);
			return url;
		}
	}
	
	public static void incrSuccess(String url, GeccoFactory factory) {
		getStatistics(getHost(url), factory).incrSuccess();
	}
	
	public static void incrServerError(String url, GeccoFactory factory) {
		getStatistics(getHost(url), factory).incrServerError();
	}
	
	public static void incrException(String url, GeccoFactory factory) {
		getStatistics(getHost(url), factory).incrException();
	}
}
