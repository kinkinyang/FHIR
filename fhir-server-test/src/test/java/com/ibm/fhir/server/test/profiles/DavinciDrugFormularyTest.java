/*
 * (C) Copyright IBM Corp. 2020
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.server.test.profiles;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ibm.fhir.client.FHIRParameters;
import com.ibm.fhir.client.FHIRResponse;
import com.ibm.fhir.core.FHIRMediaType;
import com.ibm.fhir.model.resource.Bundle;
import com.ibm.fhir.model.resource.MedicationKnowledge;
import com.ibm.fhir.model.test.TestUtil;

/**
 * Tests using http://hl7.org/fhir/us/davinci-drug-formulary/index.html#anticipated-client-queries and the given
 * profile.
 */
public class DavinciDrugFormularyTest extends ProfilesTestBase {
    private static final String CLASSNAME = DavinciDrugFormularyTest.class.getName();
    private static final Logger logger = Logger.getLogger(CLASSNAME);

    private String listCoveragePlan3001 = null;
    private String listCoveragePlan3002 = null;
    private String listCoveragePlan3004t = null;
    private String listCoveragePlan1002 = null;

    private String medicationKnowledgeCmsip9 = null;
    private String medicationKnowledge1002 = null;
    private String medicationKnowledge3001 = null;

    public Boolean skip = Boolean.TRUE;

    @Override
    public List<String> getRequiredProfiles() {
        //@formatter:off
        return Arrays.asList(
            "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-CoveragePlan|1.0.0",
            "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug|1.0.0");
        //@formatter:on
    }

    @Override
    public void setCheck(Boolean check) {
        this.skip = check;

        if (skip) {
            logger.info("Skipping Tests");
        }
    }

    public void loadCoveragePlan1() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/List-CoveragePlanV3001.json";
        WebTarget target = getWebTarget();
        com.ibm.fhir.model.resource.List list = TestUtil.readExampleResource(resource);
        Entity<com.ibm.fhir.model.resource.List> entity = Entity.entity(list, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("List").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        listCoveragePlan3001 = getLocationLogicalId(response);
        response = target.path("List/" + listCoveragePlan3001).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadCoveragePlan2() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/List-CoveragePlanV3002.json";
        WebTarget target = getWebTarget();
        com.ibm.fhir.model.resource.List list = TestUtil.readExampleResource(resource);
        Entity<com.ibm.fhir.model.resource.List> entity = Entity.entity(list, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("List").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        listCoveragePlan3002 = getLocationLogicalId(response);
        response = target.path("List/" + listCoveragePlan3002).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadCoveragePlan3() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/List-CoveragePlanV3004t.json";
        WebTarget target = getWebTarget();
        com.ibm.fhir.model.resource.List list = TestUtil.readExampleResource(resource);
        Entity<com.ibm.fhir.model.resource.List> entity = Entity.entity(list, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("List").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        listCoveragePlan3004t = getLocationLogicalId(response);
        response = target.path("List/" + listCoveragePlan3004t).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadCoveragePlan4() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/List-covplanV1002.json";
        WebTarget target = getWebTarget();
        com.ibm.fhir.model.resource.List list = TestUtil.readExampleResource(resource);
        Entity<com.ibm.fhir.model.resource.List> entity = Entity.entity(list, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("List").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        listCoveragePlan1002 = getLocationLogicalId(response);
        response = target.path("List/" + listCoveragePlan1002).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadMedicationKnowledge1() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/MedicationKnowledge-cmsip9.json";
        WebTarget target = getWebTarget();
        MedicationKnowledge mk = TestUtil.readExampleResource(resource);
        Entity<MedicationKnowledge> entity = Entity.entity(mk, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("MedicationKnowledge").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        medicationKnowledgeCmsip9 = getLocationLogicalId(response);
        response = target.path("MedicationKnowledge/" + medicationKnowledgeCmsip9).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadMedicationKnowledge2() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/MedicationKnowledge-formularydrugV1002.json";
        WebTarget target = getWebTarget();
        MedicationKnowledge mk = TestUtil.readExampleResource(resource);
        Entity<MedicationKnowledge> entity = Entity.entity(mk, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("MedicationKnowledge").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        medicationKnowledge1002 = getLocationLogicalId(response);
        response = target.path("MedicationKnowledge/" + medicationKnowledge1002).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void loadMedicationKnowledge3() throws Exception {
        String resource = "json/profiles/fhir-ig-davinci-pdex-formulary/MedicationKnowledge-FormularyDrugV3001.json";
        WebTarget target = getWebTarget();
        MedicationKnowledge mk = TestUtil.readExampleResource(resource);
        Entity<MedicationKnowledge> entity = Entity.entity(mk, FHIRMediaType.APPLICATION_FHIR_JSON);
        Response response = target.path("MedicationKnowledge").request().post(entity, Response.class);
        assertResponse(response, Response.Status.CREATED.getStatusCode());
        medicationKnowledge3001 = getLocationLogicalId(response);
        response = target.path("MedicationKnowledge/" + medicationKnowledge3001).request(FHIRMediaType.APPLICATION_FHIR_JSON).get();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    // Load Resources
    @BeforeClass
    public void loadResources() throws Exception {
        if (!skip) {
            loadCoveragePlan1();
            loadCoveragePlan2();
            loadCoveragePlan3();
            loadCoveragePlan4();
            loadMedicationKnowledge1();
            loadMedicationKnowledge2();
            loadMedicationKnowledge3();
        }
    }

    // Delete Resources
    public void deleteCoveragePlan1() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("List/" + listCoveragePlan3001).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteCoveragePlan2() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("List/" + listCoveragePlan3002).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteCoveragePlan3() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("List/" + listCoveragePlan3004t).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteCoveragePlan4() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("List/" + listCoveragePlan1002).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteMedicationKnowledge1() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("MedicationKnowledge/" + medicationKnowledgeCmsip9).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteMedicationKnowledge2() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("MedicationKnowledge/" + medicationKnowledge1002).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    public void deleteMedicationKnowledge3() throws Exception {
        WebTarget target = getWebTarget();
        Response response = target.path("MedicationKnowledge/" + medicationKnowledge3001).request(FHIRMediaType.APPLICATION_FHIR_JSON).delete();
        assertResponse(response, Response.Status.OK.getStatusCode());
    }

    @AfterClass
    public void deleteResources() throws Exception {
        if (!skip) {
            deleteCoveragePlan1();
            deleteCoveragePlan2();
            deleteCoveragePlan3();
            deleteCoveragePlan4();
            deleteMedicationKnowledge1();
            deleteMedicationKnowledge2();
            deleteMedicationKnowledge3();
        }
    }

    @Test
    public void testFindAllCoveragePlans() throws Exception {
        if (!skip) {
            // GET
            // [base]/List?_profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-CoveragePlan
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-CoveragePlan");
            parameters.searchParam("_count", "100");
            FHIRResponse response = client.search(com.ibm.fhir.model.resource.List.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, listCoveragePlan3001);
            assertContainsIds(bundle, listCoveragePlan3002);
            assertContainsIds(bundle, listCoveragePlan3004t);
            assertContainsIds(bundle, listCoveragePlan1002);
        }
    }

    @Test
    public void testFindCoveragePlanByPlanId() throws Exception {
        if (!skip) {
            // GET
            // [base]/List?_profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-CoveragePlan&identifier=myPlanID
            // Query for Find CoveragePlan by its PlanID has incorrect search code
            // https://jira.hl7.org/browse/FHIR-28362
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-CoveragePlan");
            parameters.searchParam("identifier", "HIOS-PLAN-ID");

            FHIRResponse response = client.search(com.ibm.fhir.model.resource.List.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, listCoveragePlan1002);
        }
    }

    @Test
    public void testFindAllFormularyDrugsInACoveragePlan() throws Exception {
        if (!skip) {
            // GET
            // [base]/MedicationKnowledge?_profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug&DrugPlan=myPlanID
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug");
            parameters.searchParam("DrugPlan", "40308VA0240008");

            FHIRResponse response = client.search(com.ibm.fhir.model.resource.MedicationKnowledge.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, medicationKnowledgeCmsip9);
        }
    }

    @Test
    public void testFindAllFormularyDrugsInASpecificTierOfCoveragePlan() throws Exception {
        if (!skip) {
            // GET [base]/MedicationKnowledge?
            // _profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug
            // &DrugPlan=myPlanID&DrugTier=GENERIC
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug");
            parameters.searchParam("DrugPlan", "40308VA0240008");
            parameters.searchParam("DrugTier", "generic");

            FHIRResponse response = client.search(com.ibm.fhir.model.resource.MedicationKnowledge.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, medicationKnowledgeCmsip9);
        }
    }

    @Test
    public void testFindAFormularyDrugByCodeInACoveragePlan() throws Exception {
        if (!skip) {
            // GET
            // [base]/MedicationKnowledge?_profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug
            // &DrugPlan=myPlanID&code=myCode
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug");
            parameters.searchParam("DrugPlan", "40308VA0240008");
            parameters.searchParam("code", "1000091");

            FHIRResponse response = client.search(com.ibm.fhir.model.resource.MedicationKnowledge.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, medicationKnowledgeCmsip9);
        }
    }

    @Test
    public void testFindAFormularyDrugByCodeAcrossAllCoveragePlans() throws Exception {
        if (!skip) {
            // GET
            // [base]/MedicationKnowledge?_profile=http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug
            // &code=myCode
            FHIRParameters parameters = new FHIRParameters();
            parameters.searchParam("_profile", "http://hl7.org/fhir/us/davinci-drug-formulary/StructureDefinition/usdf-FormularyDrug");
            parameters.searchParam("code", "1000091");

            FHIRResponse response = client.search(com.ibm.fhir.model.resource.MedicationKnowledge.class.getSimpleName(), parameters);
            assertSearchResponse(response, Response.Status.OK.getStatusCode());
            Bundle bundle = response.getResource(Bundle.class);
            assertNotNull(bundle);
            assertTrue(bundle.getEntry().size() >= 1);
            assertContainsIds(bundle, medicationKnowledgeCmsip9);
        }
    }
}