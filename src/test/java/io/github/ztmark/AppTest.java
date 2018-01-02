package io.github.ztmark;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Author: Mark
 * Date  : 2018/1/2
 */
public class AppTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8680);


    @Test
    public void test() throws IOException {

        stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse().withStatus(200).withBody("hello there")));

        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final HttpGet httpGet = new HttpGet("http://localhost:8680/hello");
        final CloseableHttpResponse response = httpClient.execute(httpGet);
        final StatusLine statusLine = response.getStatusLine();
        assertThat(statusLine.getStatusCode(), is(200));
        final HttpEntity entity = response.getEntity();
        assertNotNull(entity);
        assertThat(EntityUtils.toString(entity), is("hello there"));
    }
}