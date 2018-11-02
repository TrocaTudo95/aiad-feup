/*****************************************************************
JADE - Java Agent DEvelopment Framework is a framework to develop 
multi-agent systems in compliance with the FIPA specifications.
Copyright (C) 2000 CSELT S.p.A. 

GNU Lesser General Public License

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation, 
version 2.1 of the License. 

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the
Free Software Foundation, Inc., 59 Temple Place - Suite 330,
Boston, MA  02111-1307, USA.
 *****************************************************************/

package emergencies;


import java.io.IOException;
import java.util.ArrayList;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class Ambulance extends Agent {
	private AID[] emergencyAgents;
	private int localization_x;
	private int localization_y;
	private AID[] ambulanceAgents;
	private EmergencyMessage informationMessage;
	private ArrayList <EmergencyMessage> ambulancesLocalization= new ArrayList<EmergencyMessage>();


	// Put agent initializations here
	protected void setup() {
		// Printout a welcome message
		
		Object[] args = getArguments();
		if (args != null && args.length > 2) {
			localization_x = Integer.parseInt((String) args[0]);
			localization_y = Integer.parseInt((String) args[1]);
		}
		else {
			localization_x=0;
			localization_y=0;
		}
		
		informationMessage= new EmergencyMessage(0,localization_x,localization_y, getAID());
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
				
			
		System.out.println("Ambulance "+getAID().getName()+" is ready.");
		
			// Add a TickerBehaviour that schedules a request to emergency agents every minute
			addBehaviour(new TickerBehaviour(this, 60000) {
				protected void onTick() {
					// Update the list of resources agents
					listAllAmbulances();
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("emergency");
					template.addServices(sd);
					try {
						DFAgentDescription[] result = DFService.search(myAgent, template); 

						System.out.println("Found the following emergencies:");

						emergencyAgents = new AID[result.length];
						for (int i = 0; i < result.length; ++i) {
							emergencyAgents[i] = result[i].getName();
							System.out.println(emergencyAgents[i].getName());
						}
					}
					catch (FIPAException fe) {
						fe.printStackTrace();
					}

					// Perform the request
					listAllAmbulances();
					myAgent.addBehaviour(new RequestPerformer());
				}
			} );
		}
	private void listAllAmbulances() {
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("ambulance");
		template.addServices(sd);
		try {
			DFAgentDescription[] result = DFService.search(this, template); 
			ambulanceAgents = new AID[result.length];
			for (int i = 0; i < result.length; ++i) {
				ambulanceAgents[i] = result[i].getName();
				System.out.println(ambulanceAgents[i].getName());
			}
		}
		catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
	




	// Put agent clean-up operations here
	protected void takeDown() {
		// Printout a dismissal message
		System.out.println("Ambulance-agent "+getAID().getName()+" terminating.");

	}

	/**
	   Inner class RequestPerformer.
	   This is the behaviour used by resource agents to request emergencies 
	   agents the target book.
	 */
	private class RequestPerformer extends Behaviour {

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
				for (int i = 0; i < emergencyAgents.length; ++i) {
					cfp.addReceiver(emergencyAgents[i]);

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
					if (repliesCnt >= emergencyAgents.length) {
						// We received all replies
						step = 2; 
					}
				}
				else {
					block();
				}
				break;
			case 2:
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
				break;
			case 3:      
				// Receive the reply
				reply = myAgent.receive(mt);
				if (reply != null) {
					// reply received
					if (reply.getPerformative() == ACLMessage.INFORM) {
						// Purchase successful. We can terminate

						System.out.println("emergency +"+reply.getSender().getName()+" successfully responded");
						System.out.println("Priority = "+higherPriority);

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
		
	private class InformAmbulances extends Behaviour{

		private MessageTemplate mt;
		
		@Override
		public void action() {
			// Send the information about this ambulance to all ambulances
			ACLMessage inf = new ACLMessage(ACLMessage.INFORM);
			for (int i = 0; i < ambulanceAgents.length; ++i) {
				inf.addReceiver(ambulanceAgents[i]);

			} 

			inf.setConversationId("ambulances_inf");
			try {
				inf.setContentObject(informationMessage);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			inf.setReplyWith("inf"+System.currentTimeMillis()); // Unique value
			myAgent.send(inf);
			// Prepare the template to get proposals
			mt = MessageTemplate.and(MessageTemplate.MatchConversationId("ambulances_inf"),
					MessageTemplate.MatchInReplyTo(inf.getReplyWith()));
			
			int repliesCount=0;
			
			while(repliesCount<ambulanceAgents.length) {
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					try {
						ambulancesLocalization.add((EmergencyMessage) reply.getContentObject());
						repliesCount++;
					} catch (UnreadableException e) {
						e.printStackTrace();
					}
				}
			}
		
			myAgent.addBehaviour(new RequestPerformer());
		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
}
