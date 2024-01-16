package robot.runner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

import org.hibernate.mapping.Array;

import com.google.flatbuffers.FlexBuffers.Map;

import battlecode.common.*;
import robot.Constants;
import robot.comms.SectorDiscovery;
import robot.comms.squad.SquadChannel1;
import robot.comms.squad.SquadChannel2;
import robot.comms.squad.SquadComms;
import robot.pathing.BruteMover;
import robot.state.MapSymmetry;
import robot.state.RobotState;
import robot.state.SpawnZones;
import robot.state.SquadOrder;
import robot.utils.BoundingBox;
import robot.utils.Sectors;

class ExplorationDirection {
    public final Direction direction;
    public final int firstSectorNumber;

    public ExplorationDirection(Direction direction, int sectorNumber) {
        this.direction = direction;
        this.firstSectorNumber = sectorNumber;
    }
}

enum GamePhase {
    SETUP, PREP_ATTACK, START_ATTACK, LATE;

    public static GamePhase getGamePhase(int turnNumber) {
        if (turnNumber < GameConstants.SETUP_ROUNDS - 30) {
            return GamePhase.SETUP;
        } else if (turnNumber < GameConstants.SETUP_ROUNDS) {
            return GamePhase.PREP_ATTACK;
        } else if (turnNumber == GameConstants.SETUP_ROUNDS) {
            return GamePhase.START_ATTACK;
        } else {
            return GamePhase.LATE;
        }
    }
}

public final class Captain {
    private static Stack<Integer> interestingSectors = new Stack<Integer>();

    public static void doTurn(RobotController rc, RobotState state, int turnNumber, BruteMover mover)
            throws GameActionException {
        final GamePhase gamePhase = GamePhase.getGamePhase(turnNumber);
        final MapLocation currentLocation = rc.getLocation();
        final boolean isSquadA = state.getSquadNumber() < 3;

        state.updateCurrentSectorNumber(currentLocation);

        if (state.getTurnsSinceSquadFormation() == 0) {
            SquadComms.cleanSquadChannels(rc, state.getSquadNumber());
            final int currentSectorNumber = state.getMapSectors().getSectorNumber(currentLocation);
            final int spawnZoneIndex = state.getInitialSpawnZone();
            final LinkedHashMap<Direction, Integer> validExploringDirections = getValidExploringDirections(
                    state.getSpawnZones(),
                    state.getMapSectors(),
                    spawnZoneIndex,
                    currentSectorNumber);

            final ArrayList<ArrayList<Direction>> splitDirections = splitDirections(validExploringDirections.keySet());

            if (splitDirections.size() == 1) {
                final ArrayList<Direction> directions = splitDirections.get(0);
                if (directions.size() == 1) {
                    interestingSectors.push(validExploringDirections.get(directions.get(0)));
                } else {
                    final int squadAPortion = (int) Math.ceil(directions.size() / 2.0);

                    final int startIndex = isSquadA ? 0 : squadAPortion;
                    final int endIndex = isSquadA ? squadAPortion : directions.size();

                    for (int i = startIndex; i < endIndex; i++) {
                        interestingSectors.push(validExploringDirections.get(directions.get(0)));
                    }
                }
            } else {
                final ArrayList<Direction> directions = splitDirections.get(isSquadA ? 0 : 1);
                for (Direction direction : directions) {
                    interestingSectors.push(validExploringDirections.get(direction));
                }
            }
        }

        final boolean isTargetSectorDiscovered = state.getTargetSectorNumber() == -1 || state.getMapSectors()
                .getSectorCenter(state.getTargetSectorNumber())
                .isWithinDistanceSquared(currentLocation, 2);

        if (gamePhase == GamePhase.SETUP && isTargetSectorDiscovered) {
            if (state.getTargetSectorNumber() != -1) {
                SectorDiscovery.markSectorAsDiscovered(rc, state.getTargetSectorNumber());
                final boolean[] discoveryMap = SectorDiscovery.getSectorDiscoveryMap(rc);

                final ArrayList<Integer> adjacentSectors = state.getMapSectors()
                        .getAdjacentSectors(state.getTargetSectorNumber());
                for (Integer sector : adjacentSectors) {
                    if (!discoveryMap[sector]) {
                        interestingSectors.push(sector);
                    }
                }
            }

            if (!interestingSectors.isEmpty()) {
                updateTargetSector(interestingSectors.pop(), rc, state, mover);
            }
        }

        if (gamePhase == GamePhase.PREP_ATTACK) {
            /// TODO: Move close to dam
        }

        if (gamePhase == GamePhase.START_ATTACK) {
            final MapLocation[] enemyFlagLocs = rc.senseBroadcastFlagLocations();
            final MapLocation targetEnemyFlagLoc = enemyFlagLocs[state.getInitialSpawnZone()];
            final int targetEnemyFlagSector = state.getMapSectors().getSectorNumber(targetEnemyFlagLoc);
            updateTargetSector(targetEnemyFlagSector, rc, state, mover);
        }

        mover.move();
    }

    private static void updateTargetSector(int nextSectorNumber, RobotController rc, RobotState state, BruteMover mover)
            throws GameActionException {

        final MapLocation nextSectorCenter = state.getMapSectors().getSectorCenter(nextSectorNumber);
        rc.setIndicatorLine(rc.getLocation(), nextSectorCenter, 0, 100, 159);

        SquadComms.writeChannel1(rc, state.getSquadNumber(), new SquadChannel1(SquadOrder.MOVE, nextSectorCenter));
        SquadComms.writeChannel2(rc, state.getSquadNumber(),
                new SquadChannel2(state.getCurrentSectorNumber(), nextSectorNumber));

        state.setTargetSectorNumber(nextSectorNumber);
        mover.setTarget(nextSectorCenter, 1);
    }

    private static LinkedHashMap<Direction, Integer> getValidExploringDirections(
            SpawnZones spawnZones,
            Sectors sectors,
            int spawnZoneIndex,
            int currentSectorNumber) {
        final LinkedHashMap<Direction, Integer> validDirections = new LinkedHashMap<Direction, Integer>();
        final BoundingBox[] spawnZonesBoxes = spawnZones.getZoneBoundingBoxes();
        final BoundingBox spawnZone = spawnZones.getZoneBoundingBoxes()[spawnZoneIndex];
        Direction invalidDirection1 = Direction.CENTER;
        Direction invalidDirection2 = Direction.CENTER;

        if (spawnZoneIndex == 0) {
            invalidDirection1 = spawnZone.getDirectionTo(spawnZonesBoxes[1]);
            invalidDirection2 = spawnZone.getDirectionTo(spawnZonesBoxes[2]);
        } else if (spawnZoneIndex == 1) {
            invalidDirection1 = spawnZone.getDirectionTo(spawnZonesBoxes[0]);
            invalidDirection2 = spawnZone.getDirectionTo(spawnZonesBoxes[2]);
        } else if (spawnZoneIndex == 2) {
            invalidDirection1 = spawnZone.getDirectionTo(spawnZonesBoxes[0]);
            invalidDirection2 = spawnZone.getDirectionTo(spawnZonesBoxes[1]);
        }

        for (Direction direction : Constants.DIRECTIONS) {
            if (direction == invalidDirection1 || direction == invalidDirection2 || direction == Direction.CENTER) {
                continue;
            }

            final Integer sector = sectors.getSectorAtDirection(currentSectorNumber, direction);
            if (sector == -1) {
                continue;
            }

            validDirections.put(direction, sector);
        }

        return validDirections;
    }

    private static ArrayList<ArrayList<Direction>> splitDirections(Iterable<Direction> directions) {
        final ArrayList<ArrayList<Direction>> splitDirections = new ArrayList<ArrayList<Direction>>();
        splitDirections.add(new ArrayList<Direction>());

        Direction prevDirection = null;
        int groupIndex = 0;

        for (Direction direction : directions) {
            if (groupIndex == 0 &&
                    prevDirection != null &&
                    Math.abs(prevDirection.getDirectionOrderNum() - direction.getDirectionOrderNum()) > 1) {
                groupIndex++;
                splitDirections.add(new ArrayList<Direction>());
            }

            splitDirections.get(0).add(direction);
        }

        return splitDirections;
    }

    private static Direction getRelativeDirectionInBoundingBox(BoundingBox boundingBox, MapLocation location) {
        final int x = location.x;
        final int y = location.y;

        final int equator = boundingBox.south + (boundingBox.north - boundingBox.south) / 2;
        final int primeMeridian = boundingBox.east + (boundingBox.east - boundingBox.west) / 2;

        if (y > equator) {
            if (x > primeMeridian) {
                return Direction.NORTHEAST;
            } else {
                return Direction.NORTHWEST;
            }
        } else {
            if (x > primeMeridian) {
                return Direction.SOUTHEAST;
            } else {
                return Direction.SOUTHWEST;
            }
        }
    }

}
