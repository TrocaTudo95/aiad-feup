package emergencies;

import behaviours.ResourceManager;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Ambulance extends Agent {
	private int position_x;
	private int position_y;
	public EmergencyMessage em;
	
	private AID[] emergency_agents;
	private AID[] resource_agents;
	
	
	private ResourceManager manager;
	

	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length == 2) {
			position_x = Integer.parseInt((String) args[0]);
			position_y = Integer.parseInt((String) args[1]);
		}
		else {
			position_x=0;
			position_y=0;
		}
		
		System.out.println("Ambulance " + getAID().getName() + " is ready.");
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")\n");
		em=new EmergencyMessage(0,position_x,position_y,getAID());
		manager = new ResourceManager(this);
		// Register the ambulance in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ambulance");
		sd.setName("PAM");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		
		// Add a TickerBehaviour that schedules a request to emergency agents every minute
		addBehaviour(new TickerBehaviour(this, 60000) {
			protected void onTick() {
				
				setResourceAgents(listAllAgents("ambulance"));
				setEmergencyAgents(listAllAgents("emergency"));
				
				
				myAgent.addBehaviour(manager.new InformAmbulances());
				
			}
		});
	}
	
	
	private AID[] listAllAgents(String type) {
		AID[] agents = new AID[0];
		
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType(type);
		template.addServices(sd);
		
		System.out.println("\nFound the following " + type + " agents:");

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
		return emergency_agents;
	}


	public void setEmergencyAgents(AID[] emergency_agents) {
		this.emergency_agents = emergency_agents;
	}


	public AID[] getResourceAgents() {
		return resource_agents;
	}


	public void setResourceAgents(AID[] resource_agents) {
		this.resource_agents = resource_agents;
	}
	
	public int getX() {
		return position_x;
	}
	
	public int getY() {
		return position_y;
	}
	
	public EmergencyMessage getMessage() {
		return em;
	}
	
	public boolean checkDistance() {
		// TODO Auto-generated method stub
		return true;
	}

}
