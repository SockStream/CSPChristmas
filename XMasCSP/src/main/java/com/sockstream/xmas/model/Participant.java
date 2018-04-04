package com.sockstream.xmas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Participant implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 647126051541892434L;
	private String mNom;
	private String mPrenom;
	private String mMail;
	private Integer mMoitie;
	private List<Integer> mPreviousMates;
	private int mId;
	private boolean isPresent;
	
	private static int compteur = 0;
	
	public Participant(String nom, String prenom, String mail)
	{
		mNom = nom;
		mPrenom = prenom;
		mMail = mail;
		mMoitie = null;
		mPreviousMates = new ArrayList<Integer>();
		mId = compteur;
		setPresent(true);
		compteur ++;
	}

	public Participant(int id, String nom, String prenom, String mail) {
		mNom = nom;
		mPrenom = prenom;
		mMail = mail;
		mMoitie = null;
		mPreviousMates = new ArrayList<Integer>();
		mId = id;
		setPresent(true);
	}

	/**
	 * @return the nom
	 */
	public String getNom() {
		return mNom;
	}

	/**
	 * @param nom the nom to set
	 */
	public void setNom(String nom) {
		this.mNom = nom;
	}

	/**
	 * @return the prenom
	 */
	public String getPrenom() {
		return mPrenom;
	}

	/**
	 * @param prenom the prenom to set
	 */
	public void setPrenom(String prenom) {
		this.mPrenom = prenom;
	}

	/**
	 * @return the mail
	 */
	public String getMail() {
		return mMail;
	}

	/**
	 * @param mail the mail to set
	 */
	public void setMail(String mail) {
		this.mMail = mail;
	}

	/**
	 * @return the moitie
	 */
	public Integer getMoitie() {
		return mMoitie;
	}

	/**
	 * @param moitie the moitie to set
	 */
	public void setMoitie(Integer moitie) {
		this.mMoitie = moitie;
	}

	/**
	 * @return the previousMates
	 */
	public List<Integer> getPreviousMates() {
		return mPreviousMates;
	}

	/**
	 * @param previousMates the previousMates to set
	 */
	public void setPreviousMates(List<Integer> previousMates) {
		this.mPreviousMates = previousMates;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		mId = id;
	}

	public boolean isPresent() {
		return isPresent;
	}

	public void setPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
}
