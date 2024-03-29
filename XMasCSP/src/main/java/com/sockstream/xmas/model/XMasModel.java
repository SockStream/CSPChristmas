package com.sockstream.xmas.model;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;

import com.google.api.services.gmail.Gmail;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.sockstream.xmas.mailing.MailManager;

public class XMasModel {
	private static XMasModel mINSTANCE;
	private static Logger mLOGGER = LogManager.getLogger(XMasModel.class);
	
	private MailManager mMailManager;
	private boolean mTest = false;
	private String mInputFilePath;
	private Model mModel;
	private List<Participant> mParticipantList;
	private List<Participant> mSavedList;
	private boolean mMailTest;
	private Solution mSolution;
	private List<Solution> mSolutionList;
	private IntVar[] varArray;

	private Random rand = null;
	
	private XMasModel() throws NoSuchAlgorithmException
	{
		mMailManager = new MailManager();
		mParticipantList = new ArrayList<Participant>();
		mSavedList = new ArrayList<Participant>();

		rand = SecureRandom.getInstanceStrong();
	}
	
	public static XMasModel getInstance() throws NoSuchAlgorithmException
	{
		if (mINSTANCE == null)
		{
			mINSTANCE = new XMasModel();
		}
		return mINSTANCE;
	}

	public void loadOptions(String[] args) {
		Options options = new Options();
		
		Option test = new Option("t", "test", false, "test and display result");
		options.addOption(test);
		
		Option mail = new Option("m", "mailTest", false, "send mail to Participants");
		options.addOption(mail);
		
		Option file = new Option("i", "inputFile", true, "path of input file containing configuration");
		file.setRequired(true);
		options.addOption(file);
		
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			mLOGGER.error(e);
			formatter.printHelp("utility-name", options);
			
				System.exit(1);
			return;
		}
		mTest = cmd.hasOption("test");
		mTest = true;
		mMailTest = cmd.hasOption("mailTest");
		mInputFilePath = cmd.getOptionValue("inputFile");
	}

	public void initialize() throws IOException, MessagingException {
		MailManager.getGMailService();
		mModel  = new Model("XMas Solver");
		int[] possibleValues = new int[mParticipantList.size()];
		int index = 0;
		for (Participant personne : mParticipantList)
		{
			possibleValues[index] = personne.getId();
			index ++;
		}
		varArray = mModel.intVarArray(possibleValues.length, possibleValues);
		
		for (Participant personne : mParticipantList)
		{
			
			mModel.arithm(varArray[mParticipantList.indexOf(personne)],"!=",personne.getId()).post();
			if (personne.getMoitie() != null)
			{
				mModel.arithm(varArray[mParticipantList.indexOf(personne)],"!=",personne.getMoitie()).post();
			}
			
			for (int previous : personne.getPreviousMates())
			{
				mModel.arithm(varArray[mParticipantList.indexOf(personne)], "!=", previous).post();
			}
		}
		mModel.allDifferent(varArray).post();
	}

	public void loadFromInputFile() {
		FileReader fileReader = null;
		try {
			fileReader = new FileReader(mInputFilePath);
			GsonBuilder builder = new GsonBuilder();
			Type listType = new TypeToken<ArrayList<Participant>>(){}.getType();
			Gson gson = builder.create();
			mSavedList = gson.fromJson(fileReader,listType);
		} catch (FileNotFoundException e) {
			mLOGGER.error(e);
			System.exit(2);
		}
		finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					mLOGGER.error(e);
				}
			}
		}
		for (Participant personne : mSavedList)
		{
			if (personne.isPresent()) {
				mParticipantList.add(personne);
			}
		}
	}

	public Model getModel() {
		return mModel; 
	}

	public void solve() throws ExecutionException, IOException, MessagingException {
		List<Solution> solutionList = mModel.getSolver().findAllSolutions();
		mLOGGER.info(solutionList.size() + " solutions trouvées");
		while (solutionList.isEmpty())
		{
			removeOlderMates();
			initialize();
			solutionList = mModel.getSolver().findAllSolutions();
			mLOGGER.info(solutionList.size() + " solutions trouvées");
		}
		int randomNum = rand.nextInt(solutionList.size());
		mSolutionList = solutionList;
		mSolution = solutionList.get(randomNum);
	}
	
	private void removeOlderMates() throws ExecutionException {
		int max = 0;
		for (Participant personne : mParticipantList)
		{
			max = Math.max(max,personne.getPreviousMates().size());
		}
		
		if (max == 0) {
			throw new ExecutionException("Aucune solution possible au problème donné",null);
		}
		
		for (Participant personne : mParticipantList)
		{
			if (personne.getPreviousMates().size() == max)
			{
				personne.getPreviousMates().remove(max -1);
			}
		}
	}

	public boolean isTesting() {
		return mTest;
	}

	public void printSolutions() {
		for (Solution soluces : mSolutionList)
		{
			for (Participant personne : mParticipantList)
			{
				Participant match = null;
				for (Participant probableMatch : mParticipantList)
				{
					if (probableMatch.getId() == soluces.getIntVal(varArray[mParticipantList.indexOf(personne)])) // .getIntVal(personne.getBuddy()))
						match = probableMatch;
				}
				if (match != null) {
					mLOGGER.debug("#" + personne.getPrenom() + " " + personne.getNom() + " -> " + match.getPrenom() + " " + match.getNom());
				}
				else {
					mLOGGER.error("No match has been found for user : " + personne.getPrenom() + " " + personne.getPrenom());
				}
			}
			mLOGGER.debug("-----");
		}
	}
	
	public void printSavedSolution()
	{
		for (Participant personne : mParticipantList)
		{
			Participant match = null;
			for (Participant probableMatch : mParticipantList)
			{
				if (personne.getPreviousMates().get(personne.getPreviousMates().size()-1) == probableMatch.getId())
					match = probableMatch;
			}
			if (match != null) {
				mLOGGER.debug("#" + personne.getPrenom() + " " + personne.getNom() + " -> " + match.getPrenom() + " " + match.getNom());
			}
		}
	}

	public void sendMails() throws IOException, MessagingException {
		Gmail service = MailManager.getGMailService();
		for (Participant personne : mParticipantList)
		{
			Participant designe = null;
			for (Participant participant : mParticipantList)
			{
				if (participant.getId() == personne.getPreviousMates().get(personne.getPreviousMates().size()-1))
				{
					designe = participant;
				}
			}
			mMailManager.sendXMasMails(personne,designe);
		}
		mLOGGER.info("-----");
	}

	public boolean isTestingMails() {
		return mMailTest;
	}

	public void sendTestMails() throws MessagingException, IOException {
		for (Participant personne : mParticipantList)
		{
			if (personne.getNom().toUpperCase().equalsIgnoreCase("BEZE"))
			{
				mMailManager.sendTestMailTo(personne);
			}
		}
	}

	public void saveToOutPutFile() {
		
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String jsonString = gson.toJson(mSavedList);
		FileWriter writer = null;
		try {
			writer = new FileWriter(mInputFilePath+".new");
			writer.write(jsonString);
		} catch (IOException e) {
			mLOGGER.error(e);
		}
		finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					mLOGGER.error(e);
				}
			}
		}
	}

	public void saveSolution() {
		for (Participant personne : mParticipantList)
		{
			int id_designe = mSolution.getIntVal(varArray[mParticipantList.indexOf(personne)]);
			
			personne.getPreviousMates().add( id_designe);
		}
	}
}
