package io.github.ztmark;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.ContentPattern;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

/**
 * Author: Mark
 * Date  : 2018/1/2
 */
public class AppTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8680);


    @Test
    public void testGet() {

        stubFor(get(urlEqualTo("/hello"))
                .willReturn(aResponse().withStatus(200).withBody("hello there")));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet httpGet = new HttpGet("http://localhost:8680/hello");

            String responseBody = httpClient.execute(httpGet, response -> {
                final StatusLine statusLine = response.getStatusLine();
                assertThat(statusLine.getStatusCode(), is(200));
                final HttpEntity entity = response.getEntity();
                assertNotNull(entity);
                return EntityUtils.toString(entity);
            });

            assertThat(responseBody, is("hello there"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetManual() throws IOException {
        stubFor(get(urlEqualTo("/get"))
                .willReturn(ok("get")
                        .withHeader("content-length", String.valueOf("get".getBytes(StandardCharsets.UTF_8).length))
                        .withHeader("content-encoding", StandardCharsets.UTF_8.name())));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpGet httpGet = new HttpGet("http://localhost:8680/get");

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                final int statusCode = response.getStatusLine().getStatusCode();
                assertThat(statusCode, is(200));
                final HttpEntity entity = response.getEntity();

                assertNotNull(entity);
                try (final InputStream inputStream = entity.getContent()){
                    assertTrue(entity.getContentLength() > 0);
                    final byte[] bytes = new byte[(int) entity.getContentLength()];
                    final int count = inputStream.read(bytes);
                    assertTrue(count > 0);
                    final String body = new String(bytes, StandardCharsets.UTF_8);
                    assertThat(body, is("get"));

                } catch (IOException | UnsupportedOperationException e) {
                    e.printStackTrace();
                    throw e;
                }
            }

        }

    }

    @Test
    public void testGetViaProxy() throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            final HttpHost proxy = new HttpHost("127.0.0.1", 1087, "http");
            final RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).build();
            final HttpGet httpGet = new HttpGet("https://www.google.com");
            httpGet.setConfig(requestConfig);
            final CloseableHttpResponse response = httpClient.execute(httpGet);
            assertThat(response.getStatusLine().getStatusCode(), is(200));
            final HttpEntity entity = response.getEntity();
            assertNotNull(entity);
        }
    }

    @Test
    public void testQueryParam() throws IOException, URISyntaxException {

        stubFor(post(urlEqualTo("/login?username=mark&password=121212")).willReturn(okJson("{'success':true}")));


        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (final CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build()) {


            final URI uri = new URIBuilder().setScheme("http")
                                                .setHost("localhost")
                                                .setPort(8680)
                                                .setPath("/login")
                                                .addParameter("username", "mark")
                                                .addParameter("password", "121212")
                                                .build();
            final HttpPost httpPost = new HttpPost(uri);
            final CloseableHttpResponse response = httpClient.execute(httpPost);
            assertThat(response.getStatusLine().getStatusCode(), is(200));
            final HttpEntity entity = response.getEntity();
            final String body = EntityUtils.toString(entity);
            assertThat(body, is("{'success':true}"));
        }
    }

}