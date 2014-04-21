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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Module {
	public final Map<String, Requirement> modules;
    private Map<String, File> moduleFiles;
    private final WAddonCore plugin;
    private final File dir;
    private final ClassLoader classLoader;
    
    //public final List<Requirement> customRequirements = new LinkedList<Requirement>();
    public List<String> customRequirements = new LinkedList<String>();
    public Map<String, Map<String, Object>> customRequirementsHM = new HashMap<String, Map<String, Object>>();

    public Module(WAddonCore plugin) {
    	modules = new LinkedHashMap<String, Requirement>();
    	moduleFiles = new HashMap<String, File>();
    	this.plugin = plugin;
    	dir = new File(plugin.getDataFolder(), "modules");
    	dir.mkdir();

    	List<URL> urls = new ArrayList<URL>();
    	for (String reqFile : dir.list()) {
    		if (reqFile.contains(".jar")) {
    			File file = new File(dir, reqFile);
    			String name = reqFile.toLowerCase().replace(".jar", "").replace("Requirement", "");
    			if (moduleFiles.containsKey(name)) {
    				WAddonCore.Log.severe("Duplicate jar found! Please remove " + reqFile + " or " + moduleFiles.get(name).getName());
    				continue;
    			}
    			moduleFiles.put(name, file);
    			try {
    				urls.add(file.toURI().toURL());
    			} catch (MalformedURLException e) {
    				e.printStackTrace();
    			}
    		} else {
                WAddonCore.Log.info("[WHeroesAddon] Requirement" + reqFile + "has not been loaded");
    		}
    	}
    	ClassLoader cl = plugin.getClass().getClassLoader();
    	classLoader = URLClassLoader.newInstance(urls.toArray(new URL[urls.size()]), cl);
    }
    
    public Requirement getModules(String name) {
    	if (name == null) {
    		return null;
    	}
   	    // Only attempt to load files that exist
        else if (!isLoaded(name) && moduleFiles.containsKey(name.toLowerCase())) {
        	loadModule(name);
        }
        return modules.get(name.toLowerCase());
    }
    
    public Collection<Requirement> getModules() {
        return Collections.unmodifiableCollection(modules.values());
    }
    
    public boolean isLoaded(String name) {
        return modules.containsKey(name.toLowerCase());
    }
    
    public Requirement loadModule(File file) {
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
                customRequirements.add(req.getName());
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

    public void loadModules() {
        for (Entry<String, File> entry : moduleFiles.entrySet()) {
            // if the Requirement is already loaded, skip it
            if (isLoaded(entry.getKey())) {
                continue;
            }

            Requirement req = loadModule(entry.getValue());
            if (req != null) {
                addRequirement(req);
            }
        }
    }
    
    private boolean loadModule(String name) {
        // If the Requirement is already loaded, don't try to load it
        if (isLoaded(name)) {
            return true;
        }

        // Lets try loading the Requirement file
        Requirement req = loadModule(moduleFiles.get(name.toLowerCase()));
        if (req == null) {
            return false;
        }

        addRequirement(req);
        return true;
    }
    
    public void addRequirement(Requirement req) {
    	modules.put(req.getName().toLowerCase().replace("Requirement", ""), req);
    }
}
