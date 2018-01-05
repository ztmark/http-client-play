package io.github.ztmark;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.common.ConsoleNotifier;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Author: Mark
 * Date  : 2018/1/5
 */
public class OkHttpDemoTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(options().port(8680).notifier(new ConsoleNotifier(true)));

    private OkHttpClient okHttpClient = new OkHttpClient();

    @Test
    public void testGetParam() throws IOException {
        stubFor(get(urlPathEqualTo("/param")).withQueryParam("name", equalTo("mark")).willReturn(ok("done")));

        final HttpUrl httpUrl = new HttpUrl.Builder().scheme("http").host("localhost").port(8680).addPathSegment("param").addQueryParameter("name", "mark").build();
        final Request request = new Request.Builder().get().url(httpUrl).build();
        final Response response = okHttpClient.newCall(request).execute();
        assertTrue(response.isSuccessful());
        final ResponseBody body = response.body();
        assertNotNull(body);
        assertThat(body.string(), is("done"));
    }
}