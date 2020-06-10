package org.sgitario.jbpm;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.RulesExecutionService;

public class CustomKieServerApplicationComponentsService implements KieServerApplicationComponentsService {

	private static final String OWNER_EXTENSION = "Drools";

	@Override
	public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
		// Do not accept calls from extensions other than the owner extension
		// Only REST transport is implemented
		if (!OWNER_EXTENSION.equals(extension) || !SupportedTransports.REST.equals(type)) {
			return Collections.emptyList();
		}

		RulesExecutionService rulesExecutionService = findByType(services, RulesExecutionService.class);
		KieServerRegistry context = findByType(services, KieServerRegistry.class);
		Object resource = new CustomKSessionResource(rulesExecutionService, context);
		return Arrays.asList(resource);
	}

	@SuppressWarnings("unchecked")
	private <T> T findByType(Object[] services, Class<T> clazz) {
		return Stream.of(services).filter(svc -> clazz.isAssignableFrom(svc.getClass())).map(svc -> (T) svc).findFirst()
				.get();
	}

}