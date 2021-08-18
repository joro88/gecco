package com.geccocrawler.gecco;

import com.geccocrawler.gecco.listener.EventListener;

/**
 *
 * @author joro88
 */
public class GeccoMediator {
    protected GeccoEngine engine;
    protected GeccoFactory factory;
	protected EventListener eventListener;

    public GeccoMediator(GeccoEngine engine) {
        this.engine = engine;
    }
    
    public GeccoEngine getEngine() {
        return engine;
    }

    public GeccoMediator setEngine(GeccoEngine engine) {
        this.engine = engine;
        return this;
    }

	public EventListener getEventListener() {
		return eventListener;
	}

	public GeccoMediator setEventListener(EventListener eventListener) {
		this.eventListener = eventListener;
        return this;
	}

    public GeccoFactory getFactory() {
        return factory;
    }

    public void setFactory(GeccoFactory factory) {
        this.factory = factory;
    }
    
    
}
