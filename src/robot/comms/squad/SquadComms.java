package robot.comms.squad;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import robot.comms.Channels;

public class SquadComms {
    private static int getSquadChannelStart(int squadNumber) {
        return squadNumber * Channels.CHANNELS_PER_SQUAD;
    }

    public static void cleanSquadChannels(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber);
        for (int i = 0; i < Channels.CHANNELS_PER_SQUAD; i++) {
            rc.writeSharedArray(channel, 0);
        }
    }

    public static SquadChannel1 readChannel1(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber);
        final int encoded = rc.readSharedArray(channel);

        return SquadChannel1.decode(encoded);
    }

    public static void writeChannel1(RobotController rc, int squadNumber, SquadChannel1 channel) throws GameActionException {
        final int channelNumber = getSquadChannelStart(squadNumber);
        final int encoded = channel.encode();

        rc.writeSharedArray(channelNumber, encoded);
    }

    public static SquadChannel2 readChannel2(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 1;
        final int encoded = rc.readSharedArray(channel);

        return SquadChannel2.decode(encoded);
    }

    public static void writeChannel2(RobotController rc, int squadNumber, SquadChannel2 channel) throws GameActionException {
        final int channelNumber = getSquadChannelStart(squadNumber) + 1;
        final int encoded = channel.encode();

        rc.writeSharedArray(channelNumber, encoded);
    }
}
