package com.ncsu.edu.spinningwellness.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

public class Utils {

	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static Entity findEntity(Key key) {
		try {
			return datastore.get(key);
		} catch (EntityNotFoundException e) {
			return null;
		}
	}

	public static Map sortMapOnValues(Map<String, Double> unsortedMap) {

		List list = new LinkedList(unsortedMap.entrySet());

		// sort list based on comparator
		Collections.sort(list, new Comparator() {
			public int compare(Object o1, Object o2) {
				return ((Comparable) ((Map.Entry) (o1)).getValue())
						.compareTo(((Map.Entry) (o2)).getValue());
			}
		});

		// put sorted list into map again
		//LinkedHashMap make sure order in which keys were inserted
		Map sortedMap = new LinkedHashMap();
		for (Iterator it = list.iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

	public static void main(String args[]) {


		HashMap<String, Double> distanceCoveredByUsers = new HashMap<String, Double>();
		distanceCoveredByUsers.put("amarja", 0d);
		distanceCoveredByUsers.put("test1", -1d);
		distanceCoveredByUsers.put("test3", -1d);

		distanceCoveredByUsers.put("test3", 100d);
		distanceCoveredByUsers.put("test4", 100d);
		distanceCoveredByUsers.put("test5", 100d);
		distanceCoveredByUsers.put("test6", 100d);
		distanceCoveredByUsers.put("test7", 50d);

		Map<String, Double> sortedMap = sortMapOnValues(distanceCoveredByUsers);

		List list = new LinkedList(sortedMap.entrySet());		
		for (int i=list.size()-1; i>=list.size()-3; i--) {
			Map.Entry entry = (Entry) list.get(i);
			System.out.println("Key : " + entry.getKey() 
					+ " Value : " + entry.getValue());
		}
	}

	public static long convertDateToLong(Date date) {

		SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		return Long.parseLong(df.format(date));
	}

	public static Date convertLongToDate(Long date) {
		SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmmss");
		try {
			return df.parse(date.toString());
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
}
