package com.sitrica.core.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.plugin.IllegalPluginAccessException;
import com.sitrica.core.SourPlugin;
import com.sitrica.core.utils.Utils;

public class ManagerHandler {

	private final Set<ExternalManager> externalManagers = new HashSet<>();
	private final List<Manager> managers = new ArrayList<>();

	public ManagerHandler(SourPlugin instance) {
		for (String packageName : instance.getManagerPackages()) {
			for (Class<Manager> clazz : Utils.getClassesOf(instance, packageName, Manager.class)) {
				if (clazz == Manager.class)
					continue;
				try {
					managers.add(clazz.newInstance());
					instance.debugMessage("Registered manager " + clazz.getName());
				} catch (InstantiationException | IllegalAccessException e) {
					instance.consoleMessage("&dManager " + clazz.getName() + " doesn't have a nullable constructor.");
					e.printStackTrace();
					continue;
				}
			}
		}
		for (Manager manager : managers) {
			if (!manager.hasListener())
				continue;
			try {
				Bukkit.getPluginManager().registerEvents(manager, instance);
			} catch (IllegalPluginAccessException e) {
				instance.consoleMessage("&dFailed to register listener for manager: " + manager.getClass().getName());
				e.printStackTrace();
			}
		}
		Bukkit.getScheduler().runTaskLaterAsynchronously(instance, () -> {
			for (Manager manager : managers)
				manager.afterInitialize();
		}, 1);
	}

	@SuppressWarnings("unchecked")
	public <T extends ExternalManager> T getExternalManager(Class<T> clazz) {
		for (ExternalManager manager : externalManagers) {
			if (manager.getClass().equals(clazz))
				return (T) manager;
		}
		try {
			T manager = clazz.newInstance();
			externalManagers.add(manager);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public <M extends Manager> M getManager(Class<M> clazz) {
		for (Manager manager : managers) {
			if (manager.getClass().equals(clazz))
				return (M) manager;
		}
		try {
			M manager = clazz.newInstance();
			managers.add(manager);
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void registerManager(Manager manager) {
		if (!managers.contains(manager))
			managers.add(manager);
	}

	public Set<ExternalManager> getExternalManagers() {
		return externalManagers;
	}

	public List<Manager> getManagers() {
		return managers;
	}

}
