package com.geccocrawler.gecco;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.geccocrawler.gecco.downloader.proxy.FileProxys;
import com.geccocrawler.gecco.downloader.proxy.Proxys;
import com.geccocrawler.gecco.dynamic.DynamicGecco;
import com.geccocrawler.gecco.dynamic.GeccoClassLoader;
import com.geccocrawler.gecco.listener.EventListener;
import com.geccocrawler.gecco.monitor.GeccoJmx;
import com.geccocrawler.gecco.monitor.GeccoMonitor;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import com.geccocrawler.gecco.request.HttpGetRequest;
import com.geccocrawler.gecco.request.HttpPostRequest;
import com.geccocrawler.gecco.request.HttpRequest;
import com.geccocrawler.gecco.request.StartRequestList;
import com.geccocrawler.gecco.scheduler.NoLoopStartScheduler;
import com.geccocrawler.gecco.scheduler.Scheduler;
import com.geccocrawler.gecco.scheduler.StartScheduler;
import com.geccocrawler.gecco.spider.Spider;
import com.geccocrawler.gecco.spider.SpiderBeanFactory;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.util.LinkedList;

/**
 * 爬虫引擎，每个爬虫引擎最好独立进程，在分布式爬虫场景下，可以单独分配一台爬虫服务器。引擎包括Scheduler、Downloader、Spider、 SpiderBeanFactory4个主要模块
 * 
 * @author huchengyi
 *
 */
public class GeccoEngine<V> extends Thread implements Callable<V> {

	private static Log log = LogFactory.getLog(GeccoEngine.class);

	protected Date startTime;

	protected List<HttpRequest> startRequests = new ArrayList<HttpRequest>();

	protected Scheduler scheduler;

	protected PipelineFactory pipelineFactory;

	protected List<Spider> spiders;

	protected List<String> classpaths = new LinkedList<>();

	protected int threadCount;

	protected CountDownLatch cdl;

	protected int interval;

	protected Proxys proxysLoader;
	
	protected boolean proxy = true;

	protected boolean loop;

	protected boolean mobile;

	protected boolean debug;
	
	protected boolean monitor = true;

	protected int retry;

	protected String jmxPrefix;
    
    protected GeccoFactory factory;
    
    protected GeccoContext context;

	protected V ret;//callable 返回值

	public V getRet() {
		return ret;
	}

	public void setRet(V ret) {
		this.ret = ret;
	}

	public GeccoEngine(GeccoFactory factory) {
		this.retry = 3;
        
        // allow late factory registration
        if (factory != null) {
            registerFactory(factory);
        }
	}
    
    public void registerFactory(GeccoFactory factory){
        this.factory = factory;
        factory.setEngine(this);

        context = factory.createContext();
    }

	/**
	 * 动态配置规则不能使用该方法构造GeccoEngine
	 * 
	 * @return GeccoEngine
	 */
    @Deprecated
	public static GeccoEngine createDeprecated() {
		GeccoEngine geccoEngine = new GeccoEngine(new GeccoFactory());
		geccoEngine.setName("GeccoEngine");
		return geccoEngine;
	}

	public static GeccoEngine create(String classpath) {
		return create(classpath, null);
	}

    /**
     * Use constructor directly and create SpiderBeanFactory with GeccoFactory::createSpiderBeanFactory
     * @param classpath
     * @param pipelineFactory
     * @return
     * @deprecated
     */
    @Deprecated
	public static GeccoEngine create(String classpath, PipelineFactory pipelineFactory) {
		if (StringUtils.isEmpty(classpath)) {
			// classpath不为空
			throw new IllegalArgumentException("classpath cannot be empty");
		}
//        List<String> classpaths = new LinkedList<>();
//        classpaths.add(classpath);
		GeccoEngine ge = createDeprecated();
        ge.classpath(classpath);
		return ge;
	}
    
	@Deprecated
	public GeccoEngine start(String url) {
        HttpGetRequest httpGetRequest = factory.createHttpGetRequest(url);
		return seed(httpGetRequest);
	}

	@Deprecated
	public GeccoEngine start(String... urls) {
		for (String url : urls) {
			seed(url);
		}
		return this;
	}

	@Deprecated
	public GeccoEngine start(HttpRequest request) {
		this.startRequests.add(request);
		return this;
	}

	@Deprecated
	public GeccoEngine start(List<HttpRequest> requests) {
		for (HttpRequest request : requests) {
			seed(request);
		}
		return this;
	}

	public GeccoEngine seed(String url) {
        HttpGetRequest httpGetRequest = factory.createHttpGetRequest(url);
		return seed(httpGetRequest);
	}

	public GeccoEngine seed(String... urls) {
		for (String url : urls) {
			seed(url);
		}
		return this;
	}

	public GeccoEngine seed(HttpRequest request) {
		this.startRequests.add(request);
		return this;
	}

	public GeccoEngine seed(List<HttpRequest> requests) {
		for (HttpRequest request : requests) {
			seed(request);
		}
		return this;
	}

	public GeccoEngine scheduler(Scheduler scheduler) {
		this.scheduler = scheduler;
		return this;
	}

	public GeccoEngine thread(int count) {
		this.threadCount = count;
		return this;
	}

	public GeccoEngine interval(int interval) {
		this.interval = interval;
		return this;
	}

	public GeccoEngine retry(int retry) {
		this.retry = retry;
		return this;
	}

	public GeccoEngine loop(boolean loop) {
		this.loop = loop;
		return this;
	}

	public GeccoEngine proxysLoader(Proxys proxysLoader) {
		this.proxysLoader = proxysLoader;
		return this;
	}
	
	public GeccoEngine proxy(boolean proxy) {
		this.proxy = proxy;
		return this;
	}

	public GeccoEngine mobile(boolean mobile) {
		this.mobile = mobile;
		return this;
	}

	public GeccoEngine debug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	public GeccoEngine monitor(boolean monitor) {
		this.monitor = monitor;
		return this;
	}

	public GeccoEngine classpath(String classpath) {
		classpaths.add(classpath);
		return this;
	}
	
	public GeccoEngine jmxPrefix(String jmxPrefix) {
		this.jmxPrefix = jmxPrefix;
		return this;
	}

	public GeccoEngine pipelineFactory(PipelineFactory pipelineFactory) {
		this.pipelineFactory = pipelineFactory;
		return this;
	}

    @Deprecated
	public GeccoEngine spiderBeanFactory(SpiderBeanFactory spiderBeanFactory) {
//		this.spiderBeanFactory = spiderBeanFactory;
//		return this;
        throw new RuntimeException("Deprecated. Correct approach (we use in gecco.injectable)"
                + " is controlling dependent objects via GeccoFactory, not via setters in GeccoEngine");
	}

	public void register(Class<?> spiderBeanClass) {
		context.getSpiderBeanFactory().addSpiderBean(spiderBeanClass);
	}

	public void unregister(Class<?> spiderBeanClass) {
		context.getSpiderBeanFactory().removeSpiderBean(spiderBeanClass);
		DynamicGecco.unregister(spiderBeanClass);
	}

	@Override
	public void run() {
		if (debug) {
			Logger log = LogManager.getLogger("com.geccocrawler.gecco.spider.render");
			log.setLevel(Level.DEBUG);
		}
        
        factory.setEngine(this);
        context.setFactory(factory);
		if(proxysLoader == null) {//默认采用proxys文件代理集合
			proxysLoader = factory.createProxys();
		}
		if (scheduler == null) {
            scheduler = factory.createScheduler();
		}
		if (context.getSpiderBeanFactory() == null) {
			if (classpaths.size() == 0) {
				// classpath不为空
				throw new IllegalArgumentException("classpath cannot be empty");
			}
            beforeSpiderBeanFactoryCreate();
			SpiderBeanFactory spiderBeanFactory = factory.createSpiderBeanFactory(classpaths, pipelineFactory);
            context.setSpiderBeanFactory(spiderBeanFactory);
		}
		if (threadCount <= 0) {
			threadCount = 1;
		}
		this.cdl = new CountDownLatch(threadCount);
        
        beforeStart();
        
		startsJson();
		if (startRequests.isEmpty()) {
			// startRequests不为空
			// throw new IllegalArgumentException("startRequests cannot be empty");
		}
		for (HttpRequest startRequest : startRequests) {
			scheduler.into(startRequest);
		}
        String classpathsConCat = String.join(", ", classpaths);
		spiders = new ArrayList<Spider>(threadCount);
		for (int i = 0; i < threadCount; i++) {
			Spider spider = factory.createSpider();
			spiders.add(spider);
			Thread thread = new Thread(spider, "T" + classpathsConCat + "_" + i);
			thread.start();
		}
		startTime = new Date();
		if(monitor) {
			// 监控爬虫基本信息
			GeccoMonitor.monitor(this);
			// 启动导出jmx信息
			GeccoJmx.export(jmxPrefix == null ? classpathsConCat : jmxPrefix);
		}
		// 非循环模式等待线程执行完毕后关闭
		closeUnitlComplete();
	}

	@Override
	public synchronized void start() {
        EventListener eventListener = context.getEventListener();
		if (eventListener != null) {
			eventListener.onStart(this);
		}
		super.start();
	}

	protected GeccoEngine startsJson() {
		try {
			URL url = Resources.getResource("starts.json");
			File file = new File(url.getPath());
			if (file.exists()) {
				String json = Files.toString(file, Charset.forName("UTF-8"));
                final Class<? extends StartRequestList> startRequestListClass = 
                        factory.getStartRequestListClass();
                List<? extends StartRequestList> list = JSON.parseArray(json, startRequestListClass);
				for (StartRequestList start : list) {
					start(start.toRequest(factory));
				}
			}
		} catch (IllegalArgumentException ex) {
			log.info("starts.json not found");
		} catch (IOException ioex) {
			log.error(ioex);
		}
		return this;
	}

	public Scheduler getScheduler() {
		return scheduler;
	}

	public int getInterval() {
		return interval;
	}

	public Date getStartTime() {
		return startTime;
	}

	public List<HttpRequest> getStartRequests() {
		return startRequests;
	}

	public List<Spider> getSpiders() {
		return spiders;
	}

	public int getRetry() {
		return retry;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public boolean isLoop() {
		return loop;
	}

	public Proxys getProxysLoader() {
		return proxysLoader;
	}

	public boolean isMobile() {
		return mobile;
	}

	public boolean isDebug() {
		return debug;
	}
	
	public boolean isProxy() {
		return proxy;
	}
	
	public boolean isMonitor() {
		return monitor;
	}

	/**
	 * spider线程告知engine执行结束
	 */
	public void notifyComplete() {
		this.cdl.countDown();
	}

	/**
	 * 非循环模式等待线程执行完毕后关闭
	 */
	public void closeUnitlComplete() {
		if (!loop) {
			try {
				cdl.await();
			} catch (InterruptedException e) {
                processCdlInterruptException(e);
			}
            
            SpiderBeanFactory spiderBeanFactory = context.getSpiderBeanFactory();
			if (spiderBeanFactory != null) {
				spiderBeanFactory.getDownloaderFactory().closeAll();
			}
			GeccoJmx.unexport();
			log.info("close gecco!");
		}

        EventListener eventListener = context.getEventListener();
		if (eventListener != null) {
			eventListener.onStop(this);
		}
	}
    
    public void processCdlInterruptException(InterruptedException e) {
        log.error(e);
    }

	/**
	 * 启动引擎，并返回GeccoEngine对象
	 * 
	 * @return GeccoEngine
	 */
	public GeccoEngine engineStart() {
		start();
		return this;
	}

	/**
	 * 暂停
	 */
	public void pause() {
		if (spiders != null) {
			for (Spider spider : spiders) {
				spider.pause();
			}
		}
        EventListener eventListener = context.getEventListener();
		if (eventListener != null) {
			eventListener.onPause(this);
		}
	}

	/**
	 * 重新开始抓取
	 */
	public void restart() {
		if (spiders != null) {
			for (Spider spider : spiders) {
				spider.restart();
			}
		}
        EventListener eventListener = context.getEventListener();
		if (eventListener != null) {
			eventListener.onRestart(this);
		}
	}

	public void beginUpdateRule() {
		if (log.isDebugEnabled()) {
			log.debug("begin update rule");
		}
		// 修改规则前需要暂停引擎并且重新创建ClassLoader
		pause();
		GeccoClassLoader.create();
	}

	public void endUpdateRule() {
		// 修改完成后重启引擎
		restart();
		if (log.isDebugEnabled()) {
			log.debug("end update rule");
		}
	}

	public void engineStop() {
		if (spiders != null) {
			for (Spider spider : spiders) {
				spider.stop();
			}
		}
        EventListener eventListener = context.getEventListener();
		if (eventListener != null) {
			eventListener.onStop(this);
		}
	}

    public GeccoFactory getFactory() {
        return factory;
    }

    public GeccoEngine setFactory(GeccoFactory factory) {
        this.factory = factory;
        return this;
    }
    
    public void wire(){
        factory.setEngine(this);
        context = factory.createContext();
        EventListener eventListener = factory.createEventListener();
        context.setEventListener(eventListener);
    }
    
    public GeccoContext getContext() {
        return context;
    }

    public void setContext(GeccoContext context) {
        this.context = context;
    }

    /**
     * Hint: overwrite me.
     */
    protected void beforeStart() {
    }
    
    /**
     * Hint: overwrite me.
     */
    protected void beforeSpiderBeanFactoryCreate() {
    }

	@Override
	public V call() throws Exception {
		run();
		return ret;
	}

}
