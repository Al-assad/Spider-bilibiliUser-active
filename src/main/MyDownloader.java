package main;

import com.google.common.collect.Sets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.HttpClientGenerator;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.UrlUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * create by Intellij IDEA
 * Author: Al-assad
 * E-mail: yulinying_1994@outlook.com
 * Github: https://github.com/Al-assad
 * Date: 2017/4/13 21:01
 * Description:  自定义Downloader，基于 HttpClientDownLoader ，
 *              修改部分方法的实现，主要增加应对 B站服务器 403 拒绝服务的应对措施；
 */
public class MyDownloader extends AbstractDownloader {

    //
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

        private Logger logger = LoggerFactory.getLogger(this.getClass());
        private final Map<String, CloseableHttpClient> httpClients = new HashMap();
        private HttpClientGenerator httpClientGenerator = new HttpClientGenerator();

        public MyDownloader() {
        }

        private CloseableHttpClient getHttpClient(Site site) {
            if(site == null) {
                return this.httpClientGenerator.getClient((Site)null);
            } else {
                String domain = site.getDomain();
                CloseableHttpClient httpClient = (CloseableHttpClient)this.httpClients.get(domain);
                if(httpClient == null) {
                    synchronized(this) {
                        httpClient = (CloseableHttpClient)this.httpClients.get(domain);
                        if(httpClient == null) {
                            httpClient = this.httpClientGenerator.getClient(site);
                            this.httpClients.put(domain, httpClient);
                        }
                    }
                }

                return httpClient;
            }
        }

        public Page download(Request request, Task task) {
            Site site = null;
            if(task != null) {
                site = task.getSite();
            }

            String charset = null;
            Map headers = null;
            Object acceptStatCode;
            if(site != null) {
                acceptStatCode = site.getAcceptStatCode();
                charset = site.getCharset();
                headers = site.getHeaders();
            } else {
                acceptStatCode = Sets.newHashSet(new Integer[]{Integer.valueOf(200)});
            }

            this.logger.info("downloading page {}", request.getUrl());
            CloseableHttpResponse httpResponse = null;
            int statusCode = 0;

            Page page;
            try {
                try {

                    //获取到403拒绝服务状态码时，轮流暂停线程，并重新发送请求
                    HttpUriRequest e = this.getHttpUriRequest(request, site, headers);
                    httpResponse = this.getHttpClient(site).execute(e);
                    statusCode = httpResponse.getStatusLine().getStatusCode();

                    if(statusCode == 403){
                        for(int i=0;i<5 && statusCode == 403;i++){
                            try {
                                Thread.sleep(30000);
                                httpResponse = this.getHttpClient(site).execute(e);
                                statusCode = httpResponse.getStatusLine().getStatusCode();
                            } catch (InterruptedException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }

                    request.putExtra("statusCode", Integer.valueOf(statusCode));
                    if(this.statusAccept((Set)acceptStatCode, statusCode)) {
                        page = this.handleResponse(request, charset, httpResponse, task);
                        this.onSuccess(request);
                        Page e1 = page;
                        return e1;
                    }

                    this.logger.warn("code error " + statusCode + "\t" + request.getUrl());
                    page = null;
                    return page;
                } catch (IOException var23) {
                    this.logger.warn("download page " + request.getUrl() + " error", var23);
                    if(site.getCycleRetryTimes() <= 0) {
                        this.onError(request);
                        page = null;
                        return page;
                    }
                }

                page = this.addToCycleRetry(request, site);
            } finally {
                request.putExtra("statusCode", Integer.valueOf(statusCode));

                try {
                    if(httpResponse != null) {
                        EntityUtils.consume(httpResponse.getEntity());
                    }
                } catch (IOException var22) {
                    this.logger.warn("close response fail", var22);
                }

            }

            return page;
        }

        public void setThread(int thread) {
            this.httpClientGenerator.setPoolSize(thread);
        }

        protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
            return acceptStatCode.contains(Integer.valueOf(statusCode));
        }

        protected HttpUriRequest getHttpUriRequest(Request request, Site site, Map<String, String> headers) {
            RequestBuilder requestBuilder = this.selectRequestMethod(request).setUri(request.getUrl());
            if(headers != null) {
                Iterator requestConfigBuilder = headers.entrySet().iterator();

                while(requestConfigBuilder.hasNext()) {
                    Map.Entry host = (Map.Entry)requestConfigBuilder.next();
                    requestBuilder.addHeader((String)host.getKey(), (String)host.getValue());
                }
            }

            RequestConfig.Builder requestConfigBuilder1 = RequestConfig.custom().setConnectionRequestTimeout(site.getTimeOut()).setSocketTimeout(site.getTimeOut()).setConnectTimeout(site.getTimeOut()).setCookieSpec("best-match");
            HttpHost host1;
            if(site.getHttpProxyPool() != null && site.getHttpProxyPool().isEnable()) {
                host1 = site.getHttpProxyFromPool();
                requestConfigBuilder1.setProxy(host1);
                request.putExtra("proxy", host1);
            } else if(site.getHttpProxy() != null) {
                host1 = site.getHttpProxy();
                requestConfigBuilder1.setProxy(host1);
                request.putExtra("proxy", host1);
            }

            requestBuilder.setConfig(requestConfigBuilder1.build());
            return requestBuilder.build();
        }

        protected RequestBuilder selectRequestMethod(Request request) {
            String method = request.getMethod();
            if(method != null && !method.equalsIgnoreCase("GET")) {
                if(method.equalsIgnoreCase("POST")) {
                    RequestBuilder requestBuilder = RequestBuilder.post();
                    NameValuePair[] nameValuePair = (NameValuePair[])((NameValuePair[])request.getExtra("nameValuePair"));
                    if(nameValuePair != null && nameValuePair.length > 0) {
                        requestBuilder.addParameters(nameValuePair);
                    }

                    return requestBuilder;
                } else if(method.equalsIgnoreCase("HEAD")) {
                    return RequestBuilder.head();
                } else if(method.equalsIgnoreCase("PUT")) {
                    return RequestBuilder.put();
                } else if(method.equalsIgnoreCase("DELETE")) {
                    return RequestBuilder.delete();
                } else if(method.equalsIgnoreCase("TRACE")) {
                    return RequestBuilder.trace();
                } else {
                    throw new IllegalArgumentException("Illegal HTTP Method " + method);
                }
            } else {
                return RequestBuilder.get();
            }
        }

        protected Page handleResponse(Request request, String charset, HttpResponse httpResponse, Task task) throws IOException {
            String content = this.getContent(charset, httpResponse);
            Page page = new Page();
            page.setRawText(content);
            page.setUrl(new PlainText(request.getUrl()));
            page.setRequest(request);
            page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
            return page;
        }

        protected String getContent(String charset, HttpResponse httpResponse) throws IOException {
            if(charset == null) {
                byte[] contentBytes = IOUtils.toByteArray(httpResponse.getEntity().getContent());
                String htmlCharset = this.getHtmlCharset(httpResponse, contentBytes);
                if(htmlCharset != null) {
                    return new String(contentBytes, htmlCharset);
                } else {
                    this.logger.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
                    return new String(contentBytes);
                }
            } else {
                return IOUtils.toString(httpResponse.getEntity().getContent(), charset);
            }
        }

        protected String getHtmlCharset(HttpResponse httpResponse, byte[] contentBytes) throws IOException {
            String value = httpResponse.getEntity().getContentType().getValue();
            String charset = UrlUtils.getCharset(value);
            if(StringUtils.isNotBlank(charset)) {
                this.logger.debug("Auto get charset: {}", charset);
                return charset;
            } else {
                Charset defaultCharset = Charset.defaultCharset();
                String content = new String(contentBytes, defaultCharset.name());
                if(StringUtils.isNotEmpty(content)) {
                    Document document = Jsoup.parse(content);
                    Elements links = document.select("meta");
                    Iterator i$ = links.iterator();

                    while(i$.hasNext()) {
                        Element link = (Element)i$.next();
                        String metaContent = link.attr("content");
                        String metaCharset = link.attr("charset");
                        if(metaContent.indexOf("charset") != -1) {
                            metaContent = metaContent.substring(metaContent.indexOf("charset"), metaContent.length());
                            charset = metaContent.split("=")[1];
                            break;
                        }

                        if(StringUtils.isNotEmpty(metaCharset)) {
                            charset = metaCharset;
                            break;
                        }
                    }
                }

                this.logger.debug("Auto get charset: {}", charset);
                return charset;
            }
        }
    }



