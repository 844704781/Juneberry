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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class jsoupUtils {
	/**
	 * 根据url获取html
	 * @param url
	 * @return
	 */
	public static String getHTML(String url,String character) {
		try {
			URL obj = new URL(url);
			InputStream inStream=obj.openStream();
			String html=toString(inStream,character);
			return html;
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	/**
	 * 根据流获取流里的字符串
	 * @param inStream
	 * @return
	 */
	public static String toString(InputStream inStream,String character)
	{
		try {
			Reader reader=new InputStreamReader(inStream,character);
			BufferedReader bufferedReader=new BufferedReader(reader);
			String line;
			StringBuilder sb=new StringBuilder();
			while((line=bufferedReader.readLine())!=null)
			{
				sb.append(line);
			}
			return sb.toString();
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public static String getATagsTextByClass(String html,String className)
	{
		    Document document=Jsoup.parse(html);//将html解析成document对象
		    Elements elements=document.getElementsByClass(className);//找到newsContent所在class的所有html内容
		    Elements aElements = new Elements();
		    StringBuilder sb=new StringBuilder();
		    
		    for(Element element:elements)
		    {
		    	for(Element temp:element.getElementsByTag("a"))//获得这个元素下的所有a标签，将每个a标签都重新放到一个element中
		    	{
		    		aElements.add(temp);
		    	}
		    }
		    for(Element a:aElements)//遍历每个a标签，获取a标签下的text
		    {
		    	sb.append(a.text());
		    	sb.append("\n");
		    }
		   return sb.toString();
	}
	
	public static void main(String[] args) {
		String html=getHTML("http://www.qq.com/", "gbk");
	    System.out.println(getATagsTextByClass(html,"newsContent"));
	}
}
