package behaviours;

import java.io.IOException;
import java.util.ArrayList;

import emergencies.Ambulance;
import emergencies.EmergencyMessage;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ResourceManager{
	
	Ambulance resource_agent;
	EmergencyMessage  emergency = new EmergencyMessage();
	private ArrayList <EmergencyMessage> resource_positions= new ArrayList<EmergencyMessage>();
	private Pair<Double, Double> hospital=new Pair(2,3);
	public ResourceManager(Ambulance resource_agent) {
		this.resource_agent= resource_agent;
	}
	
	public double calculateDistance(EmergencyMessage ambulance, EmergencyMessage emergency) {
		return Math.sqrt(Math.pow(ambulance.getX()-emergency.getX(), 2)+Math.pow(ambulance.getY()-emergency.getY(), 2));
	}
	
	public double distanceHospital() {
		return 0;
	}
	

	private boolean checkDistance(EmergencyMessage emergency) {
		double my_distance = calculateDistance(resource_agent.getMessage(),emergency);
		
		for(int i =0; i < resource_positions.size(); i++) {
			double resource_distance = calculateDistance(resource_positions.get(i), emergency);
			
			if(resource_distance < my_distance)
				return false;
		}
		
		return true;
	}

	
	
	public class RequestEmergency extends Behaviour {
		

		private static final long serialVersionUID = 1L;
		private MessageTemplate mt; 	// The template to receive replies
		
		private int step = 0;
		private int i = 0;

		public void action() {
			switch (step) {
			// SEND CFP TO Ith EMERGENCY
			case 0:
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				if(resource_agent.getEmergencyAgents().length > i)
					cfp.addReceiver(resource_agent.getEmergencyAgents()[i]);
				else
					step =2;

				cfp.setConversationId("emergency");
				cfp.setReplyWith("cfp"+System.currentTimeMillis());
				myAgent.send(cfp);

				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				i++;
				step = 1;
				break;
			// WAIT FOR EMERGENCY PROPOSAL OR REFUSE
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						
						EmergencyMessage  emergency = new EmergencyMessage();
						
						try {
							emergency = (EmergencyMessage) reply.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						
						System.out.println("Emergency " + emergency.getSenderID().getName() + "being alocated by " + myAgent.getName() + ".\n");
						step =2;
						
					}else 
						step =0;
					
					
				}else {
					block();
				}
				break;
			}
		}

		@Override
		public boolean done() {
			return(step>1);
		}

	}
		
	public class InformAmbulances extends Behaviour{

		private MessageTemplate mt;
		private int step = 0;
		
	
		private int replies_cnt =0;
		
		@Override
		public void action() {
			
			switch (step) {
			case 0:
				// Send the information to all resources
				ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
				for (int i = 0; i < resource_agent.getResourceAgents().length; ++i) {
					inf.addReceiver(resource_agent.getResourceAgents()[i]);
				} 

				inf.setConversationId("resource_inf");
				try {
					inf.setContentObject(resource_agent.getMessage());
				} catch (IOException e) { 
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				inf.setReplyWith("inf"+System.currentTimeMillis()); // Unique value
				myAgent.send(inf);
				
				step = 1;
				break;
			case 1:
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("resource_inf"),
						MessageTemplate.MatchPerformative(ACLMessage.INFORM));
				
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {

					// Reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						try {
							resource_positions.add((EmergencyMessage) reply.getContentObject());
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						replies_cnt++;
					}
		
					if (replies_cnt >= resource_agent.getResourceAgents().length) {
						//myAgent.addBehaviour(new Reques());
						
						step = 2; 
				}}
				else {
					block();
				}
				break;
			default:
				break;
			}
		}
		

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
		
		
	}

}
