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
	private Pair<Integer, Integer> hospital=new Pair(2,3);
	
	EmergencyMessage  pendent_emergency;
	EmergencyMessage  current_emergency;
	long start_time = 0;
	long total_time = 0;
	
	public ResourceManager(Ambulance resource_agent) {
		this.my_resource= resource_agent;
	}
	
	public double calculateTime(EmergencyMessage ambulance_msg, EmergencyMessage emergency) {
		double time=0;
		double distance_to_emergency = Math.sqrt(Math.pow(ambulance_msg.getX()-emergency.getX(), 2)+Math.pow(ambulance_msg.getY()-emergency.getY(), 2));
		double distance_to_hospital = distanceHospital(emergency);
		time = (distance_to_emergency + distance_to_hospital) * my_resource.getSpeed();
			if(current_emergency!=null) {
				time += total_time - (start_time - System.currentTimeMillis())/1000;
			}
			Random rand = new Random();

			int  time_in_hospital = rand.nextInt(3) + 1;
			time+= time_in_hospital;
			
		
		return time;
	}
	
	public double distanceHospital(EmergencyMessage emergency) {
		return Math.sqrt(Math.pow(hospital.getX()-emergency.getX(), 2)+
				Math.pow(hospital.getY()-emergency.getY(), 2));
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
						
						try {
							pendent_emergency = (EmergencyMessage) reply.getContentObject();
						} catch (UnreadableException e) {
							e.printStackTrace();
						}
						
						System.out.println("Emergency " + pendent_emergency.getSenderID().getName() + " being alocated by " + myAgent.getName() + ".\n");
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
		private ArrayList<ACLMessage> resources_msg= new ArrayList<ACLMessage>();
		AID best_agent = my_resource.getAID();
	
		private int replies_cnt =0;
		
		@Override
		public void action() {
			switch (step) {
			case 0:
				
				ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				AID[] resource_agents = my_resource.getResourceAgents();
				
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
				
				System.out.println(myAgent.getName() + "|" + pendent_emergency.getSenderID().getName() + " - "+ time);

				for(int i=0; i<resources_msg.size();i++) {
					System.out.println(resources_msg.get(i).getSender().getName() + "|" + pendent_emergency.getSenderID().getName() + " - "+ Double.parseDouble(resources_msg.get(i).getContent()) );
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
			// TODO Auto-generated method stub
			return (step>4);
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
					
					EmergencyMessage emergency_position = new EmergencyMessage();
					
					try {
						emergency_position = (EmergencyMessage) msg.getContentObject();
					} catch (UnreadableException e1) {
						// TODO Auto-generated catch block
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
						
						step =2;
						myAgent.addBehaviour(new EmergencyServer());
		
					}else
						done();
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
			
			start_time = System.currentTimeMillis();
			total_time = (long) calculateTime(my_resource.getMessage(),current_emergency);
			
			myAgent.addBehaviour(new TickerBehaviour(myAgent, total_time*1000) {
				protected void onTick() {
					System.out.println("Emergency " + current_emergency.getSenderID().getName() + " served.\n");
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
