package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoContext;
import org.reflections.Reflections;

import net.sf.cglib.proxy.Enhancer;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public class MonitorRenderFactory extends RenderFactory {

	public MonitorRenderFactory(Reflections reflections, GeccoContext context) {
		super(reflections, context);
	}

	@Override
	public HtmlRender createHtmlRender() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(factory.getHtmlRenderClass(this));
		enhancer.setCallback( factory.createRenderMointorIntercetor(this) );
		return (HtmlRender)enhancer.create(new Class[]{GeccoContext.class}, new Object[] {context});
	}

	@Override
	public JsonRender createJsonRender() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass( factory.getJsonRenderClass(this) );
		enhancer.setCallback( factory.createRenderMointorIntercetor(this) );
		return (JsonRender)enhancer.create(new Class[]{GeccoContext.class}, new Object[] {context});
	}
	
	
	
}
