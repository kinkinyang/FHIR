/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.operation.reindex;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.fhir.exception.FHIROperationException;
import com.ibm.fhir.model.format.Format;
import com.ibm.fhir.model.parser.FHIRParser;
import com.ibm.fhir.model.resource.OperationDefinition;
import com.ibm.fhir.model.resource.OperationOutcome;
import com.ibm.fhir.model.resource.OperationOutcome.Issue;
import com.ibm.fhir.model.resource.Parameters;
import com.ibm.fhir.model.resource.Resource;
import com.ibm.fhir.model.type.code.IssueSeverity;
import com.ibm.fhir.server.operation.spi.AbstractOperation;
import com.ibm.fhir.server.operation.spi.FHIROperationContext;
import com.ibm.fhir.server.operation.spi.FHIRResourceHelpers;
import com.ibm.fhir.server.util.FHIROperationUtil;

/**
 * Custom operation to invoke the persistence layer reindexing of resources
 */
public class ReindexOperation extends AbstractOperation {
    private static final Logger logger = Logger.getLogger(ReindexOperation.class.getName());
    
    private static final String PARAM_TSTAMP = "_tstamp";
    private static final String PARAM_RESOURCE_COUNT = "_resourceCount";
    
    static final DateTimeFormatter DAY_FORMAT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(ZoneId.of("UTC"));
    
    public ReindexOperation() {
        super();
    }

    @Override
    protected OperationDefinition buildOperationDefinition() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("reindex.json")) {
            return FHIRParser.parser(Format.JSON).parse(in);
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    @Override
    protected Parameters doInvoke(FHIROperationContext operationContext, Class<? extends Resource> resourceType,
            String logicalId, String versionId, Parameters parameters, FHIRResourceHelpers resourceHelper)
            throws FHIROperationException {
        
        // Allow only POST because we're changing the state of the database
        String method = (String) operationContext.getProperty(FHIROperationContext.PROPNAME_METHOD_TYPE);
        if (!"POST".equalsIgnoreCase(method)) {
            throw new FHIROperationException("HTTP method not supported: " + method);
        }
        
        try {
            Instant tstamp = Instant.now();
            int resourceCount = 10;
            
            if (parameters != null) {
                for (Parameters.Parameter parameter : parameters.getParameter()) {
                    if (parameter.getValue() != null) {
                        logger.fine("reindex param: " + parameter.getName().getValue() + " = " + parameter.getValue().toString());
                    }
                    
                    if (PARAM_TSTAMP.equals(parameter.getName().getValue())
                            && parameter.getValue() != null
                            && parameter.getValue().is(com.ibm.fhir.model.type.String.class)) {
                        String val = parameter.getValue().as(com.ibm.fhir.model.type.String.class).getValue();
                        
                        
                        if (val.length() == 10) {
                            tstamp = DAY_FORMAT.parse(val, Instant::from);
                        } else {
                            // assume full ISO format
                            tstamp = Instant.parse(val);
                        }
                        
                    } else if (PARAM_RESOURCE_COUNT.equals(parameter.getName().getValue())) {
                        Integer val = parameter.getValue().as(com.ibm.fhir.model.type.Integer.class).getValue();
                        if (val != null) {
                            resourceCount = val;
                        }
                    }
                }
            }
            
            // Delegate the heavy lifting to the helper
            OperationOutcome operationOutcome = resourceHelper.doReindex(operationContext, tstamp, resourceCount);
            checkOperationOutcome(operationOutcome);
            return FHIROperationUtil.getOutputParameters(operationOutcome);
        } catch (FHIROperationException e) {
            throw e;
        } catch (Throwable t) {
            throw new FHIROperationException("Unexpected error occurred while processing request for operation '"
                    + getName() + "': " + getCausedByMessage(t), t);
        }
    }

    /**
     * Check the OperationOutcome for any errors
     * @param oo
     * @throws FHIROperationException
     */
    private void checkOperationOutcome(OperationOutcome oo) throws FHIROperationException {
        List<Issue> issues = oo.getIssue();
        for (Issue issue : issues) {
            IssueSeverity severity = issue.getSeverity();
            if (severity != null && (IssueSeverity.ERROR.getValue().equals(severity.getValue())
                    || IssueSeverity.FATAL.getValue().equals(severity.getValue()))) {
                throw new FHIROperationException("The persistence layer reported one or more issues").withIssue(issues);
            }
        }
    }

    private String getCausedByMessage(Throwable throwable) {
        return throwable.getClass().getName() + ": " + throwable.getMessage();
    }
}