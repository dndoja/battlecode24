package robot.comms.squad;

import robot.comms.Channels;

public class SquadFormation {
    public static final int getSpawnZoneSquadCountChannel(int spawnZone) {
        return Channels.SQUAD_FORMATION_BAND_END + spawnZone;
    }

    // 9 channels per squad, 8 for individual announcements and 1 for count (1st channel is for the squad count)
    public static final int getAnnouncementChannel(int squadNumber, int rank) {
        final int channelStart = squadNumber * Channels.SQUAD_FORMATION_CHANNEL_SIZE + 1 + rank;
        return channelStart;
    }

    public static final int getSquadHeadcountChannel(int squadNumber) {
        return squadNumber * Channels.SQUAD_FORMATION_CHANNEL_SIZE;
    }
}
