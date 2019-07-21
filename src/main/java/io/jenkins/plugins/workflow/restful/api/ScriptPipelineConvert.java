package io.jenkins.plugins.workflow.restful.api;

import hudson.Extension;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;

@Extension
public class ScriptPipelineConvert implements PipelineConvert{
    @Override
    public boolean accept(FlowDefinition flowDefinition) {
        return flowDefinition instanceof CpsFlowDefinition;
    }

    @Override
    public Pipeline convert(FlowDefinition flowDefinition) {
        CpsFlowDefinition def = (CpsFlowDefinition) flowDefinition;
        ScriptPipeline pipeline = new ScriptPipeline();
        pipeline.setScript(def.getScript());
        pipeline.setSandbox(def.isSandbox());
        return pipeline;
    }
}
