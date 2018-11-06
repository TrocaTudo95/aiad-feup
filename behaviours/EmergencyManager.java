package behaviours;

import java.io.IOException;

import emergencies.Emergency;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EmergencyManager {
	Emergency emergency_agent;
	
	public EmergencyManager(Emergency emergency_agent) {
		this.emergency_agent= emergency_agent;
	}
	
	public class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.PROPOSE);
				
				try {
					reply.setContentObject(emergency_agent.getMessage());
				} catch (IOException e) {
					e.printStackTrace();
				}
			
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	public class PurchaseOrdersServer extends CyclicBehaviour {
		Boolean served=false;
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// ACCEPT_PROPOSAL Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.INFORM);
				served=true;

				myAgent.send(reply);
				myAgent.doDelete();
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

}
