package emergencies;

import behaviours.ResourceManager;
import behaviours.ResourceManager.RequestResourceServer;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Ambulance extends Agent {

	private static final long serialVersionUID = 1L;
	private int position_x;
	private int position_y;
	private int speed;
	
	private EmergencyMessage message;
	private EmergencyMessage current_emergency;

	private ResourceManager manager;
	

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
		}
		
		speed=1;
		
		System.out.println("Ambulance " + getAID().getName() + " is ready.");
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")\n");
		
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
		
		addBehaviour(manager.new RequestResourceServer());
		
		addBehaviour(new TickerBehaviour(this, 30000) {
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
		
		System.out.println("Found the following " + type + " agents:");

		try {
			DFAgentDescription[] result = DFService.search(this, template); 
			agents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				agents[i] = result[i].getName();
				System.out.println(agents[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		System.out.println();
		
		return agents;
	}
	
	
	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// Printout a dismissal message
		System.out.println("Ambulance "+getAID().getName()+" terminating.");

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
	
	public EmergencyMessage getCurrent_emergency() {
		return current_emergency;
	}


	public void setCurrent_emergency(EmergencyMessage current_emergency) {
		this.current_emergency = current_emergency;
	}



}
