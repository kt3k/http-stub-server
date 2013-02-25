package au.com.sensis.stubby.test;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.HttpClientUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import au.com.sensis.stubby.test.model.JsonExchangeList;

public abstract class TestBase {

    private static final String P_TEST_SERVER = "test.server"; // server to test against

    protected Client client;

    protected String getStandaloneServer() {
        if (!TestServer.isRunning()) { // keep running for all tests
            TestServer.start();
        }
        return String.format("http://localhost:%d", TestServer.getPort());
    }

    @Before
    public void before() {
        String testServer = System.getProperty(P_TEST_SERVER);

        if (testServer == null) { // property not given, start internal server
            testServer = getStandaloneServer();
        }

        client = new Client(testServer);
        client.reset();
    }

    @After
    public void after() {
        client.close();
    }

    protected void postFile(String filename) {
        String path = "/tests/" + filename;
        InputStream resource = getClass().getResourceAsStream(path);
        if (resource != null) {
            try {
                client.postMessage(IOUtils.toString(resource));
            } catch (IOException e) {
                throw new RuntimeException("Error posting file", e);
            }
        } else {
            throw new RuntimeException("Resource not found: " + path);
        }
    }

    protected String makeUri(String path) {
        return client.makeUri(path);
    }

    protected void close(HttpResponse response) {
        HttpClientUtils.closeQuietly(response);
    }

    protected GenericClientResponse execute(HttpUriRequest request) {
        return client.execute(request);
    }

    protected Client client() {
        return client;
    }

    protected JsonExchangeList responses() {
        return client.getResponses();
    }

    protected void assertOk(GenericClientResponse response) {
        assertStatus(HttpStatus.SC_OK, response);
    }

    protected void assertStatus(int status, GenericClientResponse response) {
        Assert.assertEquals(status, response.getStatus());
    }

    protected MessageBuilder builder() {
        return new MessageBuilder(client);
    }

}
