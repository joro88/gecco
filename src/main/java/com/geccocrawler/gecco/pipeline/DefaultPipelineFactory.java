package com.geccocrawler.gecco.pipeline;

import com.geccocrawler.gecco.GeccoContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.spider.SpiderBean;

public class DefaultPipelineFactory implements PipelineFactory {

	protected Map<String, Pipeline<? extends SpiderBean>> pipelines;

	@SuppressWarnings({ "unchecked" })
	public DefaultPipelineFactory(GeccoContext context, Reflections reflections) {
		this.pipelines = new HashMap<String, Pipeline<? extends SpiderBean>>();
		Set<Class<?>> pipelineClasses = reflections.getTypesAnnotatedWith(PipelineName.class);
		for (Class<?> pipelineClass : pipelineClasses) {
			PipelineName spiderFilter = pipelineClass.getAnnotation(PipelineName.class);
			try {
//                Pipeline<? extends SpiderBean> instance = (Pipeline<? extends SpiderBean>)pipelineClass.newInstance();
                Pipeline<? extends SpiderBean> instance = (Pipeline<? extends SpiderBean>) 
                        pipelineClass.getDeclaredConstructor(GeccoContext.class).newInstance(context);
				pipelines.put(spiderFilter.value(), instance);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public Pipeline<? extends SpiderBean> getPipeline(String name) {
		return pipelines.get(name);
	}

}
