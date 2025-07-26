package org.novania.eventpvp;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.novania.eventpvp.commands.EventCommand;
import org.novania.eventpvp.commands.EventPVPCommands;
import org.novania.eventpvp.database.DatabaseManager;
import org.novania.eventpvp.integrations.TheGlowIntegration;
import org.novania.eventpvp.listeners.GuiListener;
import org.novania.eventpvp.listeners.PlayerListener;
import org.novania.eventpvp.listeners.WorldListener;
import org.novania.eventpvp.managers.ConfigManager;
import org.novania.eventpvp.managers.EventManager;
import org.novania.eventpvp.managers.KitManager;
import org.novania.eventpvp.managers.ScoreboardManager;
import org.novania.eventpvp.managers.TeamManager;

public class EventPVP extends JavaPlugin {
    
    private static EventPVP instance;
    private DatabaseManager databaseManager;
    private ConfigManager configManager;
    private TeamManager teamManager;
    private EventManager eventManager;
    private KitManager kitManager;
    private ScoreboardManager scoreboardManager;
    private TheGlowIntegration glowIntegration;
    private Economy economy;
    
    @Override
    public void onEnable() {
        instance = this;
        
        getLogger().info("Démarrage d'EventPVP...");
        
        // Vérifier les dépendances
        if (!checkDependencies()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Créer le dossier de configuration
        saveDefaultConfig();
        
        // Initialiser les composants
        this.configManager = new ConfigManager(this);
        this.databaseManager = new DatabaseManager(this);
        this.glowIntegration = new TheGlowIntegration(this);
        this.teamManager = new TeamManager(this, glowIntegration);
        this.kitManager = new KitManager(this);
        this.eventManager = new EventManager(this, databaseManager, teamManager);
        this.scoreboardManager = new ScoreboardManager(this, eventManager);
        
        // Initialiser la base de données
        if (!databaseManager.initDatabase()) {
            getLogger().severe("Impossible d'initialiser la base de données!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Vérifier que le monde event existe
        if (getServer().getWorld(configManager.getWorldName()) == null) {
            getLogger().warning("Le monde '" + configManager.getWorldName() + "' n'existe pas!");
            getLogger().warning("EventPVP fonctionnera en mode réduit.");
        }
        
        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new WorldListener(this, teamManager), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this, eventManager, teamManager), this);
        getServer().getPluginManager().registerEvents(new GuiListener(this), this);
        
        // Enregistrer les commandes
        getCommand("event").setExecutor(new EventCommand(this));
        getCommand("eventpvp").setExecutor(new EventPVPCommands(this));
        
        // Démarrer le scoreboard
        if (configManager.isScoreboardEnabled()) {
            scoreboardManager.startUpdateTask();
        }
        
        getLogger().info("EventPVP activé avec succès!");
        getLogger().info("Monde event configuré: " + configManager.getWorldName());
        getLogger().info("Intégration TheGlow: " + (glowIntegration.isEnabled() ? "✓" : "✗"));
    }
    
    @Override
    public void onDisable() {
        if (scoreboardManager != null) {
            scoreboardManager.stopUpdateTask();
        }
        
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        
        getLogger().info("EventPVP désactivé!");
    }
    
    private boolean checkDependencies() {
        // Vérifier TheGlow (requis)
        if (getServer().getPluginManager().getPlugin("TheGlow") == null) {
            getLogger().severe("TheGlow est requis pour EventPVP!");
            return false;
        }
        
        // Vérifier Vault (optionnel)
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager()
                    .getRegistration(Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                getLogger().info("Économie configurée: " + economy.getName());
            }
        }
        
        return true;
    }
    
    public static EventPVP getInstance() {
        return instance;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public TeamManager getTeamManager() {
        return teamManager;
    }
    
    public EventManager getEventManager() {
        return eventManager;
    }
    
    public KitManager getKitManager() {
        return kitManager;
    }
    
    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
    
    public TheGlowIntegration getGlowIntegration() {
        return glowIntegration;
    }
    
    public Economy getEconomy() {
        return economy;
    }
    
    public boolean hasEconomy() {
        return economy != null;
    }
}