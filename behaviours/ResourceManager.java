package behaviours;

import java.io.IOException;
import java.util.ArrayList;

import emergencies.Ambulance;
import emergencies.EmergencyMessage;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ResourceManager{
	
	Ambulance resource_agent;
	Boolean ready = false;
	
	public ResourceManager(Ambulance resource_agent) {
		this.resource_agent= resource_agent;
	}
	
	public class RequestPerformer extends Behaviour {

		private AID higherEmergency; 	// The agent who has highest priority
		private int higherPriority;  	// The highest priority
		private int repliesCnt = 0;		// The counter of replies from emergency agents
		private MessageTemplate mt; 	// The template to receive replies

		private int step = 0;

		public void action() {
			switch (step) {
			case 0:
				// Send the cfp to all emergencies
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < resource_agent.getEmergencyAgents().length; ++i) {
					cfp.addReceiver(resource_agent.getEmergencyAgents()[i]);

				} 

				cfp.setConversationId("emergency");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(cfp);
				// Prepare the template to get proposals
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
			case 1:
				// Receive all proposals/refusals from emergencies agents
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						// This is an offer 
						int priority = Integer.parseInt(reply.getContent());
						if (higherEmergency == null || priority > higherPriority) {
							// This is the highest priority at present
							higherPriority = priority;
							higherEmergency = reply.getSender();
						}
					}
					repliesCnt++;
					if (repliesCnt >= resource_agent.getEmergencyAgents().length) {
						// We received all replies
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
				if(ready && resource_agent.checkDistance()) {
					
					// Send the offer for help to the emergency with higher priority
					ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
					order.addReceiver(higherEmergency);
					order.setContent("the ambulance is coming to you sir\n");
					order.setConversationId("emergency");
					order.setReplyWith("order"+System.currentTimeMillis());
					myAgent.send(order);
					// Prepare the template to get the purchase order reply
					mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency"),
							MessageTemplate.MatchInReplyTo(order.getReplyWith()));
					step = 3;
				}
				break;
			case 3:      
				// Receive the reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {

						myAgent.doDelete();
					}
					else {
						System.out.println("Attempt failed: emergency already alocated.");
					}

					step = 4;
				}
				else {
					block();
				}
				break;
			}        
		}

	

		public boolean done() {
			if (step == 2 && higherEmergency == null) {
				System.out.println("Attempt failed: "+" there aren't emergencies");
			}
			return ((step == 2 && higherEmergency == null) || step == 4);
		}
	}// End of inner class RequestPerformer
		
	public class InformAmbulances extends Behaviour{

		private MessageTemplate mt;
		private int step = 0;
		
		private ArrayList <EmergencyMessage> resource_positions= new ArrayList<EmergencyMessage>();
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
					inf.setContentObject(new EmergencyMessage(0,resource_agent.getX(),resource_agent.getY(), resource_agent.getAID()));
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						replies_cnt++;
					}
		
					if (replies_cnt >= resource_agent.getResourceAgents().length) {

						ready =true;
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
