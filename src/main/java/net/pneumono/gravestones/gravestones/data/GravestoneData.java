package net.pneumono.gravestones.gravestones.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class GravestoneData {
    public List<PlayerGravestoneData> data;

    public GravestoneData() {
        this.data = new ArrayList<>();
    }

    public List<GravestonePosition> getPlayerGravePositions(UUID player) {
        for (PlayerGravestoneData playerData : data) {
            if (Objects.equals(playerData.getOwner().toString(), player.toString())) {
                return new ArrayList<>(playerData.getPositionsAsList());
            }
        }
        return null;
    }

    public PlayerGravestoneData getPlayerData(UUID player) {
        for (PlayerGravestoneData playerData : data) {
            if (Objects.equals(playerData.getOwner().toString(), player.toString())) {
                return playerData;
            }
        }
        return null;
    }

    public void setPlayerData(PlayerGravestoneData newPlayerData, UUID player, GravestonePosition pos) {
        for (PlayerGravestoneData playerData : data) {
            if (Objects.equals(playerData.getOwner().toString(), newPlayerData.owner.toString())) {
                data.remove(playerData);
                addPlayerDataSafely(newPlayerData, player, pos);
                return;
            }
        }

        addPlayerDataSafely(newPlayerData, player, pos);
    }

    private void addPlayerDataSafely(PlayerGravestoneData playerData, UUID player, GravestonePosition gravestonePos) {
        data.add(Objects.requireNonNullElseGet(
                playerData,
                () -> new PlayerGravestoneData(player, gravestonePos)
        ));
    }

    public boolean hasData() {
        return !data.isEmpty();
    }
}
