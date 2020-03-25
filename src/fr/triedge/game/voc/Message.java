package fr.triedge.game.voc;

public class Message {

	public String code;
	public String[] elements;
	private String sep = "_:_";
	
	public Message(String str) {
		String[] e = str.split(sep);
		this.code = e[0];
		elements = new String[e.length-1];
		for (int i = 1; i <e.length; ++i) {
			elements[i-1] = e[i];
		}
	}
	
	public Message(String code, String... elements) {
		this.code = code;
		this.elements = elements;
	}
	
	@Override
	public String toString() {
		StringBuilder tmp = new StringBuilder();
		tmp.append(this.code);
		for (int i = 0; i<this.elements.length; ++i) {
			tmp.append(this.sep);
			tmp.append(this.elements[i]);
		}
		return tmp.toString();
	}
}
