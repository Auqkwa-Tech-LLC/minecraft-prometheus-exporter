package de.sldk.mc.metrics;

import com.massivecraft.factions.Faction;
import com.massivecraft.factions.Factions;
import io.prometheus.client.Gauge;
import org.bukkit.plugin.Plugin;

public class FactionsStatistic extends Metric {

    private static final Gauge PLAYERS_WITH_NAMES = Gauge.build()
            .name(prefix("factions"))
            .help("Online state of factions")
            .labelNames("tag", "being_raided")
            .create();


    public FactionsStatistic(Plugin plugin) {
        super(plugin, PLAYERS_WITH_NAMES);
    }

    @Override
    protected void doCollect() {
        for (Faction faction : Factions.getInstance().getAllFactions()) {
            PLAYERS_WITH_NAMES.labels(faction.getTag(), String.valueOf(faction.isBeingRaided())).set(faction.getFPlayersWhereOnline(true).size());
        }
    }
}
