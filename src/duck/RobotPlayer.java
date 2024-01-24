package duck;

import battlecode.common.*;
import duck.pathing.*;
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
        final MomentumPather bounceMover = new MomentumPather(rc,
                new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
        bounceMover.setMainDirection(Constants.DIRECTIONS[rng.nextInt(8)]);
        MapLocation flagTarget = null;

        while (true) {
            turnCount += 1;
            try {
                if (!rc.isSpawned()) {
                    spawn(rc);
                } else {
                    if (isPrepGamePhase()) {
                        if (rc.isMovementReady()) {
                            bounceMover.move();
                        }
                    } else if (turnCount == 200) {
                        flagTarget = rc.senseBroadcastFlagLocations()[rc.getID() % 3];
                    } else {
                        BytecodeCounter.start();
                        final long[] pathingDistances = Pathfinder.getReachabilityMasks(new MapLocation(7, 3));
                        final Direction[] directions = Pathfinder.getNextDirectionsToTarget(pathingDistances,
                                new MapLocation(3, 3));
                        BytecodeCounter.stop("getDistancesTo");
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
