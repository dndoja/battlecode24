package robot.comms.squad;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import robot.Constants;
import robot.comms.Channels;
import robot.state.RobotRole;

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

    public static void writeRolesChannel(RobotController rc, int relativeLocation, int squadNumber, RobotRole role)
            throws GameActionException {
        // Skip center location
        if (relativeLocation > 4) {
            relativeLocation--;
        }

        final int channel = getSquadChannelStart(squadNumber) + 2;
        final int currentValue = rc.readSharedArray(channel);
        final int newValue = currentValue | (role.ordinal()+1 << (relativeLocation * 2));
        rc.writeSharedArray(channel, newValue);
    }

    public static RobotRole[] readRolesChannel(RobotController rc, int squadNumber) throws GameActionException {
        final int channel = getSquadChannelStart(squadNumber) + 2;
        final int value = rc.readSharedArray(channel);
        final RobotRole[] roles = new RobotRole[9];

        for (int i = 0; i < 8; i++) {
            final int roleOrdinal = (value >> (i * 2)) & 0x3;
            final int index = i >= 4 ? i + 1 : i;
            if (roleOrdinal == 0){
                roles[index] = null;
            }else{
                roles[index] = Constants.ROBOT_ROLES[roleOrdinal-1];
            }
        }

        roles[4] = null;

        return roles;
    }
}
