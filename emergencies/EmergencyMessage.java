package emergencies;

import java.io.Serializable;

public class EmergencyMessage implements Serializable {

	private int priority;
	private int x;
	private int y;
	public EmergencyMessage(int priority, int x, int y) {
		this.priority = priority;
		this.x = x;
		this.y = y;
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
