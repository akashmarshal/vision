package com.vision.vb;

import java.util.ArrayList;

public class MenuVb extends CommonVb {

	private static final long serialVersionUID = -2010835009684844752L;
	private String menuProgram = "";
	private String menuName = "";
	private int menuSequence = 0;
	private int parentSequence = 0;
	private String separator = "-1";
	private int menuGroupNt = 0;
	private int menuGroup = -1;
	private int menuStatusNt = 0;
	private int menuStatus = -1;
	private ArrayList<MenuVb> children = null;
	
	public String getMenuProgram() {
		return menuProgram;
	}
	public void setMenuProgram(String menuProgram) {
		this.menuProgram = menuProgram;
	}
	public String getMenuName() {
		return menuName;
	}
	public void setMenuName(String menuName) {
		this.menuName = menuName;
	}
	public int getMenuSequence() {
		return menuSequence;
	}
	public void setMenuSequence(int menuSequence) {
		this.menuSequence = menuSequence;
	}
	public int getParentSequence() {
		return parentSequence;
	}
	public void setParentSequence(int parentSequence) {
		this.parentSequence = parentSequence;
	}
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public int getMenuGroupNt() {
		return menuGroupNt;
	}
	public void setMenuGroupNt(int menuGroupNt) {
		this.menuGroupNt = menuGroupNt;
	}
	public int getMenuGroup() {
		return menuGroup;
	}
	public void setMenuGroup(int menuGroup) {
		this.menuGroup = menuGroup;
	}
	public int getMenuStatusNt() {
		return menuStatusNt;
	}
	public void setMenuStatusNt(int menuStatusNt) {
		this.menuStatusNt = menuStatusNt;
	}
	public int getMenuStatus() {
		return menuStatus;
	}
	public void setMenuStatus(int menuStatus) {
		this.menuStatus = menuStatus;
		
	}
	public ArrayList<MenuVb> getChildren() {
		return children;
	}
	public void setChildren(ArrayList<MenuVb> children) {
		this.children = children;
	}
}
