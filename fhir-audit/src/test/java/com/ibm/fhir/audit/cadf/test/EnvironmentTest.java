/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.audit.cadf.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.io.IOException;

import org.testng.annotations.Test;

import com.ibm.fhir.audit.kafka.Environment;
import com.ibm.fhir.audit.kafka.EventStreamsCredentials;
import com.ibm.fhir.exception.FHIRException;

public class EnvironmentTest {

    @Test
    public void testEnvironment() throws FHIRException, IOException {
        Environment e = new Environment(); 
        assertNotNull(e); 
        
        String[] kafkaBrokerSasls = new String[1];
        kafkaBrokerSasls[0] = "test";

        EventStreamsCredentials esc =
                EventStreamsCredentials.builder().build();

        esc =
                EventStreamsCredentials.builder().apiKey("apiKey")
                        .kafkaBrokersSasl(kafkaBrokerSasls).password("password").user("user").build();

        String jsonString = EventStreamsCredentials.Writer.generate(esc);
        esc = EventStreamsCredentials.Parser.parse(jsonString);

        assertEquals(esc.getApiKey(), "apiKey");
        assertEquals(esc.getPassword(), "password");
        assertEquals(esc.getUser(), "user");
        assertEquals(esc.getKafkaBrokersSasl().length, 1);
        assertEquals(esc.getKafkaBrokersSasl()[0], "test");

        esc = Environment.getEventStreamsCredentials();
        assertNull(esc);
        
        esc = Environment.parseEventStreamsCredentials("{}{\n" + 
                "    \"api_key\": \"apiKey\",\n" + 
                "    \"password\": \"password\",\n" + 
                "    \"user\": \"user\",\n" + 
                "    \"kafka_brokers_sasl\": [\n" + 
                "        \"test\"\n" + 
                "    ]\n" + 
                "}");
        assertNotNull(esc);
        
        esc = Environment.parseEventStreamsCredentials("{\n" + 
                "    \"api_key\": \"apiKey\",\n" + 
                "    \"password\": \"password\",\n" + 
                "    \"user\": \"user\",\n" + 
                "    \"kafka_brokers_sasl\": [\n" + 
                "        \"test\"\n" + 
                "    ]\n" + 
                "}");
        assertNotNull(esc);
        
        // Invalid
        esc = Environment.parseEventStreamsCredentials("" + 
                "    \"api_key\": \"apiKey\",\n" + 
                "        \"test\"\n" + 
                "    ]\n" + 
                "}");
        assertNull(esc);
    }
}