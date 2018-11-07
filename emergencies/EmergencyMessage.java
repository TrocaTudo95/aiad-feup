package emergencies;

import java.io.Serializable;

import jade.core.AID;

public class EmergencyMessage implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EmergencyMessage other = (EmergencyMessage) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	private int priority;
	private int x;
	private int y;
	private AID senderID;
	private int time_to_respond;
	
	public int getTime_to_respond() {
		return time_to_respond;
	}

	public void setTime_to_respond(int time_to_respond) {
		this.time_to_respond = time_to_respond;
	}

	public AID getSenderID() {
		return senderID;
	}

	public EmergencyMessage(int priority, int x,int y, AID id ) {
		this.priority = priority;
		this.x = x;
		this.y = y;
		this.senderID=id;
	}
	
	public EmergencyMessage() {
		this.priority = 0;
		this.x = 0;
		this.y = 0;
		this.senderID= null;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int priority) {
		this.priority = priority;
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	
}
