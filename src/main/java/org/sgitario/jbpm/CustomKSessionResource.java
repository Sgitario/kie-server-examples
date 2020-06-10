package org.sgitario.jbpm;

import static org.kie.server.remote.rest.common.util.RestUtils.createResponse;
import static org.kie.server.remote.rest.common.util.RestUtils.getContentType;
import static org.kie.server.remote.rest.common.util.RestUtils.getVariant;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.drools.RulesExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("server/containers/instances/{containerId}/customksession")
public class CustomKSessionResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomKSessionResource.class);
	private static final KieCommands COMMANDS_FACTORY = KieServices.Factory.get().getCommands();

	private final RulesExecutionService rulesExecutionService;
	private final KieServerRegistry registry;

	public CustomKSessionResource(RulesExecutionService rulesExecutionService, KieServerRegistry registry) {
		this.rulesExecutionService = rulesExecutionService;
		this.registry = registry;
	}

	@POST
	@Path("/{ksessionId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insertFireReturn(@Context HttpHeaders headers, @PathParam("containerId") String id,
			@PathParam("ksessionId") String ksessionId, String cmdPayload) {
		LOGGER.info("Intercept '{}'", cmdPayload);
		try {
			KieContainerInstance kci = registry.getContainer(id);

			Marshaller marshaller = kci.getMarshaller(getMarshallingFormat(headers));

			List<?> listOfFacts = marshaller.unmarshall(cmdPayload, List.class);

			List<Command<?>> commands = new ArrayList<>();
			BatchExecutionCommand executionCommand = COMMANDS_FACTORY.newBatchExecution(commands, ksessionId);

			for (Object fact : listOfFacts) {
				commands.add(COMMANDS_FACTORY.newInsert(fact, fact.toString()));
			}
			commands.add(COMMANDS_FACTORY.newFireAllRules());
			commands.add(COMMANDS_FACTORY.newGetObjects());

			ExecutionResults results = rulesExecutionService.call(kci, executionCommand);

			String result = marshaller.marshall(results);

			LOGGER.info("Returning OK response with content '{}'", result);
			return createResponse(result, getVariant(headers), Response.Status.OK);
		} catch (Exception e) {
			// If marshalling fails, return the `call-container` response to maintain
			// backward compatibility:
			String response = "Execution failed with error : " + e.getMessage();
			LOGGER.info("Returning Failure response with content '{}'", response);
			return createResponse(response, getVariant(headers), Response.Status.INTERNAL_SERVER_ERROR);
		}

	}

	private MarshallingFormat getMarshallingFormat(HttpHeaders headers) {
		String contentType = getContentType(headers);
		MarshallingFormat format = MarshallingFormat.fromType(contentType);
		if (format == null) {
			format = MarshallingFormat.valueOf(contentType);
		}

		return format;
	}

}
