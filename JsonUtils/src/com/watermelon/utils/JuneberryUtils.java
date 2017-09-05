package com.watermelon.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class JuneberryUtils {

	private static String txt_LoginID = "201467003057";
	private static String txt_Password = "201467003057";
	private static String LoginUrl = "http://zwfp.jxnu.jadl.net/Login.aspx";
	private static String __VIEWSTATE = "/wEPDwUKLTI1Nzg1ODIyMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFEGNoa19SZW1QYXNzcHdvcmTiU5zolo6/Gtin2EhtwQjwibMyu11t2YOmrWpFNSXQOw==";
	private static String __VIEWSTATEGENERATOR = "C2EE9ABB";
	private static String __EVENTVALIDATION = "/wEWBALGu8H0CwK1lMLgCgLS9cL8AgKXzJ6eD1PrwC/+tEuQt/W6kERZa2FJGBofrpzrzMbXnOcWuVzp";
	private static String subCmd = "Login";

	public static void main(String[] args) throws Exception {

		List<Cookie> cookies = login(txt_LoginID, txt_Password);

		for (Cookie cookie : cookies) {
			System.out.print(cookie.getName() + ":");
			System.out.println(cookie.getValue());
		}

	}
	

	public static List<Cookie> login(String txt_LoginID, String txt_Password) {
		CookieStore cookieStore = null;
		CloseableHttpClient httpClient = null;
		CloseableHttpResponse httpResponse = null;
		List<NameValuePair> params = new ArrayList<>();

		params.add(new BasicNameValuePair("txt_LoginID", txt_LoginID));
		params.add(new BasicNameValuePair("txt_Password", txt_Password));
		params.add(new BasicNameValuePair("subCmd", subCmd));
		params.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
		params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR));
		params.add(new BasicNameValuePair("__EVENTVALIDATION", __EVENTVALIDATION));
		try {
			cookieStore = new BasicCookieStore();
			httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
			HttpPost post = new HttpPost(LoginUrl);
			post.setEntity(new UrlEncodedFormEntity(params));
			httpResponse = httpClient.execute(post);
			String result = EntityUtils.toString(httpResponse.getEntity());
			System.out.println(result);
			return cookieStore.getCookies();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
