package reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class Reader{
	
	private static jade.core.Runtime runtime;
	private static Profile profile;
	
	
	public static void main(String [] args) throws FileNotFoundException{
		
		createJade();
		parseText();
	}
	
	
	public static void parseText() throws FileNotFoundException {
		
		String file = "test.txt";
		
		File filenew = new File("data.txt");
		
		Scanner scan = new Scanner(filenew);
	    while(scan.hasNextLine()){
	        String line = scan.nextLine();
	        //Here you can manipulate the string the way you want
	        System.out.print(line);
	        System.out.print('\n');
	        
	    	String[] part1 = line.split("(");		    	
	    	String agentName = part1[0];	    	
	    	String [] agentArguments = part1[1].split(",");		    	    	
	    	String[] part2 = agentArguments[2].split(")");
	    	agentArguments[2] = part2[0];
	    	
	    	String agentNick  = "em1";
	    	
	    	createAgent(agentNick,agentName, agentArguments);
	    }
	
	
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
			System.out.println( "Unable to open file '" +  file + "'"); 
			
		} catch (IOException e) {
			System.out.println( "Error reading file '"    + file + "'");                	           
		}
	}
	
	public static void createJade() {
		
		//Get the JADE runtime interface (singleton)
		runtime = jade.core.Runtime.instance();
		
		//Create a Profile, where the launch arguments are stored
	    profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "TestContainer");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		
	}
	
	public static void createAgent(String agentNick, String agentName, String [] agentArguments) {
		
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
