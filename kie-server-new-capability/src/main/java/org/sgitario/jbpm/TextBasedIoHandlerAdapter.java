package org.sgitario.jbpm;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieContainerCommandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBasedIoHandlerAdapter extends IoHandlerAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(TextBasedIoHandlerAdapter.class);

	private final KieContainerCommandService batchCommandService;

	public TextBasedIoHandlerAdapter(KieContainerCommandService batchCommandService) {
		this.batchCommandService = batchCommandService;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		String completeMessage = message.toString();
		LOG.debug("Received message '{}'", completeMessage);
		if (completeMessage.trim().equalsIgnoreCase("exit")) {
			session.closeOnFlush();
			return;
		}

		String[] elements = completeMessage.split("\\|");
		LOG.debug("Container id {}", elements[0]);
		ServiceResponse<String> result = batchCommandService.callContainer(elements[0], elements[1],
				MarshallingFormat.JSON, null);

		if (result.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {
			session.write(result.getResult());
			LOG.debug("Successful message written with content '{}'", result.getResult());
		} else {
			session.write(result.getMsg());
			LOG.debug("Failure message written with content '{}'", result.getMsg());
		}
	}
}