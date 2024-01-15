package robot;

import battlecode.common.*;
import robot.comms.squad.SquadFormation;
import robot.pathing.BruteMover;
import robot.runner.Captain;
import robot.state.RobotState;
import robot.utils.Utils;

import java.util.ArrayList;
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
        // Navigator navigator = null;
        // Cartographer cartographer = null;
        
        state.initialize(rc);

        while (true) {
            turnCount += 1;
            try {
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    if (state.getSquad().size() < Constants.SQUAD_SIZE-1) {
                        discoverSquadMembers(rc);
                    }

                    if (state.getSquad().size() == Constants.SQUAD_SIZE-1) {
                        state.incrementTurnsSinceSquadFormation();
                    }

                    // rc.setIndicatorString(state.getRole().toString());
                    
                    switch (state.getRole()){
                        case CAPTAIN:
                            Captain.doTurn(rc, state, turnCount, mover);
                            break;
                        default:
                            break;
                    }
                    
                    // final int sharedId = rc.readSharedArray(0);
                    // final int id = rc.getID();
                    // if (sharedId == 0){
                    // rc.writeSharedArray(0, id);
                    // }else if (sharedId == id){
                    // shouldPrint = true;
                    // }
                    // if (cartographer == null) {
                    //     cartographer = new Cartographer(rc.getMapWidth(), rc.getMapHeight());
                    // }

                    // if (!mover.hasTarget()) {
                    //     switch (1) {
                    //         case 0:
                    //             mover.setTarget(new MapLocation(0, 0), 1);
                    //             break;
                    //         case 1:
                    //             mover.setTarget(new MapLocation(rc.getMapWidth() - 1, 0), 1);
                    //             break;
                    //         case 2:
                    //             mover.setTarget(new MapLocation(0, rc.getMapHeight() - 1), 1);
                    //             break;
                    //         case 3:
                    //             mover.setTarget(new MapLocation(rc.getMapWidth() - 1, rc.getMapHeight() - 1), 1);
                    //             break;
                    //     }
                    // }

                    // final MapInfo[] mapInfos = rc.senseNearbyMapInfos();
                    // for (MapInfo mapInfo : mapInfos) {
                    //     final MapLocation location = mapInfo.getMapLocation();

                    //     LocationFeature feature = LocationFeature.EMPTY;
                    //     if (mapInfo.isWall()) {
                    //         feature = LocationFeature.WALL;
                    //     } else if (mapInfo.isWater()) {
                    //         feature = LocationFeature.WATER;
                    //     } else if (!mapInfo.isPassable()) {
                    //         feature = LocationFeature.WATER;
                    //     } else if (mapInfo.getCrumbs() > 0) {
                    //         feature = LocationFeature.CRUMBS;
                    //         mover.setTarget(location, 2);
                    //     }

                    //     cartographer.addLocationFeature(new Location(location.x, location.y, feature.ordinal()));
                    // }

                    // mover.move();

                    // cartographer.djikstraMapOfType(PathingMapType.EXPLORATION, loc, turnCount);
                }
            } catch (GameActionException e) {
                // System.out.println("GameActionException");
                // e.printStackTrace();
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

            rc.spawn(spawnLoc);

            if (isPrepGamePhase()){
                final int spawnZoneNumber = spawnZones[i];
                final int spawnZoneSquadCountChannel = SquadFormation.getSpawnZoneSquadCountChannel(spawnZoneNumber);
                System.out.println("Spawn channel " + spawnZoneSquadCountChannel);

                int squadsCount = rc.readSharedArray(spawnZoneSquadCountChannel);
                int squadNumber = spawnZoneNumber + (3 * squadsCount);
                int squadHeadcountChannel = SquadFormation.getSquadHeadcountChannel(squadNumber);
                int headcount = rc.readSharedArray(squadHeadcountChannel);
                
                if (headcount >= 8){
                    headcount = 0;
                    squadsCount += 1;
                    squadNumber = spawnZoneNumber + (3 * squadsCount);
                    rc.writeSharedArray(spawnZoneSquadCountChannel, squadsCount);
                }
                
                state.setInitialSpawnZone(spawnZoneNumber);
                state.setSquadNumber(squadNumber);
                state.setRoleFromSpawnOrder(headcount);

                rc.writeSharedArray(squadHeadcountChannel, headcount+1);
                rc.writeSharedArray(SquadFormation.getAnnouncementChannel(squadNumber, headcount), Utils.trimRobotId(state.getId()));
            }

            break;
        }
    }

    private static void discoverSquadMembers(RobotController rc) throws GameActionException {
        final int squadAnnouncementsChannelStart = SquadFormation.getSquadHeadcountChannel(state.getSquadNumber()) + 1;

        for (int i = 0; i < Constants.SQUAD_SIZE; i++) {
            final int squadMemberId = Utils.untrimRobotId(rc.readSharedArray(squadAnnouncementsChannelStart + i));

            if (squadMemberId != state.getId() && squadMemberId != 10000){
                state.addSquadMember(squadMemberId, Constants.SQUAD_COMPOSITION[i]);
            }
        }
    }
}
