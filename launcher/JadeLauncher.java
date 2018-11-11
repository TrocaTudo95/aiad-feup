package launcher;

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

public class JadeLauncher{
	
	private static jade.core.Runtime runtime;
	private static Profile profile;
	private static ContainerController mainContainer;
	
	
	public static void main(String [] args) throws FileNotFoundException, InterruptedException{
			
		createJade();
		parseText();
	}
	
	
	public static void parseText() throws FileNotFoundException, InterruptedException {
		
		int am = 1;
		int em = 1;
		
		File file = new File("data.txt");
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                
            	String line = scanner.nextLine();

                String[] info = line.split("-");
                           
                String agentNick;
                String agentName = info[0];
                Object [] agentArguments = {info[1], info[2],info[3]}; 
                
                if(agentName.equals("Ambulance")) {
                	agentNick = "amb" + am;
                	am++;
                }
                else {
                	agentNick = "em" +em;
                	em++;
                }
                
                createAgent(agentNick,agentName, agentArguments);
                Thread.sleep(3000);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
		
	}
	
	public static void createJade() {
		
		//Get the JADE runtime interface (singleton)
		runtime = jade.core.Runtime.instance();
		
		//Create a Profile, where the launch arguments are stored
	    profile = new ProfileImpl();
		profile.setParameter(Profile.CONTAINER_NAME, "TestContainer");
		profile.setParameter(Profile.MAIN_HOST, "localhost");
	    mainContainer = runtime.createMainContainer(profile);
	}
	
	public static void createAgent(String agentNick, String agentName, Object [] agentArguments) {

        try {
        	AgentController ac = mainContainer.createNewAgent(agentNick, "emergencies." + agentName, agentArguments);
            ac.start();
        } catch (jade.wrapper.StaleProxyException e) {
            System.err.println("Error launching agent...");
        }
	}

}
