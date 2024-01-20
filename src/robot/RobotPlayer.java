package robot;

import battlecode.common.*;
import robot.comms.squad.SquadComms;
import robot.pathing.BruteMover;
import robot.pathing.BugNav;
import robot.pathing.Cartographer;
import robot.runner.Captain;
import robot.runner.Grunt;
import robot.state.Formations;
import robot.state.RobotRole;
import robot.state.RobotState;
import robot.utils.BoundingBox;
import robot.utils.Offset;

import java.util.HashMap;
import java.util.Random;

public strictfp class RobotPlayer {
    static int turnCount = 0;

    static final Random rng = new Random();

    public static final RobotState state = new RobotState();

    static boolean isPrepGamePhase() {
        return turnCount < 200;
    }

    public static void run(RobotController rc) throws GameActionException {
        state.initialize(rc);

        final BruteMover mover = new BruteMover(rc);
        final BugNav bugNav = new BugNav(rc);
        final Cartographer cartographer = new Cartographer(state.getMapWidth(), state.getMapHeight());


        while (true) {
            turnCount += 1;
            try {
                rc.setIndicatorString(state.getSquadNumber() + " " + state.getRole() + " " + state.getSquad().size());

                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    if (state.getSquad().size() < Constants.SQUAD_SIZE - 1 &&
                            state.getSquadNumber() >= 0) {
                        discoverSquadMembers(rc);
                    }

                    if (state.getSquadNumber() >= 0 && state.getSquad().size() == Constants.SQUAD_SIZE - 1) {
                        state.incrementTurnsSinceSquadFormation();

                        if (state.getRole() == RobotRole.CAPTAIN) {
                            Captain.doTurn(rc, state, turnCount, bugNav, cartographer);
                        } else {
                            Grunt.doTurn(rc, state, turnCount, mover);
                        }
                    }
                }
            } catch (GameActionException e) {
                System.out.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                System.out.println("Exception");
                e.printStackTrace();
            } finally {
                Clock.yield();
            }
        }
    }

    private static HashMap<MapLocation, RobotRole> squadRolesBySpawnLocation = new HashMap<>();
    
    private static void spawn(RobotController rc) throws GameActionException {
        final BoundingBox mapBoundingBox = new BoundingBox(state.getMapHeight() - 1, state.getMapWidth(), 0, 0);

        for (int spawnZoneIndex = 0; spawnZoneIndex < Constants.SPAWN_ZONES_COUNT; spawnZoneIndex++) {
            final BoundingBox spawnZone = state.getSpawnZones().getZoneBoundingBoxes()[spawnZoneIndex];
            final MapLocation spawnCenter = spawnZone.getCenter();
            final Direction spawnOrientation = mapBoundingBox
                    .getPointOrientation(spawnCenter);
            final Offset[] formation = Formations.getFormationFromDirection(spawnOrientation.opposite());

            final int spawnZoneUnitCountChannel = SquadComms.getSpawnZoneUnitCountChannel(spawnZoneIndex);
            int unitsCount = rc.readSharedArray(spawnZoneUnitCountChannel);

            int squadNumber = (unitsCount / Constants.SQUAD_SIZE) + (spawnZoneIndex * Constants.SQUADS_PER_SPAWN_ZONE);
            int rank = unitsCount % Constants.SQUAD_SIZE;

            MapLocation captainLocation;
            switch (spawnOrientation) {
                case NORTH:
                    captainLocation = spawnCenter.translate(0, 1);
                    break;
                case EAST:
                    captainLocation = spawnCenter.translate(1, 0);
                    break;
                case SOUTH:
                    captainLocation = spawnCenter.translate(0, -1);
                    break;
                case WEST:
                    captainLocation = spawnCenter.translate(-1, 0);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + spawnOrientation);
            }

            final MapLocation spawnLoc = captainLocation.translate(formation[rank].dx, formation[rank].dy);
            
            if (!rc.canSpawn(spawnLoc)) {
                continue;
            }
            
            if (unitsCount == (Constants.SQUAD_SIZE * Constants.SQUADS_COUNT) / Constants.SPAWN_ZONES_COUNT) {
                continue;
            }
            
            rc.spawn(spawnLoc);

            state.setInitialSpawnZone(spawnZoneIndex);
            state.setSquadRank(squadNumber, rank);
            rc.writeSharedArray(spawnZoneUnitCountChannel, unitsCount + 1);
            SquadComms.writeSpawnsChannel(rc, spawnZone.getRelativePosition(spawnLoc), squadNumber);

            for (int i = 0; i < Constants.SQUAD_SIZE; i++) {
                if (i == rank) {
                    continue;
                }

                final Offset offset = formation[i];
                final MapLocation location = captainLocation.translate(offset.dx, offset.dy);
                squadRolesBySpawnLocation.put(location, Constants.SQUAD_COMPOSITION[i]);
            }

            break;
        }
    }

    private static void discoverSquadMembers(RobotController rc) throws GameActionException {
        final boolean[] spawnsByRelativeLoc = SquadComms.readSpawnsChannel(rc, state.getSquadNumber());

        for (int i = 0; i < spawnsByRelativeLoc.length; i++) {
            final boolean hasSpawned = spawnsByRelativeLoc[i];
            if (!hasSpawned) {
                continue;
            }

            final BoundingBox spawnZone = state.getSpawnZones().getZoneBoundingBoxes()[state.getInitialSpawnZone()];
            final MapLocation location = spawnZone.getLocationFromRelativePosition(i);
            final RobotInfo squadMember = rc.senseRobotAtLocation(location);

            if (squadMember != null && squadMember.getID() != state.getId()) {
                state.addSquadMember(squadMember.getID(), squadRolesBySpawnLocation.get(location));
            }
        }
    }
}
