package com.garward.wurmmodloader.mods.titan;

import com.garward.wurmmodloader.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class TitanDb {

    private static final Logger logger = Logger.getLogger(TitanDb.class.getName());
    private static final String TIMER_ID = "TITAN";

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
                        TitanState.lastSpawnedTitan = rs.getLong("TIMER");
                    } else {
                        try (PreparedStatement insert = dbcon.prepareStatement(
                                "INSERT INTO ObjectiveTimers (ID, TIMER) VALUES (?, 0)")) {
                            insert.setString(1, TIMER_ID);
                            insert.executeUpdate();
                        }
                        TitanState.lastSpawnedTitan = 0L;
                    }
                }
            }
            logger.info("[titan] initialized timer: " + TitanState.lastSpawnedTitan);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "[titan] failed to initialize timer", e);
        }
    }

    static void updateLastSpawnedTitan() {
        long now = System.currentTimeMillis();
        TitanState.lastSpawnedTitan = now;
        try (Connection dbcon = ModSupportDb.getModSupportDb();
             PreparedStatement ps = dbcon.prepareStatement(
                     "UPDATE ObjectiveTimers SET TIMER = ? WHERE ID = ?")) {
            ps.setLong(1, now);
            ps.setString(2, TIMER_ID);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.log(Level.WARNING, "[titan] failed to persist timer", e);
        }
    }

    private TitanDb() {}
}
