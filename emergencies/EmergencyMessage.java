package emergencies;

import java.io.Serializable;

import jade.core.AID;

public class EmergencyMessage implements Serializable {
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
	
	public EmergencyMessage(int priority, int x,int y, AID id ) {
		this.priority = priority;
		this.x = x;
		this.y = y;
		this.senderID=id;
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
