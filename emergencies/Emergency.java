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

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import java.util.*;

public class Emergency extends Agent {

	private static final long serialVersionUID = 1L;
	private int priority;
	private int position_x;
	private int position_y;
	
	protected void setup() {

		Object[] args = getArguments();
		if (args != null && args.length > 2) {
			priority = Integer.parseInt((String) args[0]);
			position_x = Integer.parseInt((String) args[1]);
			position_y = Integer.parseInt((String) args[2]);
		}
		else {
			priority=0;
			position_x=0;
			position_y=0;
		}
		
		System.out.println("\nNew Emergengy");
		System.out.println("Priority: " + priority);
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")\n");
				
		
		// Create and show the GUI 
		// myGui = new EmergencyGui(this);
		// myGui.showGui();

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

		addBehaviour(new OfferRequestsServer());
		addBehaviour(new PurchaseOrdersServer());
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
		System.out.println("Emergency "+getAID().getName()+" atended.");
	}

	
	private class OfferRequestsServer extends CyclicBehaviour {
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				ACLMessage reply = msg.createReply();

				reply.setPerformative(ACLMessage.PROPOSE);
				reply.setContent(String.valueOf(priority));
			
				myAgent.send(reply);
			}
			else {
				block();
			}
		}
	}  // End of inner class OfferRequestsServer

	private class PurchaseOrdersServer extends CyclicBehaviour {
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
