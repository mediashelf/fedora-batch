/**
 * Copyright (C) 2012 MediaShelf <http://www.yourmediashelf.com/>
 *
 * This file is part of fedora-batch.
 *
 * fedora-batch is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * fedora-batch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with fedora-batch.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yourmediashelf.fedora.batch;

import static com.yourmediashelf.fedora.client.FedoraClient.ingest;
import static com.yourmediashelf.fedora.client.FedoraClient.purgeObject;
import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.yourmediashelf.fedora.client.FedoraClient;
import com.yourmediashelf.fedora.client.FedoraClientException;
import com.yourmediashelf.fedora.client.FedoraCredentials;
import com.yourmediashelf.fedora.client.request.FedoraRequest;
import com.yourmediashelf.fedora.client.response.FedoraResponse;

/**
 * Base class for FedoraRequest integration tests.
 *
 * @author Edwin Shin
 */
public abstract class BaseFedoraRequestIT {

    private static FedoraCredentials credentials;

    protected static FedoraClient fedora;

    public final String testPid = "test-rest:1";

    @BeforeClass
    public static void createFedoraClient() throws Exception {
        fedora = new FedoraClient(getCredentials());
        fedora.debug(Boolean.parseBoolean(System.getProperty("test.debug")));
    }

    @Before
    public void setUp() throws Exception {
        FedoraRequest.setDefaultClient(fedora);
        ingestTestObject();
    }

    @After
    public void tearDown() throws Exception {
        FedoraRequest.setDefaultClient(fedora);
        purgeTestObject();
    }

    public static FedoraCredentials getCredentials()
            throws MalformedURLException {
        if (credentials == null) {
            String baseUrl = System.getProperty("fedora.test.baseUrl");
            String username = System.getProperty("fedora.test.username");
            String password = System.getProperty("fedora.test.password");
            credentials =
                    new FedoraCredentials(new URL(baseUrl), username, password);
        }
        return credentials;
    }

    protected void testNoDefaultClientRequest(FedoraRequest<?> request,
            int successStatusCode) throws FedoraClientException {
        FedoraRequest.setDefaultClient(null);
        FedoraResponse response = request.execute(fedora);
        assertEquals(successStatusCode, response.getStatus());
    }

    @Test
    abstract public void testNoDefaultClientRequest()
            throws FedoraClientException;

    public String getTestPid() {
        return testPid;
    }

    public void ingestTestObject() throws FedoraClientException {
        ingest(testPid).logMessage("ingestTestObject for " + getClass())
                .execute();
    }

    public void purgeTestObject() throws FedoraClientException {
        purgeObject(testPid).logMessage("purgeTestObject for " + getClass())
                .execute();
    }

    public XpathEngine getXpathEngine(Map<String, String> nsMap) {
        XpathEngine engine = XMLUnit.newXpathEngine();
        if (nsMap != null) {
            NamespaceContext ctx = new SimpleNamespaceContext(nsMap);
            engine.setNamespaceContext(ctx);
        }
        return engine;
    }

    public XpathEngine getXpathEngine(String prefix, String uri) {
        Map<String, String> nsMap = new HashMap<String, String>();
        nsMap.put(prefix, uri);
        return getXpathEngine(nsMap);
    }

    /**
     * <p>Returns true if the server version (provided by 
     * FedoraClient.getServerVersion()) is greater than or equal to the 
     * supplied version.
     * 
     * <p>Note that 3.6 is greater than 3.6-SNAPSHOT and 2.12 is greater than
     * 2.2.
     * 
     * @param version The version to compare against (e.g. 3.5 or 3.6-SNAPSHOT)
     * @return true if the server-provided version is greater than or equal to 
     * the supplied version
     * @throws FedoraClientException
     */
    public static boolean isVersionGreaterThanOrEqualTo(String version)
            throws FedoraClientException {
        ArtifactVersion a =
                new DefaultArtifactVersion(fedora.getServerVersion());
        ArtifactVersion b = new DefaultArtifactVersion(version);
        return a.compareTo(b) != -1;
    }

}
