package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import emergencies.Ambulance;
import emergencies.EmergencyMessage;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ResourceManager{
	
	Ambulance resource_agent;

	private Pair<Double, Double> hospital=new Pair(2,3);
	private EmergencyMessage emergency;
	public ResourceManager(Ambulance resource_agent) {
		this.resource_agent= resource_agent;
	}
	
	public double calculateTime(EmergencyMessage ambulance, EmergencyMessage emergency) {
		double time=0;
		double distance_to_emergency = Math.sqrt(Math.pow(ambulance.getX()-emergency.getX(), 2)+Math.pow(ambulance.getY()-emergency.getY(), 2));
		double distance_to_hospital = distanceHospital(emergency);
		time = (distance_to_emergency + distance_to_hospital) * resource_agent.getSpeed();
			if(resource_agent.getCurrent_emergency()!=null) {
				time+=2;//TODO:tempo atual que falta para acabar a emergência
			}
			Random rand = new Random();

			int  time_in_hospital = rand.nextInt(3) + 1;
			time+= time_in_hospital;
			
		
		return time;
	}
	
	public double distanceHospital(EmergencyMessage emergency) {
		return Math.sqrt(Math.pow(hospital.getX()-emergency.getX(), 2)+Math.pow(hospital.getY()-emergency.getY(), 2));
	}
	

//	private boolean checkDistance(EmergencyMessage emergency) {
//		double my_distance = calculateTime(resource_agent.getMessage(),emergency);
//		
//		for(int i =0; i < resource_positions.size(); i++) {
//			double resource_distance = calculateTime(resource_positions.get(i), emergency);
//			
//			if(resource_distance < my_distance)
//				return false;
//		}
//		
//		return true;
//	}

	
	
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
		
	public class InformAmbulances extends CyclicBehaviour{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		private int step = 0;
		private ArrayList <ACLMessage> resource_positions= new ArrayList<ACLMessage>();
		
	
		private int replies_cnt =0;
		
		@Override
		public void action() {
			AID best_agent = myAgent.getAID();
			switch (step) {
			case 0:
				// Send the information to all resources
				ACLMessage inf = new ACLMessage(ACLMessage.CFP);
				for (int i = 0; i < resource_agent.getResourceAgents().length; ++i) {
					if(!resource_agent.getResourceAgents()[i].equals(myAgent.getAID()))
					inf.addReceiver(resource_agent.getResourceAgents()[i]);
				} 

				inf.setConversationId("resource_inf");
				try {
					inf.setContentObject(emergency);
				} catch (IOException e) { 
					e.printStackTrace();
				}
				
				inf.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				myAgent.send(inf);
				
				step = 1;
				break;
			case 1:
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("resource_inf"),
						MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
				
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {

					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
				
							resource_positions.add(reply);
						replies_cnt++;
					}
		
					if (replies_cnt >= resource_agent.getResourceAgents().length-1) {

						step = 2; 
				}
					}
				else {
					block();
				}
				break;
			case 2:
				double time = calculateTime(resource_agent.getMessage(),emergency);
				for(int i=0; i<resource_positions.size();i++) {
				
					if(Integer.parseInt(resource_positions.get(i).getContent()) < time) {
						time=Integer.parseInt(resource_positions.get(i).getContent());
						best_agent=resource_positions.get(i).getSender();
					}
				}
				
					if(best_agent.equals(myAgent.getAID())){
						step=4;
					}
					else {
						step=3;
					}
				
				
				break;
				
			case 3:
				// Send the information to all resources
				ACLMessage ref = new ACLMessage(ACLMessage.REFUSE);
				for (int i = 0; i < resource_positions.size(); ++i) {
					if(!resource_positions.get(i).getSender().equals(myAgent.getAID()))
						ref.addReceiver(resource_positions.get(i).getSender());
				} 

				ref.setConversationId("resource_inf");
				
				
				ref.setReplyWith("ref"+System.currentTimeMillis()); // Unique value
				myAgent.send(ref);
				
				break;
			case 4:
				
				// Send the information to all resources
				ACLMessage ref2 = new ACLMessage(ACLMessage.REFUSE);
				ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				for (int i = 0; i < resource_positions.size(); ++i) {
					if(!resource_positions.get(i).getSender().equals(myAgent.getAID()) && !resource_positions.get(i).getSender().equals(best_agent))
						ref2.addReceiver(resource_positions.get(i).getSender());
					else if(resource_positions.get(i).getSender().equals(best_agent)) {
						accept.addReceiver(resource_positions.get(i).getSender());
					}
				} 

				ref2.setConversationId("resource_inf");
				
				
				ref2.setReplyWith("ref"+System.currentTimeMillis()); // Unique value
				myAgent.send(ref2);
				accept.setConversationId("resource_inf");
				
				
				accept.setReplyWith("acc"+System.currentTimeMillis()); // Unique value
				myAgent.send(accept);
				
				
				break;
			default:
				break;
			}
		}
		

		
		
	}

}
