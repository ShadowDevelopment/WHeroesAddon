package me.Wiedzmin137.wheroesaddon;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class AddonsFileManager {	
    private Map<String, Requirement> requirements;
    private Map<String, Requirement> identifiers;
    private Map<String, File> requirementFiles;
    private final WAddonCore plugin;
    private final File dir;
    private final ClassLoader classLoader;

    public AddonsFileManager(WAddonCore plugin) {
    	requirements = new LinkedHashMap<String, Requirement>();
    	identifiers = new HashMap<String, Requirement>();
    	requirementFiles = new HashMap<String, File>();
    	this.plugin = plugin;
    	dir = new File(plugin.getDataFolder(), "modules");
    	dir.mkdir();

    	List<URL> urls = new ArrayList<URL>();
    	for (String reqFile : dir.list()) {
    		if (reqFile.contains(".jar")) {
    			File file = new File(dir, reqFile);
    			String name = reqFile.toLowerCase().replace(".jar", "").replace("Requirement", "");
    			if (requirementFiles.containsKey(name)) {
    				WAddonCore.Log.severe("Duplicate jar found! Please remove " + reqFile + " or " + requirementFiles.get(name).getName());
    				continue;
    			}
    			requirementFiles.put(name, file);
    			try {
    				urls.add(file.toURI().toURL());
    			} catch (MalformedURLException e) {
    				e.printStackTrace();
    			}
    		}
    	}
    	ClassLoader cl = plugin.getClass().getClassLoader();
    	classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), cl);
    }
    
    public Requirement getRequirement(String name) {
    	if (name == null) {
    		return null;
    	}
   	    // Only attempt to load files that exist
        else if (!isLoaded(name) && requirementFiles.containsKey(name.toLowerCase())) {
        	loadRequirement(name);
        }
        return requirements.get(name.toLowerCase());
    }
    
    public Collection<Requirement> getRequirements() {
        return Collections.unmodifiableCollection(requirements.values());
    }
    
    public boolean isLoaded(String name) {
        return requirements.containsKey(name.toLowerCase());
    }
    
    public Requirement loadRequirement(File file) {
        try {
            @SuppressWarnings("resource")
			JarFile jarFile = new JarFile(file);
            Enumeration<JarEntry> entries = jarFile.entries();

            String mainClass = null;
            while (entries.hasMoreElements()) {
                JarEntry element = entries.nextElement();
                if (element.getName().equalsIgnoreCase("addon.info")) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(jarFile.getInputStream(element)));
                    mainClass = reader.readLine().substring(12);
                    break;
                }
            }

            if (mainClass != null) {
                Class<?> clazz = Class.forName(mainClass, true, classLoader);
                Class<? extends Requirement> reqClass = clazz.asSubclass(Requirement.class);
                java.lang.reflect.Constructor<? extends Requirement> ctor = reqClass.getConstructor(plugin.getClass());
                Requirement req = ctor.newInstance(plugin);
                req.init();
                WAddonCore.Log.info("[WHeroesAddon] Requirement" + req.getName() + "has been loaded");
                return req;
            } else {
                throw new Exception();
            }
        } catch (Exception e) {
            e.printStackTrace();
            WAddonCore.Log.info("The Requirement " + file.getName() + " failed to load");
            return null;
        }
    }

    public void loadRequirements() {
        for (Entry<String, File> entry : requirementFiles.entrySet()) {
            // if the Requirement is already loaded, skip it
            if (isLoaded(entry.getKey())) {
                continue;
            }

            Requirement req = loadRequirement(entry.getValue());
            if (req != null) {
                addRequirement(req);
                WAddonCore.Log.info("Requirement " + req.getName() + " Loaded");
            }
        }
    }
    
    private boolean loadRequirement(String name) {
        // If the Requirement is already loaded, don't try to load it
        if (isLoaded(name)) {
            return true;
        }

        // Lets try loading the Requirement file
        Requirement req = loadRequirement(requirementFiles.get(name.toLowerCase()));
        if (req == null) {
            return false;
        }

        addRequirement(req);
        return true;
    }
    
    public void addRequirement(Requirement req) {
    	requirements.put(req.getName().toLowerCase().replace("requirement", ""), req);
    	for (String ident : req.getIdentifiers()) {
   	        identifiers.put(ident.toLowerCase(), req);
    	}
    }
}
