package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import utils.EmergencyMessage;

import java.io.Serializable;

import behaviours.EmergencyManager;


public class Emergency extends Agent {

	private static final long serialVersionUID = 1L;

	private int priority;
	private int position_x;
	private int position_y;
	
	private EmergencyManager manager;
	private EmergencyMessage message;
	
	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length == 3) {
			priority = Integer.parseInt((String) args[0]);
			position_x = Integer.parseInt((String) args[1]);
			position_y = Integer.parseInt((String) args[2]);
		}
		else {
			priority=0;
			position_x=0;
			position_y=0;
		}
		
		System.out.println("New Emergengy " + getAID().getName());
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")");
		System.out.println("Priority: " + priority + "\n");
		
		message = new EmergencyMessage(priority,position_x,position_y,getAID());
		manager = new EmergencyManager(this);

		// Register the emergency in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("emergency");
		sd.setName("teste");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
		// Add Emergency Behaviours
		addBehaviour(manager.new RequestEmergencyServer());
		addBehaviour(manager.new AcceptAmbulance());
	}

	protected void takeDown() {
		try {
			DFService.deregister(this);
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
		
	}

	public Serializable getMessage() {
		return message;
	}

	
	
}
