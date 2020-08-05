package org.sgitario.jbpm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MinaDroolsKieServerExtension implements KieServerExtension {

	public static final String DROOLS_EXTENSION = DroolsKieServerExtension.EXTENSION_NAME;
	public static final String EXTENSION_NAME = "Drools-Mina";
	public static final String MINA_CAPABILITY = "BRM-Mina";

	private static final Logger LOG = LoggerFactory.getLogger(MinaDroolsKieServerExtension.class);

	private static final String MINA_HOST = System.getProperty("org.kie.server.drools-mina.ext.port", "localhost");
	private static final String MINA_PORT = System.getProperty("org.kie.server.drools-mina.ext.port", "9123");

	private IoAcceptor acceptor;
	private boolean initialized = false;

	@Override
	public boolean isActive() {
		return true;
	}

	@Override
	public void init(KieServerImpl kieServer, KieServerRegistry registry) {

		KieServerExtension droolsExtension = registry.getServerExtension(DROOLS_EXTENSION);
		if (droolsExtension == null) {
			LOG.warn("No Drools extension available, quiting...");
			return;
		}

		KieContainerCommandService batchCommandService = findByType(droolsExtension.getServices(),
				KieContainerCommandService.class);

		if (batchCommandService != null) {
			acceptor = new NioSocketAcceptor();
			acceptor.getFilterChain().addLast("codec",
					new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"))));

			acceptor.setHandler(new TextBasedIoHandlerAdapter(batchCommandService));
			acceptor.getSessionConfig().setReadBufferSize(2048);
			acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
			try {
				acceptor.bind(new InetSocketAddress(MINA_HOST, Integer.parseInt(MINA_PORT)));

				LOG.info("{} -- Mina server started at {} and port {}", toString(), MINA_HOST, MINA_PORT);
			} catch (IOException e) {
				LOG.error("Unable to start Mina acceptor due to {}", e.getMessage(), e);
			}

			initialized = true;
		}
	}

	@Override
	public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
		if (acceptor != null) {
			acceptor.dispose();
			acceptor = null;
		}

		LOG.info("{} -- Mina server stopped", toString());
	}

	@Override
	public List<Object> getAppComponents(SupportedTransports type) {
		// Nothing for supported transports (REST or JMS)
		return Collections.emptyList();
	}

	@Override
	public <T> T getAppComponents(Class<T> serviceType) {

		return null;
	}

	@Override
	public String getImplementedCapability() {
		return MINA_CAPABILITY;
	}

	@Override
	public List<Object> getServices() {
		return Collections.emptyList();
	}

	@Override
	public String getExtensionName() {
		return EXTENSION_NAME;
	}

	@Override
	public Integer getStartOrder() {
		return 20;
	}

	@Override
	public String toString() {
		return EXTENSION_NAME + " KIE Server extension";
	}

	@Override
	public boolean isInitialized() {
		return initialized;
	}

	@Override
	public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
		// Empty, already handled by the `Drools` extension
	}

	@Override
	public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
		// Empty, already handled by the `Drools` extension
	}

	@Override
	public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance,
			Map<String, Object> parameters) {
		// Empty, already handled by the `Drools` extension
		return false;
	}

	@Override
	public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
		// Empty, already handled by the `Drools` extension
	}

	@SuppressWarnings("unchecked")
	private <T> T findByType(List<Object> services, Class<T> clazz) {
		return services.stream().filter(svc -> clazz.isAssignableFrom(svc.getClass())).map(svc -> (T) svc).findFirst()
				.get();
	}
}