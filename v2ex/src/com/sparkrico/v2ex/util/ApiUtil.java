package com.sparkrico.v2ex.util;

public class ApiUtil {

	private static final String baseUrl = "http://www.v2ex.com/api/";
	
	//site(UNUSED)
	public static final String site_stats = baseUrl + "site/stats.json";
	public static final String site_info = baseUrl + "site/info.json";
	
	//nodes
	public static final String nodes_all = baseUrl + "nodes/all.json";
	//(UNUSED)
	public static final String nodes_show = baseUrl + "nodes/show.json?id=%s&name=%s";
	
	//topics
	public static final String topics_latest = baseUrl + "topics/latest.json";
	//topic by node
	public static final String topics_show = baseUrl + "topics/show.json?node_name=%s";
	//topic
	public static final String topic_show = baseUrl + "topics/show.json?id=%s";
	//member topic
	public static final String topic_member_show = baseUrl + "topics/show.json?username=%s";
	
	//replies
	public static final String replies_show = baseUrl + "replies/show.json?topic_id=%s";
	
	//members
	public static final String members_show = baseUrl + "members/show.json?username=%s";
	
	//(UNUSED)
	public static final String topics_create = baseUrl + "topics/create.json";
	public static final String login = "http://v2ex.com/signin";
	
}
