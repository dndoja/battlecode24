package duck;

import battlecode.common.*;
import duck.movement.*;
import duck.utils.stack.StackMapLocation;
import robot.utils.BytecodeCounter;

import java.util.Random;

public strictfp class RobotPlayer {
    static int turnCount = 0;

    static final Random rng = new Random();

    static boolean isPrepGamePhase() {
        return turnCount < 200;
    }

    public static void run(RobotController rc) throws GameActionException {
        // final Cartographer cartographer = new Cartographer(rc.getMapWidth(),
        // rc.getMapHeight());
        final MapLocation mapCenter = new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2);
        final PathedMovement pathedMover = new PathedMovement(rc);
        final DirectonalMovement directionalMover = new DirectonalMovement(rc, mapCenter);
        directionalMover.setShouldBounce(true);
        directionalMover.setMainDirection(Constants.DIRECTIONS[rng.nextInt(8)]);

        MapLocation flagTarget = null;

        while (true) {
            turnCount += 1;
            try {
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    final MapLocation currentLocation = rc.getLocation();
                    final StackMapLocation impassableLocations = new StackMapLocation(48);
                    final MapInfo[] mapInfos = rc.senseNearbyMapInfos(49);

                    for (int i = mapInfos.length; --i >= 0;) {
                        MapInfo mapInfo = mapInfos[i];
                        if (!mapInfo.isPassable()){
                            impassableLocations.push(mapInfo.getMapLocation());
                        }
                    }

                    if (isPrepGamePhase()) {
                        if (rc.isMovementReady()) {
                            directionalMover.move(currentLocation);
                        }
                    } else if (turnCount == 200) {
                        flagTarget = rc.senseBroadcastFlagLocations()[rc.getID() % 3];
                        directionalMover.setShouldBounce(false);
                        directionalMover.setMainDirection(currentLocation.directionTo(flagTarget));
                        pathedMover.pathTowards(currentLocation, flagTarget, impassableLocations);
                    } else if (rc.isMovementReady()) {
                        if (pathedMover.hasReachedLocalTarget(currentLocation)) {
                            pathedMover.pathTowards(currentLocation, flagTarget, impassableLocations);
                        }

                        if (!pathedMover.move(currentLocation)) {
                            directionalMover.move(currentLocation);
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
        for (MapLocation loc : rc.getAllySpawnLocations()) {
            if (rc.canSpawn(loc)) {
                rc.spawn(loc);
                return;
            }
        }
    }
}
