package com.geccocrawler.gecco;

import com.geccocrawler.gecco.listener.EventListener;
import com.geccocrawler.gecco.pipeline.PipelineFactory;
import com.geccocrawler.gecco.spider.SpiderBeanFactory;
import com.geccocrawler.gecco.spider.conversion.Conversion;

/**
 *
 * @author joro88
 */
public class GeccoContext {
    protected GeccoEngine engine;
    protected GeccoFactory factory;
	protected EventListener eventListener;
    protected SpiderBeanFactory spiderBeanFactory;
    protected Conversion fieldConversion;

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
        fieldConversion = factory.createFieldConversion();
    }

    public SpiderBeanFactory getSpiderBeanFactory() {
        return spiderBeanFactory;
    }

    public void setSpiderBeanFactory(SpiderBeanFactory spiderBeanFactory) {
        this.spiderBeanFactory = spiderBeanFactory;
    }
    
    public PipelineFactory getPipelineFactory() {
        return spiderBeanFactory.getPipelineFactory();
    }

    public Conversion getFieldConversion() {
        return fieldConversion;
    }

    public void setFieldConversion(Conversion fieldConversion) {
        this.fieldConversion = fieldConversion;
    }

}
