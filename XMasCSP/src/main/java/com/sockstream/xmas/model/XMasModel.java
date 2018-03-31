package com.sockstream.xmas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solution;
import org.chocosolver.solver.variables.IntVar;
import com.sockstream.xmas.mailing.MailManager;

public class XMasModel {
	private static XMasModel mINSTANCE;
	
	private boolean mTest = false;
	private Model mModel;
	private List<Participant> mParticipantList;
	private boolean mMailTest;
	private Solution mSolution;
	private List<Solution> mSolutionList;
	private static Logger mLogger = LogManager.getLogger(XMasModel.class);
	
	private XMasModel()
	{
		mModel  = new Model("XMas Solver");
		mParticipantList = new ArrayList<Participant>();
	}
	
	public static XMasModel getInstance()
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
		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd;
		try
		{
			cmd = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			mLogger.error(e.getMessage());
			formatter.printHelp("utility-name", options);
			System.exit(1);
			return;
		}
		mTest = cmd.hasOption("test");
		mMailTest = cmd.hasOption("mailTest");
	}

	public void initialize() {
		Participant clement = new Participant("BEZE", "Clement", "clement.beze@gmail.com");
		Participant pi = new Participant("BOURDON", "PI", "py.bourdon@gmail.com");
		Participant julien = new Participant("MAZUREK", "Julien", "mazurek.julien@free.fr");
		Participant celia = new Participant("ROZAN", "Celia", "celia.rozan@hotmail.fr");
		Participant maxime = new Participant("MORTIER", "Maxime", "sigmakappa666@gmail.com");
		Participant lisa = new Participant("BENOTHMANE","Lisa","lisa.benothmane@gmail.com");
		Participant atri = new Participant("PILIER", "Maxime", "xxxxx");
		
		celia.setMoitie(julien);
		julien.setMoitie(celia);
		maxime.setMoitie(lisa);
		lisa.setMoitie(maxime);

		//2015
		pi.getPreviousMates().add(celia);
		//2016
		pi.getPreviousMates().add(clement);
		//2017
		pi.getPreviousMates().add(julien);
		
		//2015
		clement.getPreviousMates().add(julien);
		//2016
		clement.getPreviousMates().add(pi);
		//2017
		clement.getPreviousMates().add(lisa);
		
		//2015
		celia.getPreviousMates().add(maxime);
		//2016
		celia.getPreviousMates().add(maxime);
		//2017
		celia.getPreviousMates().add(pi);
		
		//2015
		julien.getPreviousMates().add(atri);
		//2016
		julien.getPreviousMates().add(celia);
		//2017
		julien.getPreviousMates().add(maxime);
		
		//2015
		lisa.getPreviousMates().add(pi);
		//2016
		lisa.getPreviousMates().add(julien);
		//2017
		lisa.getPreviousMates().add(clement);
		
		//2016
		maxime.getPreviousMates().add(atri);
		//2015
		maxime.getPreviousMates().add(clement);
		//2017
		maxime.getPreviousMates().add(celia);
		
		//2015
		atri.getPreviousMates().add(lisa);
		//2016
		atri.getPreviousMates().add(lisa);
		
		mParticipantList.add(clement);
		mParticipantList.add(pi);
		mParticipantList.add(julien);
		mParticipantList.add(celia);
		mParticipantList.add(maxime);
		mParticipantList.add(lisa);
		mParticipantList.add(atri);

		int[] possibleValues = new int[mParticipantList.size()];
		int index = 0;
		for (Participant personne : mParticipantList)
		{
			possibleValues[index] = personne.getId().getValue();
			index ++;
		}
		IntVar[] varArray = mModel.intVarArray(possibleValues.length, possibleValues);
		
		for (Participant personne : mParticipantList)
		{
			personne.setBuddy(varArray[mParticipantList.indexOf(personne)]);
			
			mModel.arithm(personne.getBuddy(),"!=",personne.getId()).post();
			if (personne.getMoitie() != null)
			{
				mModel.arithm(personne.getBuddy(),"!=",personne.getMoitie().getId()).post();
			}
			
			for (Participant previous : personne.getPreviousMates())
			{
				mModel.arithm(personne.getBuddy(), "!=", previous.getId()).post();
			}
		}
		mModel.allDifferent(varArray).post();
	}

	public Model getModel() {
		return mModel; 
	}

	public void solve() throws ExecutionException {
		List<Solution> solutionList = mModel.getSolver().findAllSolutions();
		mLogger.info(solutionList.size() + " solutions trouvées");
		while (!solutionList.isEmpty())
		{
			removeOlderMates();
			solutionList = mModel.getSolver().findAllSolutions();
		}
		Random rand = new Random();
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
		
		if (max == 0)
			throw new ExecutionException("Aucune solution possible au problème donné",null);
		

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
				for (Participant buddy : mParticipantList)
				{
					if (buddy.getId().getValue() == soluces.getIntVal(personne.getBuddy()))
						match = buddy;
				}
				if (match != null) {
					mLogger.debug("#" + personne.getPrenom() + " " + personne.getNom() + " -> " + match.getPrenom() + " " + match.getNom());
				}
				else {
					mLogger.error("A null match has been found for : " + personne.getPrenom() + " " + personne.getNom());
				}
					
			}
			mLogger.debug("-----");
		}
	}

	public void sendMails() {
		for (Participant personne : mParticipantList)
		{
			personne.getPreviousMates().add( mParticipantList.get(mSolution.getIntVal(personne.getBuddy())));
			MailManager.sendXMasMails(personne);
			
		}
		mLogger.info("-----");
	}

	public boolean isTestingMails() {
		return mMailTest;
	}

	public void sendTestMails() {
		for (Participant personne : mParticipantList)
		{
			MailManager.sendTestMailTo(personne);
		}
	}
}
