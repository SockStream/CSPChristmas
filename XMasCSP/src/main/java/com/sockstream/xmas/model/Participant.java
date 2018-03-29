package com.sockstream.xmas.model;

import java.util.ArrayList;
import java.util.List;

import org.chocosolver.solver.variables.IntVar;

public class Participant {
	private String mNom;
	private String mPrenom;
	private String mMail;
	private Participant mMoitie;
	private List<Participant> mPreviousMates;
	private IntVar mId;
	private IntVar mBuddyId;
	
	private static int compteur = 0;
	
	public Participant(String nom, String prenom, String mail)
	{
		mNom = nom;
		mPrenom = prenom;
		mMail = mail;
		mMoitie = null;
		mPreviousMates = new ArrayList<Participant>();
		mId = XMasModel.getInstance().getModel().intVar(compteur);
		compteur ++;
	}

	public Participant(int Id, String nom, String prenom, String mail) {
		mNom = nom;
		mPrenom = prenom;
		mMail = mail;
		mMoitie = null;
		mPreviousMates = new ArrayList<Participant>();
		mId = XMasModel.getInstance().getModel().intVar(Id);
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
	public Participant getMoitie() {
		return mMoitie;
	}

	/**
	 * @param moitie the moitie to set
	 */
	public void setMoitie(Participant moitie) {
		this.mMoitie = moitie;
	}

	/**
	 * @return the previousMates
	 */
	public List<Participant> getPreviousMates() {
		return mPreviousMates;
	}

	/**
	 * @param previousMates the previousMates to set
	 */
	public void setPreviousMates(List<Participant> previousMates) {
		this.mPreviousMates = previousMates;
	}

	/**
	 * @return the id
	 */
	public IntVar getId() {
		return mId;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(IntVar id) {
		mId = id;
	}

	/**
	 * @return the mBuddy
	 */
	public IntVar getBuddy() {
		return mBuddyId;
	}

	/**
	 * @param mBuddy the mBuddy to set
	 */
	public void setBuddy(IntVar mBuddyId) {
		this.mBuddyId = mBuddyId;
	}
}
