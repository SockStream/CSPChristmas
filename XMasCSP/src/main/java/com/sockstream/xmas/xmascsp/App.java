package com.sockstream.xmas.xmascsp;
import com.sockstream.xmas.model.XMasModel;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	XMasModel model = XMasModel.getInstance();
    	model.loadOptions(args);
    	model.loadFromInputFile();
    	model.initialize();
    	
    	if (model.isTestingMails())
    	{
    		model.sendTestMails();
    		return;
    	}
    	
    	model.solve();
    	
    	model.saveSolution();
    	
    	if (model.isTesting())
    	{
    		model.printSolutions();
    		model.printSavedSolution();
    	}
    	else
    	{
    		model.printSavedSolution();
    		model.sendMails();
    		model.saveToOutPutFile();
    	}
    	
    }
}
