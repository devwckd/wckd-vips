package co.wckd.ranks.entity.rank;

import co.wckd.ranks.event.PlayerLoseRankTimeEvent;
import co.wckd.ranks.event.PlayerRankExpireEvent;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Setter
@Getter
public class RankPlayer {

    private final UUID uuid;
    private final Map<String, Rank> ranks;
    private Rank active;
    private long joined;

    public RankPlayer(UUID uuid) {
        this.uuid = uuid;
        this.ranks = new ConcurrentHashMap<>();
        this.active = null;
        this.joined = System.currentTimeMillis();
    }

    public boolean hasRank(RankType type) {
        return hasRank(type.getIdentifier());
    }

    public boolean hasRank(String identifier) {
        return ranks.containsKey(identifier.toLowerCase());
    }

    public Rank getRank(RankType type) {
        return getRank(type.getIdentifier());
    }

    public Rank getRank(String identifier) {
        return ranks.get(identifier);
    }

    public void addRank(Rank rank) {
        addRank(rank.getType().getIdentifier(), rank);
    }

    public void addRank(String identifier, Rank rank) {
        if (active == null) active = rank;

        rank.onActivate(this);
        if (ranks.containsKey(identifier)) {
            Rank containRank = ranks.get(identifier);
            if (containRank.getTime() == -1) return;
            containRank.increaseTime(rank.getTime());
            return;
        }

        ranks.put(identifier, rank);
    }

    public void removeRank(Rank rank) {
        removeRank(rank.getType(), rank.getTime());
    }

    public void removeRank(RankType rankType, long time) {
        removeRank(rankType.getIdentifier(), time);
    }

    public void removeRank(String identifier, long time) {

        Rank rank = ranks.get(identifier);
        if (rank == null) return;

        if (time == -1 || rank.getTime() - time < 1) {

            ranks.remove(identifier);
            rank.onChangedFrom(this);

            if (rank.equals(active)) {
                if (ranks.size() > 0) {
                    active = ranks.values().iterator().next();
                    active.onChangeTo(this);
                    return;
                }
                active = null;
            }

        }
    }

    public void addRanks(Map<String, Rank> rankMap) {
        ranks.putAll(rankMap);
    }

    public void setActive(String identifier) {
        if (!ranks.containsKey(identifier)) return;

        active.onChangedFrom(this);
        active = ranks.get(identifier);
        active.onChangeTo(this);
    }

    public void setActiveRaw(String identifier) {
        if (!ranks.containsKey(identifier)) return;

        active = ranks.get(identifier);
    }

    public void tick(long now, long time) {

        if (active == null || active.getTime() == -1) return;

        active.reduceTime(Math.min(now - joined, time * 1000));

        if (active.getTime() < 1) {
            PlayerRankExpireEvent expireEvent = new PlayerRankExpireEvent(
                    Bukkit.getPlayer(uuid),
                    this,
                    active.getType(),
                    PlayerLoseRankTimeEvent.Source.TIME,
                    0,
                    null,
                    60);

            Bukkit.getPluginManager().callEvent(expireEvent);

            if (expireEvent.isCancelled()) return;

            removeRank(active);

        }
    }

}
