package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoContext;
import org.reflections.Reflections;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public class DefaultRenderFactory extends RenderFactory {
	
	public DefaultRenderFactory(Reflections reflections, GeccoContext context) {
		super(reflections, context);
	}

	public HtmlRender createHtmlRender() { // keep this method for compatibility and for easier merge process
		return factory.createHtmlRender(this);
	}
	
	public JsonRender createJsonRender() { // keep this method for compatibility and for easier merge process
		return factory.createJsonRender( this );
	}
	
}
