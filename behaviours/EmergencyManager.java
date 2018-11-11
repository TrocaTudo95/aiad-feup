package behaviours;

import java.io.IOException;

import emergencies.Emergency;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class EmergencyManager {
	Emergency emergency_agent;
	private boolean being_solved=false;
	
	public EmergencyManager(Emergency emergency_agent) {
		this.emergency_agent= emergency_agent;
	}
	
	public class RequestEmergencyServer extends CyclicBehaviour {
		
		private static final long serialVersionUID = 1L;
		private int step=0;
		public void action() {
			switch(step) {
			case 0:
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null ) {
				ACLMessage reply = msg.createReply();

				if(being_solved) {
					reply.setPerformative(ACLMessage.REFUSE);
				}
				else{
					//being_solved=true;
					reply.setPerformative(ACLMessage.PROPOSE);
					
					try {
						reply.setContentObject(emergency_agent.getMessage());
					} catch (IOException e) {
						e.printStackTrace();
					}
				
				}
				
				myAgent.send(reply);
				step=1;
			}
			else {
				block();
			}
			break;
			case 1:
				MessageTemplate inf = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
				ACLMessage msg_inf = myAgent.receive(inf);
				if (msg_inf != null ) {
					being_solved=true;
					System.out.println("im "+ myAgent.getLocalName() +" and im being solved");
					step=0;
				}
				else {
					step=0;
				}
				
		} 
	}
	}

	public class AcceptAmbulance extends CyclicBehaviour {
		Boolean served=false;
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				
				myAgent.doDelete();
				
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

}
