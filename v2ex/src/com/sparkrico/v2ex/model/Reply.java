package com.sparkrico.v2ex.model;

public class Reply {

	private long id;
	private int thanks;
	private String content;
	private String content_rendered;
	private MemberMini member;
	private long created;
	private long last_modified;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getThanks() {
		return thanks;
	}
	public void setThanks(int thanks) {
		this.thanks = thanks;
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
	public MemberMini getMember() {
		return member;
	}
	public void setMember(MemberMini member) {
		this.member = member;
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
}
