package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoFactory;
import com.geccocrawler.gecco.GeccoMediator;
import java.util.HashMap;
import java.util.Map;

import org.reflections.Reflections;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public abstract class RenderFactory {
	
	protected Map<RenderType, Render> renders;
    
    protected GeccoMediator mediator;
    protected GeccoFactory factory;
	
	public RenderFactory(Reflections reflections, GeccoMediator mediator) {
        this.mediator = mediator;
        this.factory = mediator.getFactory();
        
		CustomFieldRenderFactory customFieldRenderFactory = factory.createCustomFieldRenderFactory(reflections, this);
		renders = new HashMap<RenderType, Render>();
		
		AbstractRender htmlRender = createHtmlRender();
		htmlRender.setCustomFieldRenderFactory(customFieldRenderFactory);
		
		AbstractRender jsonRender = createJsonRender();
		jsonRender.setCustomFieldRenderFactory(customFieldRenderFactory);
		
		renders.put(RenderType.HTML, htmlRender);
		renders.put(RenderType.JSON, jsonRender);
	}
	
	public Render getRender(RenderType type) {
		return renders.get(type);
	}
	
	public abstract HtmlRender createHtmlRender();
	
	public abstract JsonRender createJsonRender();
	
}
