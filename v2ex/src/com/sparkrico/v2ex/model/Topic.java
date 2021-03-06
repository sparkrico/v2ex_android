package com.sparkrico.v2ex.model;

import java.io.Serializable;

public class Topic implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private long id;
	private String title;
	private String url;
	private String content;
	private String content_rendered;
	private int replies;
	private MemberMini member;
	private Node node;
	private long created;
	private long last_modified;
	private long last_touched;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getContent_rendered() {
		return content_rendered;
	}
	public void setContent_rendered(String content_rendered) {
		this.content_rendered = content_rendered;
	}
	public int getReplies() {
		return replies;
	}
	public void setReplies(int replies) {
		this.replies = replies;
	}
	public MemberMini getMember() {
		return member;
	}
	public void setMember(MemberMini member) {
		this.member = member;
	}
	public Node getNode() {
		return node;
	}
	public void setNode(Node node) {
		this.node = node;
	}
	public long getCreated() {
		return created;
	}
	public void setCreated(long created) {
		this.created = created;
	}
	public long getLast_modified() {
		return last_modified;
	}
	public void setLast_modified(long last_modified) {
		this.last_modified = last_modified;
	}
	public long getLast_touched() {
		return last_touched;
	}
	public void setLast_touched(long last_touched) {
		this.last_touched = last_touched;
	}
	
}
