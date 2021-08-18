package com.geccocrawler.gecco.spider.render;

import com.geccocrawler.gecco.GeccoFactory;
import com.geccocrawler.gecco.GeccoMediator;
import org.reflections.Reflections;

import net.sf.cglib.proxy.Enhancer;

import com.geccocrawler.gecco.spider.render.html.HtmlRender;
import com.geccocrawler.gecco.spider.render.json.JsonRender;

public class MonitorRenderFactory extends RenderFactory {

	public MonitorRenderFactory(Reflections reflections, GeccoMediator mediator) {
		super(reflections, mediator);
	}

	@Override
	public HtmlRender createHtmlRender() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(HtmlRender.class);
		enhancer.setCallback( factory.createRenderMointorIntercetor(this) );
		return (HtmlRender)enhancer.create(new Class[]{GeccoFactory.class}, new Object[] {factory});
	}

	@Override
	public JsonRender createJsonRender() {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(JsonRender.class);
		enhancer.setCallback( factory.createRenderMointorIntercetor(this) );
		return (JsonRender)enhancer.create(new Class[]{GeccoFactory.class}, new Object[] {factory});
	}
	
	
	
}
