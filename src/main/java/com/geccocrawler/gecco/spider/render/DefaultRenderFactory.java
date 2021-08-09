package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoFactory;
import org.reflections.Reflections;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public class DefaultRenderFactory extends RenderFactory {
	
	public DefaultRenderFactory(Reflections reflections, GeccoFactory factory) {
		super(reflections, factory);
	}

	public HtmlRender createHtmlRender() { // keep this method for compatibility and for easier merge process
		return factory.createHtmlRender(this);
	}
	
	public JsonRender createJsonRender() { // keep this method for compatibility and for easier merge process
		return factory.createJsonRender( this );
	}
	
}
