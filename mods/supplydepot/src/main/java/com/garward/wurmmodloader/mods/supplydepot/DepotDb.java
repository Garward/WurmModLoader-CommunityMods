package com.garward.wurmmodloader.mods.supplydepot;

import com.garward.wurmmodloader.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class DepotDb {

    private static final Logger logger = Logger.getLogger(DepotDb.class.getName());
    private static final String TIMER_ID = "DEPOT";

    static void initialize() {
        try (Connection dbcon = ModSupportDb.getModSupportDb()) {
            try (PreparedStatement create = dbcon.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS ObjectiveTimers (ID TEXT PRIMARY KEY, TIMER INTEGER)")) {
                create.executeUpdate();
            }
            try (PreparedStatement select = dbcon.prepareStatement(
                    "SELECT TIMER FROM ObjectiveTimers WHERE ID = ?")) {
                select.setString(1, TIMER_ID);
                try (ResultSet rs = select.executeQuery()) {
                    if (rs.next()) {
                        DepotState.lastSpawnedDepot = rs.getLong("TIMER");
                    } else {
                        try (PreparedStatement insert = dbcon.prepareStatement(
                                "INSERT INTO ObjectiveTimers (ID, TIMER) VALUES (?, 0)")) {
                            insert.setString(1, TIMER_ID);
                            insert.executeUpdate();
                        }
                        DepotState.lastSpawnedDepot = 0L;
                    }
                }
            }
            logger.info("[supplydepot] initialized timer: " + DepotState.lastSpawnedDepot);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[supplydepot] failed to initialize timer", e);
        }
    }

    static void updateLastSpawnedDepot() {
        long now = System.currentTimeMillis();
        DepotState.lastSpawnedDepot = now;
        try (Connection dbcon = ModSupportDb.getModSupportDb();
             PreparedStatement ps = dbcon.prepareStatement(
                     "UPDATE ObjectiveTimers SET TIMER = ? WHERE ID = ?")) {
            ps.setLong(1, now);
            ps.setString(2, TIMER_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "[supplydepot] failed to persist timer", e);
        }
    }

    private DepotDb() {}
}
