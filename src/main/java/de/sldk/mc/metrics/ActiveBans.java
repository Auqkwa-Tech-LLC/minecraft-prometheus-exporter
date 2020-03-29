package de.sldk.mc.metrics;

import de.sldk.mc.PrometheusExporter;
import io.prometheus.client.Gauge;
import org.bukkit.plugin.Plugin;

public class ActiveBans extends Metric {

    private static final Gauge FACTIONS = Gauge.build()
            .name(prefix("active_bans"))
            .help("Number of Active Bans")
            .labelNames()
            .create();

    public ActiveBans(Plugin plugin) {
        super(plugin, FACTIONS);
    }

    @Override
    protected void doCollect() {
        FACTIONS.labels().set(PrometheusExporter.banCount);
    }
}
