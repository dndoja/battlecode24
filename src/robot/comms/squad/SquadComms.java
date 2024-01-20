package robot.comms.squad;

import java.util.ArrayList;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import robot.Constants;
import robot.Loggy;
import robot.comms.Channels;
import robot.state.RobotRole;
import robot.utils.BoundingBox;
import robot.utils.Sectors;
import robot.utils.Utils;

public class SquadComms {
    public static final int getSpawnZoneUnitCountChannel(int spawnZone) {
        return Channels.BAND_SQUAD_END + spawnZone;
    }

    private static int getSquadChannelStart(int squadNumber) {
        return squadNumber * Channels.CHANNELS_PER_SQUAD;
    }

    public static void cleanSquadChannels(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber);
        System.out.println("Cleaning channels " + channel + " to " + (channel + Channels.CHANNELS_PER_SQUAD));
        for (int i = 0; i < Channels.CHANNELS_PER_SQUAD; i++) {
            rc.writeSharedArray(channel, 0);
        }
    }

    public static SquadChannel1 readChannel1(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber);
        final int encoded = rc.readSharedArray(channel);

        return SquadChannel1.decode(encoded);
    }

    public static void writeChannel1(RobotController rc, int squadNumber, SquadChannel1 channel)
            throws GameActionException {
        final int channelNumber = getSquadChannelStart(squadNumber);
        final int encoded = channel.encode();

        rc.writeSharedArray(channelNumber, encoded);
    }

    // public static SquadChannel2 readChannel2(RobotController rc, int squadNumber) throws GameActionException {
    //     final int channel = getSquadChannelStart(squadNumber) + 1;
    //     final int encoded = rc.readSharedArray(channel);

    //     return SquadChannel2.decode(encoded);
    // }

    // public static void writeChannel2(RobotController rc, int squadNumber, SquadChannel2 channel)
    //         throws GameActionException {
    //     final int channelNumber = getSquadChannelStart(squadNumber) + 1;
    //     final int encoded = channel.encode();

    //     rc.writeSharedArray(channelNumber, encoded);
    // }

    public static void writeCaptainLocation(RobotController rc, int squadNumber, MapLocation location)
            throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 1;
        final int encoded = Utils.encodeLocation(location);

        rc.writeSharedArray(channel, encoded);
    }

    public static MapLocation readCaptainLocation(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 1;
        final int encoded = rc.readSharedArray(channel);

        return Utils.decodeLocation(encoded);
    }

    public static void writeSpawnsChannel(RobotController rc, int relativeLocation, int squadNumber)
            throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 2;
        final int currentValue = rc.readSharedArray(channel);
        final int newValue = currentValue | (1 << relativeLocation);
        rc.writeSharedArray(channel, newValue);
    }

    public static boolean[] readSpawnsChannel(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 2;
        final int value = rc.readSharedArray(channel);
        final boolean[] spawnMap = new boolean[9];

        for (int i = 0; i < 9; i++) {
            final int spawnMapValue = (value >> i) & 0x1;
            spawnMap[i] = spawnMapValue == 1;
        }

        return spawnMap;
    }

    public static void cleanWallsChannel(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 3;
        rc.writeSharedArray(channel, 0);
        rc.writeSharedArray(channel + 1, 0);
        rc.writeSharedArray(channel + 2, 0);
    }

    public static void writeSectorWalls(RobotController rc, int squadNumber, ArrayList<Integer> wallRelativeLocations)
            throws GameActionException {
        final int baseChannel = getSquadChannelStart(squadNumber) + 3;
        final ArrayList<ArrayList<Integer>> splitLocations = new ArrayList<ArrayList<Integer>>() {
            {
                add(new ArrayList<Integer>());
                add(new ArrayList<Integer>());
                add(new ArrayList<Integer>());
            }
        };

        for (int i = wallRelativeLocations.size()-1; --i >= 0;) {
            final int relativeLocation = wallRelativeLocations.get(i);
            final int channelOffset = relativeLocation / 16;
            if (channelOffset > splitLocations.size() - 1) {
                continue;
            }

            splitLocations.get(channelOffset).add(relativeLocation);
        }

        for (int channelOffset = 0; channelOffset < splitLocations.size(); channelOffset++) {
            final ArrayList<Integer> locations = splitLocations.get(channelOffset);
            final int channel = baseChannel + channelOffset;

            int newValue = 0;
            for (int location : locations) {
                final int offset = location % 16;
                newValue = newValue | (1 << offset);
            }

            rc.writeSharedArray(channel, newValue);
        }
    }

    public static ArrayList<MapLocation> readSectorWalls(RobotController rc, int squadNumber, Sectors sectors)
            throws GameActionException {
        final ArrayList<MapLocation> wallLocations = new ArrayList<MapLocation>();
        final MapLocation captainLocation = readCaptainLocation(rc, squadNumber);
        if (captainLocation == null) {
            return wallLocations;
        }

        final BoundingBox sectorBounds = new BoundingBox(captainLocation, Constants.SECTOR_SIZE, Constants.SECTOR_SIZE);
        final int baseChannel = getSquadChannelStart(squadNumber) + 3;
        for (int channelOffset = 2; --channelOffset >= 0;) {
            final int channel = baseChannel + channelOffset;
            final int value = rc.readSharedArray(channel);

            for (int i = 15; --i>=0;) {
                final int wallMapValue = (value >> i) & 0x1;

                if (wallMapValue == 1) {
                    final int location = channelOffset * 16 + i;
                    final MapLocation wallLocation = sectorBounds.getLocationFromRelativePosition(location);
                    wallLocations.add(wallLocation);
                }
            }
        }

        return wallLocations;
    }
}
