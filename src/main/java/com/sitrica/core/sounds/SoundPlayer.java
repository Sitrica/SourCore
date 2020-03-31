package com.sitrica.core.sounds;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.sitrica.core.SourPlugin;

public class SoundPlayer {

	private final Set<SourSound> sounds = new HashSet<>();
	private final SourPlugin instance;

	public SoundPlayer(SourPlugin instance, String node) {
		this.instance = instance;
		Optional<FileConfiguration> configuration = instance.getConfiguration("sounds");
		if (!configuration.isPresent())
			return;
		ConfigurationSection section = configuration.get().getConfigurationSection(node);
		if (!section.getBoolean("enabled", true))
			return;
		section = section.getConfigurationSection("sounds");
		for (String key : section.getKeys(false)) {
			this.sounds.add(new SourSound(section.getConfigurationSection(key), "CLICK"));
		}
	}

	public SoundPlayer(SourPlugin instance, ConfigurationSection section) {
		this.instance = instance;
		if (!section.getBoolean("enabled", true))
			return;
		section = section.getConfigurationSection("sounds");
		for (String node : section.getKeys(false)) {
			this.sounds.add(new SourSound(section.getConfigurationSection(node), "CLICK"));
		}
	}

	public SoundPlayer(SourPlugin instance, Collection<SourSound> sounds) {
		this.sounds.addAll(sounds);
		this.instance = instance;
	}

	private List<SourSound> getSorted() {
		return sounds.parallelStream()
				.sorted(Comparator.comparing(SourSound::getDelay))
				.collect(Collectors.toList());
	}

	public void playAt(Collection<Location> locations) {
		playAt(locations.toArray(new Location[locations.size()]));		
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

	public void playTo(Collection<Player> players) {
		playTo(players.toArray(new Player[players.size()]));		
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
