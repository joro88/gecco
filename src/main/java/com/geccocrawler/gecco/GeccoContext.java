package com.geccocrawler.gecco;

import com.geccocrawler.gecco.listener.EventListener;
import com.geccocrawler.gecco.spider.SpiderBeanFactory;

/**
 *
 * @author joro88
 */
public class GeccoContext {
    protected GeccoEngine engine;
    protected GeccoFactory factory;
	protected EventListener eventListener;
    protected SpiderBeanFactory spiderBeanFactory;

    public GeccoContext(GeccoEngine engine) {
        this.engine = engine;
        engine.setContext(this);
    }
    
    public GeccoEngine getEngine() {
        return engine;
    }

    public GeccoContext setEngine(GeccoEngine engine) {
        this.engine = engine;
        return this;
    }

	public EventListener getEventListener() {
		return eventListener;
	}

	public GeccoContext setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
        return this;
	}

    public GeccoFactory getFactory() {
        return factory;
    }

    public void setFactory(GeccoFactory factory) {
        this.factory = factory;
    }

    public SpiderBeanFactory getSpiderBeanFactory() {
        return spiderBeanFactory;
    }

    public void setSpiderBeanFactory(SpiderBeanFactory spiderBeanFactory) {
        this.spiderBeanFactory = spiderBeanFactory;
    }
    
    
}
