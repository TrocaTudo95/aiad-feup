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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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
		System.out.println("Priority: " + priority);
		System.out.println("Coordinates: (" + position_x + "," + position_y + ")\n");
		
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
		
		addBehaviour(manager.new RequestEmergencyServer());
		addBehaviour(manager.new AcceptAmbulance());
	}

	// Put agent clean-up operations here
	protected void takeDown() {
		// Deregister from the yellow pages
		System.out.println("IM DONE");
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
