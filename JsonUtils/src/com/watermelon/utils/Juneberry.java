package com.watermelon.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonNull;

public class Juneberry {
	private static CloseableHttpClient httpClient = null;
	private static CookieStore cookieStore = null;
	private static HttpHost proxy = null;
	private static RequestConfig config = null;
	static {
		cookieStore = new BasicCookieStore();
		httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		proxy = new HttpHost("127.0.0.1", 8888, "http");
		// config = RequestConfig.custom().setProxy(proxy).build();
	}

	private static String txt_LoginID = "201467003007";
	private static String txt_Password = "201467003007";
	private static String LoginUrl = "http://zwfp.jxnu.jadl.net/Login.aspx";// 登录url
	private static String __VIEWSTATE = "";
	private static String __VIEWSTATEGENERATOR = "";
	private static String __EVENTVALIDATION = "";
	private static String subCmd = "";
	private static String roomName[] = { "自习室101(南)", "自习室201(南)", "自习室202(北)", "自习室301(南)", "自习室302(北)" };

	public static void main(String[] args) throws Exception {
		// getTestUserAccount();
		// testGetSeat();
		testCancelAndGetSeat();
		// testCancel();

	}

	public static void testCancelAndGetSeat() throws Exception {
		// 登录
		String loginResult = login(txt_LoginID, txt_Password);
		String h1 = null;
		try {
			h1 = jsoupUtils.getHTMLByTagFirst(loginResult, "h2");
		} catch (Exception e) {
			System.out.println(txt_LoginID + "已经修改密码");
			return;
		}
		System.out.println("登陆成功");
		// 取消现在的座位
		String logs = queryLogs();
		String li = null;
		try {
			li = jsoupUtils.getHTMLByTagSeconds(logs, "ul");// 预约记录li

		} catch (Exception e) {
			System.out.println("无预约记录");
			return;
		}
		Document document = Jsoup.parse(li);
		Elements elements = document.getElementsByTag("li");
		li = elements.get(1).outerHtml();

		//System.out.println(li);
		String name = jsoupUtils.getRoomName(li);
		Integer roomId = jsoupUtils.getRoomId(name, roomName);
		//System.out.println(roomId);
		String key = jsoupUtils.getKey(li);// 查询要取消的key
		if (key == null) {
			System.out.println("没有预约位置");
			return;
		}

		String cancelResult = cancel(key);
		// System.out.println(cancelResult);
		// System.out.println("--------------------------------------");
		try {
			String cancelSuccess = jsoupUtils.getCancelSuccess(cancelResult);
			//System.out.println(cancelSuccess);
		} catch (Exception e) {
			System.out.println("取消失败");
		}

		System.out.println("取消成功");

		// 查询现在的座位
		String getSeatResult = getSeat(selReadingRooms[roomId]);
		// System.out.println(getSeatResult);

		document = Jsoup.parse(getSeatResult);// 将html解析成document对象
		elements = document.getElementsByTag("span");
		if (elements.size() >= 1) {
			System.out.println(name + "无位置");
			return;
		}

		System.out.println("有位置");
		// 获取table
		String[] seatsArray = jsoupUtils.getSeat(getSeatResult);
        
		if (seatsArray == null||seatsArray.length==0) {
			System.out.println("位置被抢光啦");
		}
		String seats=jsoupUtils.getSeat(seatsArray);
		System.out.println(seats);
		String bookResult = bookSeat(seats, selReadingRooms[roomId]);
		System.out.println("_______________________");
		//System.out.println(bookResult);
		
		document=Jsoup.parse(bookResult);
		Element ele=document.getElementById("MessageTip");
		if(ele.text().equals("座位预约成功，请在6:00至10:00到图书馆刷卡确认"))
		{
			System.out.println("恭喜你，座位预定成功。");
			System.out.println("座位号："+seats);
		}

	}

	public static void testGetSeat() throws Exception {
		String loginResult = login(txt_LoginID, txt_Password);
		String h1 = null;
		try {
			h1 = jsoupUtils.getHTMLByTagFirst(loginResult, "h2");
		} catch (Exception e) {
			System.out.println(txt_LoginID + "已经修改密码");
			return;
		}
		System.out.println("登陆成功");
		String getSeatResult = getSeat(selReadingRooms[0]);
		System.out.println(getSeatResult);
		String noSeat = jsoupUtils.getHTMLByTagFirst(getSeatResult, "span");
		System.out.println(noSeat);
		if (noSeat != null) {
			System.out.println("今天没位置了");
			return;
		}

	}

	/**
	 * 测试取消功能
	 * 
	 * @throws Exception
	 */
	public static void testCancel() throws Exception {
		String loginResult = login(txt_LoginID, txt_Password);
		String h1 = null;
		try {
			h1 = jsoupUtils.getHTMLByTagFirst(loginResult, "h2");
		} catch (Exception e) {
			System.out.println(txt_LoginID + "已经修改密码");
			return;
		}
		System.out.println("登陆成功");
		String logs = queryLogs();
		String li = null;
		try {
			li = jsoupUtils.getHTMLByTagSeconds(logs, "ul");// 预约记录li
		} catch (Exception e) {
			System.out.println("无预约记录");
			return;
		}

		String key = jsoupUtils.getKey(li);// 查询要取消的key
		if (key == null) {
			System.out.println("没有预约位置");
			return;
		}

		String cancelResult = cancel(key);
		System.out.println(cancelResult);
		try {
			String cancelSuccess = jsoupUtils.getHTMLByTagSeconds(cancelResult, "script");
		} catch (Exception e) {
			System.out.println("取消失败");
		}
		System.out.println("取消成功");

	}

	public static void getTestUserAccount() throws Exception {
		for (int i = 1; i < 70; i++) {
			if (i < 10) {
				txt_LoginID = "20146700300" + i;
				txt_Password = "20146700300" + i;
			} else {
				txt_LoginID = "2014670030" + i;
				txt_Password = "2014670030" + i;
			}

			System.out.println(txt_LoginID);
			String loginResult = login(txt_LoginID, txt_Password);
			String h1;
			try {
				h1 = jsoupUtils.getHTMLByTagFirst(loginResult, "h2");
			} catch (Exception e) {
				System.out.println(txt_LoginID + "已经修改密码");
				continue;
			}

			String main = "Object moved to <a href=\"/MainFunctionPage.aspx\">here</a>.";
			if (!main.equals(h1)) {
				System.out.println("登录失败");
				continue;
				// return;
			}
			System.out.println("登录成功");
			System.out.println("查询记录");
			String logs = queryLogs();
			// System.out.println(logs);
			String li = jsoupUtils.getHTMLByTagSeconds(logs, "ul");// 预约记录li
			String position = null;
			// 获取取消的位置key
			String key = jsoupUtils.getKey(li);
			if (key == null) {
				System.out.println("该用户没有可用位置");
				continue;
			}
			try {
				position = jsoupUtils.getHTMLByTagSeconds(li, "li");
			} catch (Exception e) {
				System.out.println("该用户还没有预约过任何位置");
				continue;
			}
			System.out.println(position);

			System.out.println(key);

		}

		// 取消预约
		// String cancelResult=cancel(key);
		// System.out.println(cancelResult);

	}

	/**
	 * 取消预约，测试通过
	 * 
	 * @param key
	 * @return
	 */
	public static String cancel(String key) {
		__VIEWSTATE = "/wEPDwUKMTY3NjM4MDk3NA9kFgICAw9kFgoCAg8WAh4FY2xhc3MFDXVpLWJ0bi1hY3RpdmVkAgMPFgIfAGVkAgQPFgIfAGVkAgYPEGQPFgUCAQICAgMCBAIFFgUQBRHoh6rkuaDlrqQxMDEo5Y2XKQUGMTAxMDAxZxAFEeiHquS5oOWupDIwMSjljZcpBQYxMDEwMDJnEAUR6Ieq5Lmg5a6kMjAyKOWMlykFBjEwMTAwM2cQBRHoh6rkuaDlrqQzMDEo5Y2XKQUGMTAxMDA0ZxAFEeiHquS5oOWupDMwMijljJcpBQYxMDEwMDVnZGQCBw8WAh4HVmlzaWJsZWhkZGD9eBr/IadjWW0IL4y1WR4FBrSGh5D1gj6lVVxCVu3S";
		__VIEWSTATEGENERATOR = "47429C9F";
		__EVENTVALIDATION = "/wEWBQLR1aHCDQLgu8z3BgKk+56eBwLMgJnOCQLytq6nD9ftacDfHnbBjjMcjqehy+7ugquD4S9TTZp4BnSM6jML";
		chooseDate = "选择日期";
		ddlDate = "7";
		ddlRoom = "-1";
		subCmd = "cancel";
		subBookNo = key;
		ContentLength = "643";
		Referer = "http://zwfp.jxnu.jadl.net/UserInfos/QueryLogs.aspx";

		// String params1 = "__VIEWSTATE=" + __VIEWSTATE +
		// "&__VIEWSTATEGENERATOR=" +
		// __VIEWSTATEGENERATOR
		// + "&__EVENTVALIDATION=" + __EVENTVALIDATION +
		// "&subCmd=&subBookNo=&chooseDate=" + chooseDate
		// + "&ddlDate=" + ddlDate + "&ddlRoom=" + ddlRoom + "";
		// System.out.println(params1);
		// StringBuilder sb = new StringBuilder();
		// sb.append("__VIEWSTATE=");
		// sb.append(__VIEWSTATE);
		// sb.append("&__VIEWSTATEGENERATOR=");
		// sb.append(__VIEWSTATEGENERATOR);
		// sb.append("&__EVENTVALIDATION=");
		// sb.append(__EVENTVALIDATION);
		// sb.append("&subCmd=");
		// sb.append("&subBookNo=");
		// sb.append("&chooseDate=");
		// sb.append(chooseDate);
		// sb.append("&ddlDate=");
		// sb.append(ddlDate);
		// sb.append("&ddlRoom=");
		// sb.append(ddlRoom);
		// sb.append("&");
		// String params = sb.toString();
		Map<String, String> params = new HashMap<>();
		params.put("__VIEWSTATE", __VIEWSTATE);
		params.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		params.put("__EVENTVALIDATION", __EVENTVALIDATION);
		params.put("subCmd", subCmd);
		params.put("subBookNo", subBookNo);
		params.put("chooseDate", chooseDate);
		params.put("ddlDate", ddlDate);
		params.put("ddlRoom", ddlRoom);

		Map<String, String> headers = new HashMap<>();
		headers.put("ContentLength", ContentLength);
		headers.put("Content-Type", ContentType);
		headers.put("Cache-Control", CacheControl);
		headers.put("Origin", Origin);
		headers.put("Referer", Referer);
		String result = sendPost2(params, queryLogsURL, headers);
		return result;
	}

	/**
	 * 登录
	 * 
	 * @param txt_LoginID
	 * @param txt_Password
	 * @return
	 */
	public static String login(String txt_LoginID, String txt_Password) {
		subCmd = "Login";
		__VIEWSTATE = "/wEPDwUKLTI1Nzg1ODIyMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFEGNoa19SZW1QYXNzcHdvcmTiU5zolo6/Gtin2EhtwQjwibMyu11t2YOmrWpFNSXQOw==";
		__VIEWSTATEGENERATOR = "C2EE9ABB";
		__EVENTVALIDATION = "/wEWBALGu8H0CwK1lMLgCgLS9cL8AgKXzJ6eD1PrwC/+tEuQt/W6kERZa2FJGBofrpzrzMbXnOcWuVzp";
		CloseableHttpResponse httpResponse = null;
		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("txt_LoginID", txt_LoginID));
		params.add(new BasicNameValuePair("txt_Password", txt_Password));
		params.add(new BasicNameValuePair("subCmd", subCmd));
		params.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
		params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR));
		params.add(new BasicNameValuePair("__EVENTVALIDATION", __EVENTVALIDATION));
		try {

			HttpPost post = new HttpPost(LoginUrl);
			post.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			httpResponse = httpClient.execute(post);
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static String[] selReadingRooms = { "101001", "101002", "101003", "101004", "101005" };// 教室号
	private static String getSeatUrl = "http://zwfp.jxnu.jadl.net/BookSeat/BookSeatListForm.aspx";// 获取座位号请求
	private static String hidBookDate = "";
	private static String hidRrId = "";
	private static String nextDate = null;// 当前日期的下一天日期
	private static String Accept = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
	private static String AcceptEncoding = "gzip, deflate";
	private static String AcceptLanguage = "zh-CN,zh;q=0.8";
	private static String CacheControl = "max-age=0";
	private static String Connection = "keep-alive";
	private static String ContentLength = "";
	private static String ContentType = "application/x-www-form-urlencoded";
	private static String Host = "zwfp.jxnu.jadl.net";
	private static String Origin = "http://zwfp.jxnu.jadl.net";
	private static String Referer = "";
	private static String UpgradeInsecureRequests = "1";
	private static String UserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36";
	private static String txtBookDate = "";
	private static String selReadingRoom = "";

	public static String getSeat2(String readingRoom) {
		ContentLength = "593";
		Referer = "http://zwfp.jxnu.jadl.net/BookSeat/BookSeatListForm.aspx";
		__VIEWSTATE = "/wEPDwUKMTM5MDYwMzc1OA9kFgICAw9kFgYCBw8QZA8WBWYCAQICAgMCBBYFEAUR6Ieq5Lmg5a6kMTAxKOWNlykFBjEwMTAwMWcQBRHoh6rkuaDlrqQyMDEo5Y2XKQUGMTAxMDAyZxAFEeiHquS5oOWupDIwMijljJcpBQYxMDEwMDNnEAUR6Ieq5Lmg5a6kMzAxKOWNlykFBjEwMTAwNGcQBRHoh6rkuaDlrqQzMDIo5YyXKQUGMTAxMDA1Z2RkAgkPFgQeCWlubmVyaHRtbGUeB1Zpc2libGVoZAILDzwrAAkAZGTx+ukjUIPRsKDM56UDnVvXNZuCLN9TkcbAge3Mz7zTZQ==";
		__VIEWSTATEGENERATOR = "871AA8B3";
		__EVENTVALIDATION = "/wEWBQKt5JrACQKP25+PBwKozKffBwKe8pjrCQLbhOG0Aql5TSJ0nhtEutd/JRVIFbzcy0tbYY4shPyNbOh7kYaY";
		hidBookDate = CommonUtils.getNextDate(new Date());
		hidRrId = readingRoom;
		subCmd = "query";
		txtBookDate = hidBookDate;
		selReadingRoom = readingRoom;

		Map<String, String> headers = new HashMap<>();
		headers.put("CacheControl", CacheControl);
		headers.put("ContentLength", ContentLength);
		headers.put("ContentType", ContentType);
		headers.put("Origin", Origin);
		headers.put("Referer", Referer);

		Map<String, String> params = new HashMap<>();
		params.put("__VIEWSTATE", __VIEWSTATE);
		params.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		params.put("__EVENTVALIDATION", __EVENTVALIDATION);
		params.put("hidBookDate", hidBookDate);
		params.put("hidRrId", hidRrId);
		params.put("subCmd", subCmd);
		params.put("txtBookDate", txtBookDate);
		params.put("selReadingRoom", selReadingRoom);
		return sendPost(params, getSeatUrl, headers);
	}

	/**
	 * 获取某某教室座位
	 * 
	 * @param room
	 * @param cookies
	 */
	public static String getSeat1(String room) {
		subCmd = "query";
		__VIEWSTATE = "/wEPDwUKMTM5MDYwMzc1OA9kFgICAw9kFgYCBw8QZA8WBWYCAQICAgMCBBYFEAUR6Ieq5Lmg5a6kMTAxKOWNlykFBjEwMTAwMWcQBRHoh6rkuaDlrqQyMDEo5Y2XKQUGMTAxMDAyZxAFEeiHquS5oOWupDIwMijljJcpBQYxMDEwMDNnEAUR6Ieq5Lmg5a6kMzAxKOWNlykFBjEwMTAwNGcQBRHoh6rkuaDlrqQzMDIo5YyXKQUGMTAxMDA1Z2RkAgkPFgQeCWlubmVyaHRtbGUeB1Zpc2libGVoZAILDzwrAAkAZGTx+ukjUIPRsKDM56UDnVvXNZuCLN9TkcbAge3Mz7zTZQ==";
		__VIEWSTATEGENERATOR = "871AA8B3";
		__EVENTVALIDATION = "/wEWBQKt5JrACQKP25+PBwKozKffBwKe8pjrCQLbhOG0Aql5TSJ0nhtEutd/JRVIFbzcy0tbYY4shPyNbOh7kYaY";
		ContentLength = "593";
		Referer = "http://zwfp.jxnu.jadl.net/BookSeat/BookSeatListForm.aspx";
		nextDate = CommonUtils.getNextDate(new Date());
		CloseableHttpResponse httpResponse = null;

		List<NameValuePair> params = new ArrayList<>();
		params.add(new BasicNameValuePair("hidBookDate", hidBookDate));
		params.add(new BasicNameValuePair("hidRrId", hidRrId));
		params.add(new BasicNameValuePair("subCmd", subCmd));
		params.add(new BasicNameValuePair("txtBookDate", nextDate));
		params.add(new BasicNameValuePair("selReadingRoom", room));
		params.add(new BasicNameValuePair("__VIEWSTATE", __VIEWSTATE));
		params.add(new BasicNameValuePair("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR));
		params.add(new BasicNameValuePair("__EVENTVALIDATION", __EVENTVALIDATION));

		try {
			HttpPost httpPost = new HttpPost(getSeatUrl);
			httpPost.setConfig(config);
			httpPost = addHeader(httpPost);// 加入默认header
			// String cookie = CommonUtils.getSessionId(cookies);
			// System.out.println(cookie);
			// httpPost.addHeader("Cookie", cookie);
			httpPost.addHeader("CacheControl", CacheControl);
			httpPost.addHeader("ContentLength", ContentLength);
			httpPost.addHeader("ContentType", ContentType);
			httpPost.addHeader("Origin", Origin);
			httpPost.addHeader("Referer", Referer);
			httpPost.setEntity(new UrlEncodedFormEntity(params, "utf-8"));
			httpResponse = httpClient.execute(httpPost);
			String result = EntityUtils.toString(httpResponse.getEntity());
			System.out.println(result);
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static String getSeat(String room) {
		ContentLength = "593";
		Referer = "http://zwfp.jxnu.jadl.net/BookSeat/BookSeatListForm.aspx";
		__VIEWSTATE = "/wEPDwUKMTM5MDYwMzc1OA9kFgICAw9kFgYCBw8QZA8WBWYCAQICAgMCBBYFEAUR6Ieq5Lmg5a6kMTAxKOWNlykFBjEwMTAwMWcQBRHoh6rkuaDlrqQyMDEo5Y2XKQUGMTAxMDAyZxAFEeiHquS5oOWupDIwMijljJcpBQYxMDEwMDNnEAUR6Ieq5Lmg5a6kMzAxKOWNlykFBjEwMTAwNGcQBRHoh6rkuaDlrqQzMDIo5YyXKQUGMTAxMDA1Z2RkAgkPFgQeCWlubmVyaHRtbGUeB1Zpc2libGVoZAILDzwrAAkAZGTx+ukjUIPRsKDM56UDnVvXNZuCLN9TkcbAge3Mz7zTZQ==";
		__VIEWSTATEGENERATOR = "871AA8B3";
		__EVENTVALIDATION = "/wEWBQKt5JrACQKP25+PBwKozKffBwKe8pjrCQLbhOG0Aql5TSJ0nhtEutd/JRVIFbzcy0tbYY4shPyNbOh7kYaY";
		hidBookDate = null;
		hidRrId = null;
		subCmd = "query";
		txtBookDate = CommonUtils.getNextDate(new Date());
		selReadingRoom = room;

		Map<String, String> headers = new HashMap<>();
		headers.put("CacheControl", CacheControl);
		headers.put("ContentLength", ContentLength);
		headers.put("ContentType", ContentType);
		headers.put("Origin", Origin);
		headers.put("Referer", Referer);

		Map<String, String> params = new HashMap<>();
		params.put("__VIEWSTATE", __VIEWSTATE);
		params.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		params.put("__EVENTVALIDATION", __EVENTVALIDATION);
		params.put("hidBookDate", hidBookDate);
		params.put("hidRrId", hidRrId);
		params.put("subCmd", subCmd);
		params.put("txtBookDate", txtBookDate);
		params.put("selReadingRoom", selReadingRoom);
		return sendPost(params, getSeatUrl, headers);

	}

	public static String sendGet(String url) {

		HttpResponse httpResponse = null;
		try {
			HttpGet httpGet = new HttpGet(url);
			httpResponse = httpClient.execute(httpGet);
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 发送post请求
	 * 
	 * @param queryParams
	 *            查询参数
	 * @param url
	 *            请求URL
	 * @return
	 */
	public static String sendPost2(Map<String, String> params, String url, Map<String, String> headers) {

		CloseableHttpResponse httpResponse = null;

		HttpPost httpPost = new HttpPost(url);

		httpPost.setConfig(config);

		httpPost = addHeader(httpPost);// 加入默认Header

		// 加入header
		for (Entry<String, String> entry : headers.entrySet()) {
			httpPost.addHeader(entry.getKey(), entry.getValue());
		}
		httpPost = setQueryParams(params, httpPost);

		try {
			httpResponse = httpClient.execute(httpPost);
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 发送post请求
	 * 
	 * @param queryParams
	 *            查询参数
	 * @param url
	 *            请求URL
	 * @return
	 */
	public static String sendPost(Map<String, String> params, String url, Map<String, String> headers) {

		CloseableHttpResponse httpResponse = null;

		HttpPost httpPost = new HttpPost(url);

		httpPost.setConfig(config);

		httpPost = addHeader(httpPost);// 加入默认Header
		if (headers != null) {
			// 加入header
			for (Entry<String, String> entry : headers.entrySet()) {
				httpPost.addHeader(entry.getKey(), entry.getValue());
			}
		}

		httpPost = setQueryParams(params, httpPost);

		try {
			httpResponse = httpClient.execute(httpPost);
			String result = EntityUtils.toString(httpResponse.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String subBookNo = "";
	private static String chooseDate = "";
	private static String ddlDate = "";
	private static String ddlRoom = "";
	private static String queryLogsURL = "http://zwfp.jxnu.jadl.net/UserInfos/QueryLogs.aspx";

	/**
	 * 查询预约记录
	 * 
	 * @return
	 */
	public static String queryLogs() {
		__VIEWSTATE = "/wEPDwUKMTY3NjM4MDk3NA9kFgICAw9kFgQCBg8QZA8WBQIBAgICAwIEAgUWBRAFEeiHquS5oOWupDEwMSjljZcpBQYxMDEwMDFnEAUR6Ieq5Lmg5a6kMjAxKOWNlykFBjEwMTAwMmcQBRHoh6rkuaDlrqQyMDIo5YyXKQUGMTAxMDAzZxAFEeiHquS5oOWupDMwMSjljZcpBQYxMDEwMDRnEAUR6Ieq5Lmg5a6kMzAyKOWMlykFBjEwMTAwNWdkZAIHDxYCHgdWaXNpYmxlaGRkuNNJ88meOHQR4U6H0pQ0xWrJGebU5BBBHiKMNXjnVaI=";
		__VIEWSTATEGENERATOR = "47429C9F";
		__EVENTVALIDATION = "/wEWBQLgoa2UCALgu8z3BgKk%2B56eBwLMgJnOCQLytq6nD0azTZjETU9Qq%2BN3rZIc46kabQ/aa7nEe0FXajQMeYny";
		chooseDate = "选择日期";
		ddlDate = "7";
		ddlRoom = "-1";
		subCmd = "";
		ContentLength = "631";
		Referer = "http://zwfp.jxnu.jadl.net/UserInfos/QueryLogs.aspx";

		// String params1 = "__VIEWSTATE=" + __VIEWSTATE +
		// "&__VIEWSTATEGENERATOR=" +
		// __VIEWSTATEGENERATOR
		// + "&__EVENTVALIDATION=" + __EVENTVALIDATION +
		// "&subCmd=&subBookNo=&chooseDate=" + chooseDate
		// + "&ddlDate=" + ddlDate + "&ddlRoom=" + ddlRoom + "";
		// System.out.println(params1);
		// StringBuilder sb = new StringBuilder();
		// sb.append("__VIEWSTATE=");
		// sb.append(__VIEWSTATE);
		// sb.append("&__VIEWSTATEGENERATOR=");
		// sb.append(__VIEWSTATEGENERATOR);
		// sb.append("&__EVENTVALIDATION=");
		// sb.append(__EVENTVALIDATION);
		// sb.append("&subCmd=");
		// sb.append("&subBookNo=");
		// sb.append("&chooseDate=");
		// sb.append(chooseDate);
		// sb.append("&ddlDate=");
		// sb.append(ddlDate);
		// sb.append("&ddlRoom=");
		// sb.append(ddlRoom);
		// sb.append("&");
		// String params = sb.toString();
		Map<String, String> params = new HashMap<>();
		params.put("__VIEWSTATE", __VIEWSTATE);
		params.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		params.put("__EVENTVALIDATION", __EVENTVALIDATION);
		params.put("subCmd", subCmd);
		params.put("subBookNo", subBookNo);
		params.put("chooseDate", chooseDate);
		params.put("ddlDate", ddlDate);
		params.put("ddlRoom", ddlRoom);

		Map<String, String> headers = new HashMap<>();
		headers.put("ContentLength", ContentLength);
		headers.put("Content-Type", ContentType);
		headers.put("Cache-Control", CacheControl);
		headers.put("Origin", Origin);
		headers.put("Referer", Referer);
		String result = sendPost(params, queryLogsURL, headers);
		return result;
	}

	/**
	 * 设置post的查询参数
	 * 
	 * @param params
	 * @param post
	 * @return
	 */
	public static HttpPost setQueryParams2(Map<String, String> params, HttpPost post) {
		StringBuilder sb = new StringBuilder();
		for (Entry entry : params.entrySet()) {
			// System.out.println(entry.getKey());
			sb.append(entry.getKey());
			// System.out.println("=");
			sb.append("=");
			// System.out.println(entry.getValue());
			sb.append(entry.getValue());
			sb.append("&");
		}
		String str = sb.toString();
		System.out.println(str);
		try {
			post.setEntity(new StringEntity(str, "utf-8"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return post;
	}

	/**
	 * 设置post的查询参数
	 * 
	 * @param params
	 * @param post
	 * @return
	 */
	public static HttpPost setQueryParams(Map<String, String> params, HttpPost post) {
		List<NameValuePair> params1 = new ArrayList<>();
		for (Entry entry : params.entrySet()) {
			String name = (String) entry.getKey();
			String value = (String) entry.getValue();
			params1.add(new BasicNameValuePair(name, value));
		}
		try {
			post.setEntity(new UrlEncodedFormEntity(params1, "utf-8"));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return post;
	}

	public static HttpPost addHeader(HttpPost httpPost) {
		httpPost.addHeader("Accept", Accept);
		httpPost.addHeader("Accept-Encoding", AcceptEncoding);
		httpPost.addHeader("Accept-Language", AcceptLanguage);
		httpPost.addHeader("Connection", Connection);
		httpPost.addHeader("Host", Host);

		httpPost.addHeader("Upgrade-Insecure-Requests", UpgradeInsecureRequests);
		httpPost.addHeader("User-Agent", UserAgent);
		return httpPost;
	}

	/**
	 * 预定座位,测试通过
	 * 
	 * @param seatShortNo
	 *            A18
	 * @param roomNo
	 *            101001
	 * @return
	 */
	public static String bookSeat(String seatShortNo, String roomNo) {
		// QueryData
		String seatNo =  roomNo+seatShortNo;
		String date = CommonUtils.getNextDate(new Date());
		String timeSpan = "";

		// FormData
		__VIEWSTATE = "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";
		__VIEWSTATEGENERATOR = "7629D439";
		__EVENTVALIDATION = "/wEWAwL55vP0CgL0ntKNAwLCi9reA0yaRpkdgpS8+9pWCS+9JOUpucy4wjhzcv9qx7/TWLH3";
		subCmd = "query";
		String spanSelect = "7:00";

		// String
		// URL="http://zwfp.jxnu.jadl.net/BookSeat/BookSeatMessage.aspx?seatNo=101001A18&seatShortNo=A18&roomNo=101001&date=2017-9-8&timeSpan=";
		      //http://zwfp.jxnu.jadl.net/BookSeat/BookSeatMessage.aspx?seatNo=A18 L05101001&seatShortNo=A18 L05&roomNo=101001&date=2017-9-8&timeSpan=
		StringBuilder sb = new StringBuilder();
		sb.append("http://zwfp.jxnu.jadl.net/BookSeat/BookSeatMessage.aspx?");
		sb.append("seatNo=");
		sb.append(seatNo);
		sb.append("&seatShortNo=");
		sb.append(seatShortNo);
		sb.append("&roomNo=");
		sb.append(roomNo);
		sb.append("&date=");
		sb.append(date);
		sb.append("&timeSpan=");

		String URL = sb.toString();
       // System.out.println(URL);
		Map<String, String> params = new HashMap<>();
		params.put("__VIEWSTATE", __VIEWSTATE);
		params.put("__VIEWSTATEGENERATOR", __VIEWSTATEGENERATOR);
		params.put("__EVENTVALIDATION", __EVENTVALIDATION);
		params.put("subCmd", subCmd);
		params.put("spanSelect", spanSelect);
		return sendPost(params, URL, null);

	}

}
