package com.watermelon.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class jsoupUtils {
	/**
	 * 根据url获取html
	 * 
	 * @param url
	 * @return
	 */
	public static String getHTML(String url, String character) {
		try {
			URL obj = new URL(url);
			InputStream inStream = obj.openStream();
			String html = toString(inStream, character);
			return html;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 根据流获取流里的字符串
	 * 
	 * @param inStream
	 * @return
	 */
	public static String toString(InputStream inStream, String character) {
		try {
			Reader reader = new InputStreamReader(inStream, character);
			BufferedReader bufferedReader = new BufferedReader(reader);
			String line;
			StringBuilder sb = new StringBuilder();
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	public static String getATagsTextByClass(String html, String className) {
		Document document = Jsoup.parse(html);// 将html解析成document对象
		Elements elements = document.getElementsByClass(className);// 找到newsContent所在class的所有html内容
		Elements aElements = new Elements();
		StringBuilder sb = new StringBuilder();

		for (Element element : elements) {
			for (Element temp : element.getElementsByTag("a"))// 获得这个元素下的所有a标签，将每个a标签都重新放到一个element中
			{
				aElements.add(temp);
			}
		}
		for (Element a : aElements)// 遍历每个a标签，获取a标签下的text
		{
			sb.append(a.text());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static String getHTMLByTagFirst(String html, String className) throws Exception {

		Document document = Jsoup.parse(html);// 将html解析成document对象
		Elements elements = document.getElementsByTag(className);
		return elements.get(0).html();

	}

	public static Elements getHTMLByTag(String html, String className) throws Exception {

		Document document = Jsoup.parse(html);// 将html解析成document对象
		Elements elements = document.getElementsByTag(className);
		return elements;
	}

	public static String getHTMLByTagSeconds(String html, String className) throws Exception {
		Document document = Jsoup.parse(html);// 将html解析成document对象
		Elements elements = document.getElementsByTag(className);
		if (elements == null) {
			return null;
		}
		return elements.get(1).html();
	}

	public static String getCancelSuccess(String html) {
		Document document = Jsoup.parse(html);// 将html解析成document对象
		Elements elements = document.getElementsByTag("script");
		return elements.get(3).html();
	}

	public static String getKey(String html) throws Exception {
		Elements elements = getHTMLByTag(html, "li");
		for (int i = 0; i < elements.size(); i++) {
			if ("预约状态：等待确认".equals(elements.get(i).html())) {
				String input = elements.get(i + 1).html();
				String strs[] = input.split("'");
				return strs[1];
			}
		}
		return null;
	}

	/**
	 * <li data-theme="d" data-role="list-divider" role="heading">预约记录</li>
	 * <li date-theme="d">自习室101(南)：A18
	 * <ul date-theme="d">
	 * <li>预约时间：2017-9-8 7:00:00</li>
	 * <li>提交时间：2017-9-7 0:18:17</li>
	 * <li>取消时间：1900-1-1 0:00:00</li>
	 * <li>预约状态：等待确认</li>
	 * <li><input data-inline="true" data-mini="false" value="取消" type="button"
	 * onclick="subCancel('291053')"></li>
	 * </ul>
	 * </li>
	 * 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String getSeatResult = "<table id=\"DataListBookSeat\" cellspacing=\"0\" class=\"dlClass\" border=\"0\" style=\"width:100%;border-collapse:collapse;\">\n"
				+ "	<tr>\n" + "		<td>\n"
				+ "                    <div id=\"DataListBookSeat_ctl00_divItem\" data-role=\"fieldcontain\" style=\"width: 25%; height: 30px;\n"
				+ "                        margin-left: 0; margin-top: 0\">\n"
				+ "                        <div onclick='ShowBookMessage(&quot;101001A18&quot;,&quot;A18&quot;,&quot;&quot;)'\n"
				+ "                            data-inline=\"true\" data-transition=\"none\" data-role=\"button\" style=\"width: 69px;\n"
				+ "                            text-align: center; margin-left: 0; margin-top: 0\">\n"
				+ "                            A18\n" + "                        </div>\n"
				+ "                    </div>\n" + "                </td><td></td><td></td><td></td>\n" + "	</tr>\n"
				+ "</table>";
		
		Document document=Jsoup.parse(getSeatResult);
        Element tables=document.getElementById("DataListBookSeat");
        System.out.print(tables.text()+"|");
	}
	
	/**
	 * 获得所有的位置
	 * @param table
	 * @return
	 */
	public static String[] getSeat(String table)
	{
		Document document=Jsoup.parse(table);
        Element tables=document.getElementById("DataListBookSeat");
        return tables.text().trim().split("\\s");
	}
	/**
	 * 从位置数组中随机获得一个位置
	 * @param seats
	 * @return
	 */
	public static String getSeat(String []seats)
	{
		Random random=new Random();
		int randomInt=random.nextInt(seats.length);
		return seats[randomInt];
	}

	public static String getRoomName(String html) {
		Document document = Jsoup.parse(html);
		Elements elements = document.getElementsByTag("li");
		String num1[] = null;
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i).hasAttr("date-theme"))
				;
			{
				num1 = elements.get(i).text().split("\\s");
				break;

			}
		}
		return num1[0].split("：")[0];
	}

	public static Integer getRoomId(String roomName, String roomNames[]) {
		for (int i = 0; i < roomNames.length; i++) {
			if (roomName.equals(roomNames[i])) {
				return i;
			}
		}
		return null;
	}
}
