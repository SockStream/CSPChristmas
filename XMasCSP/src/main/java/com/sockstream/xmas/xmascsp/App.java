package com.sockstream.xmas.xmascsp;
import com.sockstream.xmas.model.XMasModel;

public class App 
{
    public static void main( String[] args ) throws Exception
    {
    	XMasModel model = XMasModel.getInstance();
    	model.loadOptions(args);
    	
    	model.initialize();
    	
    	if (model.isTestingMails())
    	{
    		model.sendTestMails();
    		return;
    	}
    	
    	model.solve();
    	
    	if (model.isTesting())
    	{
    		model.printSolutions();
    	}
    	else
    	{
    		model.sendMails();
    		model.save();
    	}
    	
    }
}
