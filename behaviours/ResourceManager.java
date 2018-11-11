package behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import behaviours.ResourceManager.RequestEmergency;
import emergencies.Ambulance;
import emergencies.EmergencyMessage;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ResourceManager{
	
	Ambulance my_resource;
	private Pair<Integer, Integer> hospital=new Pair(10,10);
	
	EmergencyMessage  pendent_emergency;
	EmergencyMessage  current_emergency=null;
	long start_time = 0;
	long total_time = 0;
	
	public ResourceManager(Ambulance resource_agent) {
		this.my_resource= resource_agent;
	}
	
	public double calculateTime(EmergencyMessage ambulance_msg, EmergencyMessage emergency) {
		double time=0;
		double distance_to_emergency = Math.sqrt(Math.pow(ambulance_msg.getX()-emergency.getX(), 2)+Math.pow(ambulance_msg.getY()-emergency.getY(), 2));
		double distance_to_hospital = distanceHospital(emergency);
		time = (distance_to_emergency + distance_to_hospital) /my_resource.getSpeed();
			if(!current_emergency.equals(null)) {
				System.out.println("MEEERDDAAAA"+ current_emergency.getSenderID().getLocalName());
				time += (total_time - (start_time - System.currentTimeMillis())/1000);
			}
			Random rand = new Random();

			int  time_in_hospital = rand.nextInt(3) + 1;
			time+= time_in_hospital;
			System.out.println("im ambulance "+my_resource.getLocalName() + " and i'll take "+ time + " and my speed is "+my_resource.getSpeed());
		
		return time;
	}
	
	public double distanceHospital(EmergencyMessage emergency) {
		return Math.sqrt(Math.pow(hospital.getX()-emergency.getX(), 2)+
				Math.pow(hospital.getY()-emergency.getY(), 2));
	}
	
	
	public class RequestEmergency extends Behaviour {
		
		private static final long serialVersionUID = 1L;
		private MessageTemplate mt; 	// The template to receive replies
		private int replies_cnt =0;
		private ArrayList <EmergencyMessage> emergency_positions= new ArrayList<EmergencyMessage>();
		
		private int step = 0;
		
		
		public EmergencyMessage calculate_higher_priority() {
			EmergencyMessage max=emergency_positions.get(0);
			for(int i=1;i<emergency_positions.size();i++) {
				if(emergency_positions.get(i).getPriority()>max.getPriority())
					max=emergency_positions.get(i);
		}
			return max;
		}

		public void action() {
			switch (step) {
			case 0:
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				System.out.println("there are "+ my_resource.getEmergencyAgents().length +" active emergencies.\n");
				for(int i=0;i<my_resource.getEmergencyAgents().length;i++)
					cfp.addReceiver(my_resource.getEmergencyAgents()[i]);

				cfp.setConversationId("emergency");
				cfp.setReplyWith("cfp"+System.currentTimeMillis());
				myAgent.send(cfp);

				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency"),
						MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				step = 1;
				break;
				
			case 1:
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
						try {
							if(!Ambulance.being_treated.contains(emergency_positions.add((EmergencyMessage) reply.getContentObject())))
							emergency_positions.add((EmergencyMessage) reply.getContentObject());
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						replies_cnt ++;
						
					}
					else if(reply.getPerformative() == ACLMessage.REFUSE)
						replies_cnt ++;
					
					if (replies_cnt >= my_resource.getEmergencyAgents().length && emergency_positions.size()>0) {
						step=2;	
					}
				}
				else {
					block();
				}
				break;
			case 2:
				pendent_emergency=calculate_higher_priority();
				
				ACLMessage inform_yes = new ACLMessage(ACLMessage.INFORM);
				inform_yes.setContent("yes");
				inform_yes.addReceiver(pendent_emergency.getSenderID());
				myAgent.send(inform_yes);
				
				ACLMessage inform_no = new ACLMessage(ACLMessage.INFORM);
				inform_no.setContent("no");
				
				for(int i=0;i<my_resource.getEmergencyAgents().length;i++) {
					if(!my_resource.getEmergencyAgents()[i].equals(pendent_emergency.getSenderID()))
					inform_no.addReceiver(my_resource.getEmergencyAgents()[i]);
				}
				
				myAgent.send(inform_no);
				
				
				
				Ambulance.being_treated.add(pendent_emergency);
				myAgent.addBehaviour(new InformAmbulances());
				emergency_positions= new ArrayList<EmergencyMessage>();
				replies_cnt=0;
				
				step = 3; 
				
			}
		}

		@Override
		public boolean done() {
			return(step>2);
		}

	}
		
	public class InformAmbulances extends Behaviour{

		private static final long serialVersionUID = 1L;
		private MessageTemplate mt;
		private int step = 0;
		private ArrayList<ACLMessage> resources_msg= new ArrayList<ACLMessage>();
		AID best_agent = my_resource.getAID();
	
		private int replies_cnt =0;
		
		@Override
		public void action() {
			
			System.out.println("im ambulance "+myAgent.getLocalName() + " and i'm serving the emergencie "+ pendent_emergency.getSenderID().getLocalName());
			switch (step) {
			case 0:
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				AID[] resource_agents = my_resource.getResourceAgents();
				
					if(resource_agents.length==1) {
						step=3;
						break;
					}
				for (int i = 0; i < resource_agents.length; ++i) {
					if(!resource_agents[i].equals(myAgent.getAID()))
					cfp.addReceiver(resource_agents[i]);
				} 

				cfp.setConversationId("emergency_inf");
				cfp.setReplyWith("cfp"+System.currentTimeMillis()); // Unique value
				try {
					cfp.setContentObject(pendent_emergency);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				myAgent.send(cfp);
				
				step = 1;
				break;
			case 1:
				mt = MessageTemplate.and(MessageTemplate.MatchConversationId("emergency_inf"),
						MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
				
				ACLMessage reply = myAgent.receive(mt);
				if (reply != null) {

					// Reply received
					if (reply.getPerformative() == ACLMessage.PROPOSE) {
				
						resources_msg.add(reply);
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

				double time = calculateTime(my_resource.getMessage(),pendent_emergency);
				
				for(int i=0; i<resources_msg.size();i++) {
					if(Double.parseDouble(resources_msg.get(i).getContent()) < time) {
						time=Double.parseDouble(resources_msg.get(i).getContent());
						best_agent=resources_msg.get(i).getSender();
					}
				}
				
				System.out.println("\nResource " + best_agent.getName() + " will attend emergency " + pendent_emergency.getSenderID().getName());
				
					if(best_agent.equals(myAgent.getAID())){
						step=3;
					}
					else {
						step=4;
					}
				
				
				break;
				
			case 3:
				ACLMessage ref = new ACLMessage(ACLMessage.REFUSE);

				for (int i = 0; i < resources_msg.size(); ++i) {
					if(!resources_msg.get(i).getSender().equals(myAgent.getAID()))
						ref.addReceiver(resources_msg.get(i).getSender());
				} 

				ref.setConversationId("emergency_inf");
				
				
				ref.setReplyWith("ref"+System.currentTimeMillis()); // Unique value
				myAgent.send(ref);
				
				step=5;
				current_emergency = pendent_emergency;
				myAgent.addBehaviour(new EmergencyServer());
				
				break;
			case 4:				
				// Send the information to all resources
				ACLMessage ref2 = new ACLMessage(ACLMessage.REFUSE);
				ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				for (int i = 0; i < resources_msg.size(); ++i) {
					if(!resources_msg.get(i).getSender().equals(myAgent.getAID()) && !resources_msg.get(i).getSender().equals(best_agent)) {
						ref2.addReceiver(resources_msg.get(i).getSender());
					}
					else if(resources_msg.get(i).getSender().equals(best_agent)) {
						accept.addReceiver(resources_msg.get(i).getSender());
					}
				} 

				ref2.setConversationId("emergency_inf");
				myAgent.send(ref2);
				accept.setConversationId("emergency_inf");
				
				
				try {
					accept.setContentObject(pendent_emergency);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				myAgent.send(accept);
				
				step=5;
				break;
			default:
				break;
			}
		}

		@Override
		public boolean done() {
			return (step>4);
		}
		

		
		
	}
	
	public class AmbulanceRequestsServer extends CyclicBehaviour{
		
		private int step=0;

		@Override
		public void action() {
			switch (step) {
			case 0:
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
				ACLMessage msg = myAgent.receive(mt);
		
				if (msg != null ) {
					
					System.out.println("CFP Received");
					ACLMessage reply = msg.createReply();
					
					EmergencyMessage emergency_position = new EmergencyMessage();
					
					try {
						emergency_position = (EmergencyMessage) msg.getContentObject();
					} catch (UnreadableException e1) {
						e1.printStackTrace();
					}
					
					double time = calculateTime(my_resource.getMessage(),emergency_position);
						
					reply.setPerformative(ACLMessage.PROPOSE);
					reply.setContent(Double.toString(time));
					
					myAgent.send(reply);
					
					step =1;
				}
				else {
					block();
				}
				break;
			case 1:
				mt = MessageTemplate.MatchConversationId("emergency_inf");
				
				msg = myAgent.receive(mt);
				if (msg != null) {
					
					if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
						try {
							current_emergency = (EmergencyMessage) msg.getContentObject();
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
						myAgent.addBehaviour(new EmergencyServer());
						step=0;
		
					}else
						step=0;
				}
				else {
					block();
				}
				break;
			}
				
			
		}
		
	}
	
	public class EmergencyServer extends Behaviour{
		
		int step =0;

		@Override
		public void action() {
			ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
			
			accept.addReceiver(current_emergency.getSenderID());
			myAgent.send(accept);
			my_resource.updateAmbulancePosition(hospital.getX(), hospital.getY());
			
			start_time = System.currentTimeMillis();
			total_time = (long) calculateTime(my_resource.getMessage(),current_emergency);
			
			myAgent.addBehaviour(new TickerBehaviour(myAgent, total_time*1000) {
				protected void onTick() {
					System.out.println("Emergency " + current_emergency.getSenderID().getLocalName() + " served.\n");
					current_emergency = null;
					myAgent.removeBehaviour(this);
					
				}
			});
			
			step =1;
		}

		@Override
		public boolean done() {
			return (step>0);
		}
		
	}

}
