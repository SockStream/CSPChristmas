package com.sockstream.xmas.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
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
import com.sockstream.xmas.mailing.MailManager;

public class XMasModel {
	private static XMasModel mINSTANCE;
	private static Logger mLOGGER = LogManager.getLogger(XMasModel.class);
	
	private boolean mTest = false;
	private Model mModel;
	private List<Participant> mParticipantList;
	private boolean mMailTest;
	private Solution mSolution;
	private List<Solution> mSolutionList;
	
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
		// TODO Auto-generated method stub
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
			mLOGGER.error(e);
			formatter.printHelp("utility-name", options);
			
			System.exit(1);
			return;
		}
		mTest = cmd.hasOption("test");
		mMailTest = cmd.hasOption("mailTest");
		
	}

	public void initialize() {
		Participant p1 = new Participant("NOM", "Prenom", "mail");
		Participant p2 = new Participant("Nom2", "Prenom2","mail2");
		Participant p3 = new Participant("Nom3", "Prenom3", "mail3");
		
		//assuming p1 and p3 are in a relationship
		p1.setMoitie(p3);
		p3.setMoitie(p1);
		
		//adding previous updates from past christmas
		p2.getPreviousMates().add(p1);
		
		mParticipantList.add(p1);
		mParticipantList.add(p2);
		mParticipantList.add(p3);

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
		mLOGGER.info(solutionList.size() + " solutions trouvées");
		while (solutionList.isEmpty())
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
					mLOGGER.debug("#" + personne.getPrenom() + " " + personne.getNom() + " -> " + match.getPrenom() + " " + match.getNom());
				}
				else {
					mLOGGER.error("No match has been found for user : " + personne.getPrenom() + " " + personne.getPrenom());
				}
			}
			mLOGGER.debug("-----");
		}
	}

	public void sendMails() {
		for (Participant personne : mParticipantList)
		{
			
			personne.getPreviousMates().add( mParticipantList.get(mSolution.getIntVal(personne.getBuddy())));
			MailManager.sendXMasMails(personne);
			
		}
		mLOGGER.info("-----");
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
