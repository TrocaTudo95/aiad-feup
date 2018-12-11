package launcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
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
	public static ArrayList<Double> times= new ArrayList<Double>(); 
	private static ArrayList<Integer> velocidades= new ArrayList<Integer>();
	private static ArrayList<Integer> prioridades= new ArrayList<Integer>();
	public static ArrayList<Double> distancias= new ArrayList<Double>();
	private static int number_emergencies=0;
	private static int number_ambulances=0;
	private static int velocidade_media=0;
	private static int media_gravidades=0;
	public static PrintWriter out2;
	public static PrintWriter out;
	
	
	public static void main(String [] args) throws InterruptedException, IOException{
		 
				   
//				    if(log.exists()==false){
//				            try {
//				            	System.out.println("RIP");
//								log.createNewFile();
//							} catch (IOException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//				    }
//				     out = new PrintWriter(new FileWriter(log, true));
//				    			out2 =new PrintWriter(new FileWriter(log2, true));
				    
			
		createJade();
		parseText();
		
	    System.out.println("ACABOUUUU");
//		random_generator(5,15);
//		//out.append()
//		Thread.sleep(80000);
//		 out.append(number_ambulances+","+number_emergencies+","+calculate_distancia()+","+calculate_time()+","+calculate_velocidade()+","+calculate_gravidade()+"\n");
//		    out.close();
//		    out2.close();
//		    try {
//				mainContainer.kill();
//			} catch (StaleProxyException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		
	}
	
	public static double calculate_velocidade() {
		double soma=0;
		for(int i=0; i<velocidades.size();i++) {
			soma+= velocidades.get(i);
		}
		return soma/number_ambulances;
	}
	
	public static double calculate_gravidade() {
		double soma=0;
		for(int i=0; i<prioridades.size();i++) {
			soma+= prioridades.get(i);
		}
		return soma/number_emergencies;
	}
	
	public static double calculate_time() {
		double soma=0;
		for(int i=0; i<times.size();i++) {
			soma+= times.get(i);
		}
		return soma/number_emergencies;
	}
	
	public static double calculate_distancia() {
		double soma=0;
		for(int i=0; i<distancias.size();i++) {
			soma+= distancias.get(i);
		}
		return soma/number_emergencies;
	}
	
	
	public static void parseText() throws InterruptedException, IOException {
		
		File log = new File("main_log.csv");
		 File log2 = new File("ind_log.csv");
		
		File file = new File("data.txt");
		if(log.exists()==false){
            try {
            	System.out.println("RIP");
				log.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    
        try {
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                
            	String line = scanner.nextLine();
                
                String[] info = line.split("-");
                                           
                out = new PrintWriter(new FileWriter(log, true));
    			out2 =new PrintWriter(new FileWriter(log2, true));
          
                random_generator(Integer.parseInt(info[0]),Integer.parseInt(info[1]));
        		//out.append()
                if(number_emergencies<15)
        		Thread.sleep(180000);
                else if(number_emergencies<20)
                	Thread.sleep(150000);
                else
                	Thread.sleep(200000);
        		 out.append(number_ambulances+","+number_emergencies+","+calculate_distancia()+","+calculate_time()+","+calculate_velocidade()+","+calculate_gravidade()+"\n");
        		    try {
        				mainContainer.kill();
        			} catch (StaleProxyException e) {
        				// TODO Auto-generated catch block
        				e.printStackTrace();
        			}
        		    profile = new ProfileImpl();
        			profile.setParameter(Profile.CONTAINER_NAME, "TestContainer");
        			profile.setParameter(Profile.MAIN_HOST, "localhost");
        		    mainContainer = runtime.createMainContainer(profile);
        		    out.close();
        		    out2.close();
                
                
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
	}
	public static void random_generator(int numAmbulances, int numEmergencies) {
		
		int am = 1;
		int em = 1;
		Random rand = new Random();
		String agentNick;
		number_ambulances=numAmbulances;
		number_emergencies=numEmergencies;
		velocidades= new ArrayList<Integer>();
		prioridades= new ArrayList<Integer>();
		distancias=new ArrayList<Double>();
		times=new ArrayList<Double>();
		
		for(int i=0; i<numAmbulances; i++) {
			int x= rand.nextInt(30)+1;
			int y=rand.nextInt(30)+1;
			int speed=rand.nextInt(10)+5;
			velocidades.add(speed);
			agentNick = "amb" + am;
        	am++;
        	Object [] agentArguments = {""+speed,""+x, ""+y}; 
        	createAgent(agentNick,"Ambulance", agentArguments);
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		for(int i=0; i<numEmergencies; i++) {
			int x= rand.nextInt(30)+1;
			int y=rand.nextInt(30)+1;
			int priority= rand.nextInt(10)+1;
			prioridades.add(priority);
			agentNick = "em" + em;
        	em++;
        	Object [] agentArguments = {""+priority,""+x, ""+y}; 
        	createAgent(agentNick,"Emergency", agentArguments);
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
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
        	AgentController ac = mainContainer.createNewAgent(agentNick, "agents." + agentName, agentArguments);
            ac.start();
        } catch (jade.wrapper.StaleProxyException e) {
            System.err.println("Error launching agent...");
        }
	}

}
