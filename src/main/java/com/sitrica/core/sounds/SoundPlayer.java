package com.sitrica.core.sounds;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SoundPlayer {

	private final Set<SourSound> sounds = new HashSet<>();
	private final JavaPlugin instance;

	public SoundPlayer(JavaPlugin instance, ConfigurationSection section) {
		this.instance = instance;
		if (!section.getBoolean("enabled", true))
			return;
		section = section.getConfigurationSection("sounds");
		for (String node : section.getKeys(false)) {
			this.sounds.add(new SourSound(section.getConfigurationSection(node), "CLICK"));
		}
	}

	public SoundPlayer(JavaPlugin instance, Collection<SourSound> sounds) {
		this.sounds.addAll(sounds);
		this.instance = instance;
	}

	private List<SourSound> getSorted() {
		return sounds.parallelStream()
				.sorted(Comparator.comparing(SourSound::getDelay))
				.collect(Collectors.toList());
	}

	public void playAt(Location... locations) {
		if (sounds.isEmpty())
			return;
		for (SourSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playAt(locations);
				}
			}, sound.getDelay());
		}
	}

	public void playTo(Player... player) {
		if (sounds.isEmpty())
			return;
		for (SourSound sound : getSorted()) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(instance, new Runnable() {
				@Override
				public void run() {
					sound.playTo(player);
				}
			}, sound.getDelay());
		}
	}

}
