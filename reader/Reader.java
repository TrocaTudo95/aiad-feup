package reader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Reader{
	
	private jade.core.Runtime runtime;
	private Profile profile;
	
	
	public void getLine() {
		
		String fileName = "temp.txt";
	
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		 		    	
		    	String[] part1 = line.split("(");		    	
		    	String agentName = part1[0];	    	
		    	String [] agentArguments = part1[1].split(",");		    	    	
		    	String[] part2 = agentArguments[2].split(")");
		    	agentArguments[2] = part2[0];
		    	
		    	String agentNick  = "em1";
		    	
		    	createAgent(agentNick,agentName, agentArguments);
		    	
		    	
		    }
		    
		} catch (FileNotFoundException e) {
			System.out.println( "Unable to open file '" +  fileName + "'"); 
			
		} catch (IOException e) {
			System.out.println( "Error reading file '"    + fileName + "'");                	           
		}
	}
	
	public void createJade() {
		
		//Get the JADE runtime interface (singleton)
		runtime = jade.core.Runtime.instance();
		
		//Create a Profile, where the launch arguments are stored
	    profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "TestContainer");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		
	}
	
	public void createAgent(String agentNick, String agentName, String [] agentArguments) {
		
		//create a non-main agent container
		ContainerController container = runtime.createAgentContainer(profile);
		try {
		        AgentController ag = container.createNewAgent("agentnick", "emergencies." + agentName, agentArguments);//arguments   
		        ag.start();
		} catch (StaleProxyException e) {
		    e.printStackTrace();
		}
			
	}

}
