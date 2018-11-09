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
	
	Ambulance my_resource;

	private Pair<Double, Double> hospital=new Pair(2,3);
	private EmergencyMessage emergency;
	public ResourceManager(Ambulance resource_agent) {
		this.my_resource= resource_agent;
	}
	
	public double calculateTime(EmergencyMessage ambulance_msg, EmergencyMessage emergency) {
		double time=0;
		double distance_to_emergency = Math.sqrt(Math.pow(ambulance_msg.getX()-emergency.getX(), 2)+Math.pow(ambulance_msg.getY()-emergency.getY(), 2));
		double distance_to_hospital = distanceHospital(emergency);
		time = (distance_to_emergency + distance_to_hospital) * my_resource.getSpeed();
			if(my_resource.getCurrent_emergency()!=null) {
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
				if(my_resource.getEmergencyAgents().length > i)
					cfp.addReceiver(my_resource.getEmergencyAgents()[i]);
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
						myAgent.addBehaviour(new InformAmbulances());
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

		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		private int step = 0;
		private ArrayList <EmergencyMessage> resource_positions= new ArrayList<EmergencyMessage>();
		
	
		private int replies_cnt =0;
		
		@Override
		public void action() {
			
			switch (step) {
			case 0:
				
				ACLMessage inf = new ACLMessage(ACLMessage.CFP);
				AID[] resource_agents = my_resource.getResourceAgents();
				
				for (int i = 0; i < resource_agents.length; ++i) {
					if(!resource_agents[i].equals(myAgent.getAID()))
					inf.addReceiver(resource_agents[i]);
				} 

				inf.setConversationId("resource_inf");
				try {
					inf.setContentObject(emergency);
				} catch (IOException e) { 
					e.printStackTrace();
				}
				
				inf.setReplyWith("inf"+System.currentTimeMillis()); // Unique value
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
						try {
							resource_positions.add((EmergencyMessage) reply.getContentObject());
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						replies_cnt++;
					}
		
					if (replies_cnt >= my_resource.getResourceAgents().length-1) {

						step = 2; 
				}
					}
				else {
					block();
				}
				break;
			case 2:
				double time = calculateTime(my_resource.getMessage(),emergency);
				AID best_agent = myAgent.getAID();
				for(int i=0; i<resource_positions.size();i++) {
				
					if(resource_positions.get(i).getTime_to_respond() < time) {
						time=resource_positions.get(i).getTime_to_respond();
						best_agent=resource_positions.get(i).getSenderID();
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
				for (int i = 0; i < my_resource.getResourceAgents().length; ++i) {
					if(!my_resource.getResourceAgents()[i].equals(myAgent.getAID()))
						ref.addReceiver(my_resource.getResourceAgents()[i]);
				} 

				ref.setConversationId("resource_inf");
				try {
					ref.setContentObject(emergency);
				} catch (IOException e) { 
					e.printStackTrace();
				}
				
				ref.setReplyWith("inf"+System.currentTimeMillis()); // Unique value
				myAgent.send(ref);
				
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
	
	public class RequestResourceServer extends CyclicBehaviour{
		
		private int step=0;

		@Override
		public void action() {
			switch (step) {
			case 0:
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
		
				if (msg != null ) {
					ACLMessage reply = msg.createReply();
						
					reply.setPerformative(ACLMessage.PROPOSE);
					
					try {
						reply.setContentObject(my_resource.getMessage());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					myAgent.send(reply);
					
					step =1;
				}
				else {
					block();
				}
				break;
			case 1:
				break;
			}
				
			
		}
		
	}

}
