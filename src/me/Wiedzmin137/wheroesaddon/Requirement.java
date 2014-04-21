package me.Wiedzmin137.wheroesaddon;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

public abstract class Requirement {
	private String reqName;
    private String description = "";
	private String reqAuthor;
	private boolean executeRequirement = false;
    
    public final Map<String,Object> datamap = new HashMap<String, Object>();
	
	public Requirement(WAddonCore plugin, String name) {
		this.reqName = name;
	}
	
    public abstract boolean isRequirementPassed(Player player, Map<String, Object> map);
    
    /**
     * This method execute Requirement
     * E.g. if Requirement needs you to have X money
     * you can take here this money
     */
	public void executeRequirement() {}
	

	public String getName() { return reqName; }
    public String getDescription() { return description; }
    public String getAuthor() { return reqAuthor; }
    
	public void setName(String name) { this.reqName = name; }
	public void setDescription(String description) { this.description = description; }
	public void setAuthor(String author) { this.reqAuthor = author; }
	
    public boolean isExecutingRequirement() { return executeRequirement; }
    public void setExecuteRequirement(boolean set) { executeRequirement = set; }
	
	public void addData(String name) { datamap.put(name, null); }
	
}
