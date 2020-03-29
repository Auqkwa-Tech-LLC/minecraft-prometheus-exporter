package de.sldk.mc.metrics;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.prometheus.client.Gauge;
import org.bukkit.plugin.Plugin;

public class FactionsStatistic extends Metric {

    private static final Gauge FACTIONS = Gauge.build()
            .name(prefix("factions"))
            .help("Online state of factions")
            .labelNames("tag", "being_raided")
            .create();


    public FactionsStatistic(Plugin plugin) {
        super(plugin, FACTIONS);
    }

    @Override
    protected void doCollect() {
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            FACTIONS.labels(faction.getTag(), String.valueOf(faction.isBeingRaided())).set(faction.getFPlayersWhereOnline(true).size());
        }
    }
}
