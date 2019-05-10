package com.rj.soft.global;

/**
 * Cookie
 * 
 * @author cjy
 * @since 2018年2月5日
 * @version 1.0
 * 
 */
public class Cookie {

	/**
	 * 是否记住用户名(1为rem, 0为不rem, 默认为0)
	 */
	private String remUser;

	/**
	 * 是否记住密码(1为rem, 0为不rem, 默认为0)
	 */
	private String remPass;

	private String userName;

	private String passWord;

	public String getRemUser() {
		return remUser;
	}

	public void setRemUser(String remUser) {
		this.remUser = remUser;
	}

	public String getRemPass() {
		return remPass;
	}

	public void setRemPass(String remPass) {
		this.remPass = remPass;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

}
