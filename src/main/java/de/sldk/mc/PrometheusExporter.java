package de.sldk.mc;

import de.sldk.mc.config.PrometheusExporterConfig;
import de.sldk.mc.metrics.ActiveBans;
import de.sldk.mc.metrics.FactionsStatistic;
import litebans.api.Database;
import litebans.api.Entry;
import litebans.api.Events;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Server;

import java.net.InetSocketAddress;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class PrometheusExporter extends JavaPlugin {

    private final PrometheusExporterConfig config = new PrometheusExporterConfig(this);
    private Server server;
    public static long banCount = 0;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("Factions") != null) {
            PrometheusExporterConfig.METRICS.add(PrometheusExporterConfig.metricConfig("faction_statistic", true, FactionsStatistic::new));
        }
        if (getServer().getPluginManager().getPlugin("LiteBans") != null) {
            PrometheusExporterConfig.METRICS.add((PrometheusExporterConfig.metricConfig("ban_statistic", true, ActiveBans::new)));
        }
        config.loadDefaultsAndSave();

        config.enableConfiguredMetrics();

        banCount = initalCount();
        registerEvents();

        serveMetrics();
    }

    private void serveMetrics() {
        int port = config.get(PrometheusExporterConfig.PORT);
        String host = config.get(PrometheusExporterConfig.HOST);
        InetSocketAddress address = new InetSocketAddress(host, port);
        server = new Server(address);
        server.setHandler(new MetricsController(this));

        try {
            server.start();
            getLogger().info("Started Prometheus metrics endpoint at: " + host + ":" + port);

        } catch (Exception e) {
            getLogger().severe("Could not start embedded Jetty server");
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (server != null) {
            try {
                server.stop();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Failed to stop metrics server gracefully: " + e.getMessage());
                getLogger().log(Level.FINE, "Failed to stop metrics server gracefully", e);
            }
        }
    }
    private void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                if (entry.getType().equals("ban")) {
                    banCount++;
                }
             }

            @Override
            public void entryRemoved(Entry entry) {
                if (entry.getType().equals("ban")) {
                    banCount--;
                }
            }
        });
    }
    private long initalCount() {
        String query = "SELECT COUNT(*) FROM {bans} WHERE `active` = '1'";
        try (PreparedStatement st = Database.get().prepareStatement(query)) {
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return banCount = rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
