package org.sgitario.jbpm;

import org.jbpm.executor.AsynchronousJobListener;
import org.jbpm.services.api.DeploymentEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.dmn.api.core.event.DMNRuntimeEventListener;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.prometheus.PrometheusMetricsProvider;
import org.optaplanner.core.impl.phase.event.PhaseLifecycleListener;

public class RulesPrometheusMetricsProvider implements PrometheusMetricsProvider {

	@Override
	public DMNRuntimeEventListener createDMNRuntimeEventListener(KieContainerInstance kContainer) {
		return null;
	}

	@Override
	public AgendaEventListener createAgendaEventListener(String kieSessionId, KieContainerInstance kContainer) {
		return new RulesMatchedCountAgendaEventListener();
	}

	@Override
	public PhaseLifecycleListener createPhaseLifecycleListener(String solverId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AsynchronousJobListener createAsynchronousJobListener() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DeploymentEventListener createDeploymentEventListener() {
		// TODO Auto-generated method stub
		return null;
	}

}
