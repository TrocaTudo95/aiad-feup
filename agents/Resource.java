package agents;

import java.util.ArrayList;

import behaviours.ResourceManager;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.EmergencyMessage;

public class Resource extends Agent {

	private static final long serialVersionUID = 1L;
	private int position_x;
	private int position_y;
	private int speed;
	
	private EmergencyMessage message;
	private ResourceManager manager;
	
	public static ArrayList <EmergencyMessage> being_treated = new ArrayList<EmergencyMessage>();
	

	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length == 3) {
			speed = Integer.parseInt((String) args[0]);
			position_x = Integer.parseInt((String) args[1]);
			position_y = Integer.parseInt((String) args[2]);
		}
		else {
			position_x=0;
			position_y=0;
			speed=1;
		}
		
		
		System.out.println("Ambulance " + getAID().getName() + " is ready.");
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")");
		System.out.println("Speed: " + speed + "\n");
		
		message = new EmergencyMessage(0,position_x,position_y,getAID());
		manager = new ResourceManager(this);
		
		// Register the ambulance in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("resources");
		sd.setName("PAM");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// Add Resource Behaviours
		addBehaviour(manager.new AmbulanceRequestsServer());
		addBehaviour(new TickerBehaviour(this, 30000) {
			private static final long serialVersionUID = 1L;
			protected void onTick() {
				addBehaviour(manager.new RequestEmergency());
			}
		});
		
		
	}
	
	
	public AID[] listAllAgents(String type) {
		AID[] agents = new AID[0];
		
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		template.addServices(sd);
		

		try {
			DFAgentDescription[] result = DFService.search(this, template); 
			agents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				agents[i] = result[i].getName();
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		return agents;
	}
	
	
	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println("Ambulance " + getAID().getName() + " terminating.");
	}


	public AID[] getEmergencyAgents() {
		AID[] agents = listAllAgents("emergency");

		if(agents!= null)
			return agents;
		else 
			return new AID[0];
	}


	public AID[] getResourceAgents() {
		AID[] agents = listAllAgents("resources");

		if(agents!= null)
			return agents;
		else 
			return new AID[0];
	}

	public int getX() {
		return position_x;
	}
	
	public int getY() {
		return position_y;
	}
	
	public EmergencyMessage getMessage() {
		return message;
	}
	
	public int getSpeed() {
		return speed;
	}
	
	public void updateAmbulancePosition(int x, int y) {
		this.position_x=x;
		this.position_y=y;
		message.setX(x);
		message.setY(y);
	}

}
