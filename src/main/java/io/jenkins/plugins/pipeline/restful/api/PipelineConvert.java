package io.jenkins.plugins.pipeline.restful.api;

import org.jenkinsci.plugins.workflow.flow.FlowDefinition;

public interface PipelineConvert {
    boolean accept(FlowDefinition flowDefinition);

    Pipeline convert(FlowDefinition flowDefinition);
}
