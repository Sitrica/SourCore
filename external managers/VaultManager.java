package com.sitrica.core.manager.external;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import com.sitrica.core.SourPlugin;
import com.sitrica.core.manager.ExternalManager;

public class VaultManager extends ExternalManager {

	private Economy economy;

	public VaultManager(SourPlugin instance) {
		super(instance, "vault", false);
		if (!Bukkit.getServer().getPluginManager().isPluginEnabled("Vault"))
			return;
		RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (rsp != null) {
			economy = rsp.getProvider();
		}
	}

	public void withdraw(OfflinePlayer player, double amount) {
		if (economy != null)
			economy.withdrawPlayer(player, amount);
	}	

	public void deposit(OfflinePlayer player, double amount) {
		if (economy != null)
			economy.depositPlayer(player, amount);
	}

	public double getBalance(OfflinePlayer player) {
		if (economy == null)
			return 0;
		return economy.getBalance(player);
	}

	public Economy getEconomy() {
		return economy;
	}

	@Override
	public boolean isEnabled() {
		return economy != null;
	}

}
