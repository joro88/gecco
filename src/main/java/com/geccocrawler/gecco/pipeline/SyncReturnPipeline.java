package com.geccocrawler.gecco.pipeline;

import com.geccocrawler.gecco.GeccoContext;
import com.geccocrawler.gecco.annotation.PipelineName;
import com.geccocrawler.gecco.spider.SpiderBean;
import com.geccocrawler.gecco.utils.EngineRetUtil;

@PipelineName("syncReturnPipeline")
public class SyncReturnPipeline implements Pipeline<SpiderBean> {

    /**
     *
     * @param context - it should be of type GeccoContext because it is created
     * by reflection
     */
    public SyncReturnPipeline(GeccoContext context) {
    }

	@Override
	public void process(SpiderBean bean) {
		EngineRetUtil.setRet(bean);
	}

}
