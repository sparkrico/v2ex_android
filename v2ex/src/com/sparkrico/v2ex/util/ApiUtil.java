package com.sparkrico.v2ex.util;

public class ApiUtil {

	private static final String baseUrl = "http://www.v2ex.com/api/";
	
	//site
	public static final String site_stats = baseUrl + "site/stats.json";
	public static final String site_info = baseUrl + "site/info.json";
	
	//nodes
	public static final String nodes_all = baseUrl + "nodes/all.json";
	public static final String nodes_show = baseUrl + "nodes/show.json?id=%s&name=%s";
	
	//topics
	public static final String topics_latest = baseUrl + "topics/latest.json";
	public static final String topics_show = baseUrl + "topics/show.json?id=%s&username=%s&node_id=%s&node_name=%s";
	public static final String topics_create = baseUrl + "topics/create.json";
	
	//replies
	public static final String replies_show = baseUrl + "replies/show.json?topic_id=%s&page=%s";
	
	//members
	public static final String members_show = baseUrl + "members/show.json?username=%s";
	
}
