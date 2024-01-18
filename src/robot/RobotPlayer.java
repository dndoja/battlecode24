package robot;

import battlecode.common.*;
import robot.comms.squad.SquadComms;
import robot.pathing.BruteMover;
import robot.runner.Captain;
import robot.runner.Grunt;
import robot.state.RobotRole;
import robot.state.RobotState;
import robot.utils.BoundingBox;
import java.util.Random;

public strictfp class RobotPlayer {
    static int turnCount = 0;

    static final Random rng = new Random();

    public static final RobotState state = new RobotState();

    static boolean isPrepGamePhase() {
        return turnCount < 200;
    }

    public static void run(RobotController rc) throws GameActionException {
        final BruteMover mover = new BruteMover(rc);

        state.initialize(rc);

        while (true) {
            turnCount += 1;
            try {
                rc.setIndicatorString(state.getSquadNumber() + " " + state.getRole());

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
                            Captain.doTurn(rc, state, turnCount, mover);
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

    private static void spawn(RobotController rc) throws GameActionException {
        MapLocation[] spawnLocs = rc.getAllySpawnLocations();
        int[] spawnZones = state.getSpawnZones().getSpawnLocationToZoneIndex();

        for (int i = 0; i < spawnLocs.length; i++) {
            final MapLocation spawnLoc = spawnLocs[i];
            if (!rc.canSpawn(spawnLoc)) {
                continue;
            }

            final int spawnZoneNumber = spawnZones[i];
            final BoundingBox spawnZone = state.getSpawnZones().getZoneBoundingBoxes()[spawnZoneNumber];
            
            if (spawnZone.getCenter().equals(spawnLoc)) {
                continue;
            }

            final int spawnZoneUnitCountChannel = SquadComms.getSpawnZoneUnitCountChannel(spawnZoneNumber);
            int unitsCount = rc.readSharedArray(spawnZoneUnitCountChannel);

            int squadNumber = (unitsCount / Constants.SQUAD_SIZE) + (spawnZoneNumber * Constants.SQUADS_PER_SPAWN_ZONE);
            int rank = unitsCount % Constants.SQUAD_SIZE;

            if (unitsCount == (Constants.SQUAD_SIZE * Constants.SQUADS_COUNT) / Constants.SPAWN_ZONES_COUNT) {
                continue;
            }

            rc.spawn(spawnLoc);

            state.setInitialSpawnZone(spawnZoneNumber);
            state.setSquadRank(squadNumber, rank);
            rc.writeSharedArray(spawnZoneUnitCountChannel, unitsCount + 1);
            SquadComms.writeRolesChannel(rc, spawnZone.getRelativePosition(spawnLoc), squadNumber, state.getRole());

            break;
        }
    }

    private static void discoverSquadMembers(RobotController rc) throws GameActionException {
        final RobotRole[] rolesByRelativeLoc = SquadComms.readRolesChannel(rc, state.getSquadNumber());

        for (int i = 0; i < rolesByRelativeLoc.length; i++){
            final RobotRole role = rolesByRelativeLoc[i];
            if (role == null){
                continue;
            }

            final BoundingBox spawnZone = state.getSpawnZones().getZoneBoundingBoxes()[state.getInitialSpawnZone()];
            final MapLocation location = spawnZone.getLocationFromRelativePosition(i);
            final RobotInfo squadMember = rc.senseRobotAtLocation(location);

            if (squadMember != null && squadMember.getID() != state.getId()) {
                state.addSquadMember(squadMember.getID(), role);
            }
        }
    }
}
