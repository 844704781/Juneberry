package com.watermelon.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.cookie.Cookie;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CommonUtils {

	public static String getNextDate(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date nextDate = calendar.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
		return sdf.format(nextDate);
	}

	public static String getSessionId(List<Cookie> cookies) {
		StringBuffer sb = new StringBuffer();

		for (Cookie cookie : cookies) {
			// System.out.print(cookie.getName() + ":");
			// System.out.println(cookie.getValue());

			if (cookie.getName().equals("ASP.NET_SessionId")) {
				sb.append(cookie.getName());
				sb.append("=");
				sb.append(cookie.getValue());
			}
		}

		return sb.toString();
	}

	public static String toJson(Object obj) {
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
		return gson.toJson(obj);
	}

}
