package me.Wiedzmin137.wheroesaddon;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class Requirement {
	private String reqName;
    private String description = "";
	private String reqAuthor;
    private String[] reqIdentifiers = new String[0];
    
    public final Map<String,Object> datamap = new HashMap<String, Object>();
	
	public Requirement(String name) {
		this.reqName = name;
	}
	
    public abstract boolean isRequirementPassed(Player player, Map<String, Object> map);
    public abstract void init();

	public String getName() { return reqName; }
    public String getDescription() { return description; }
    public String getAuthor() { return reqAuthor; }
    public String[] getIdentifiers() { return reqIdentifiers; }
    
	public void setName(String name) { this.reqName = name; }
	public void setDescription(String description) { this.description = description; }
	public void setAuthor(String author) { this.reqAuthor = author; }
	public void setIdentifiers(String... identifiers) { this.reqIdentifiers = identifiers; };
	
	public void addData(String name) { datamap.put(name, null); }
	
}
