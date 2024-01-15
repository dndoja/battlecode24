package robot.comms;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import robot.Constants;

public class SectorDiscovery {
    public static void cleanSectorDiscovery(RobotController rc) throws GameActionException {
        for (int i = Channels.BAND_SECTOR_DISCOVERY_START; i < Channels.BAND_SECTOR_DISCOVERY_END; i++) {
            rc.writeSharedArray(i, 0);
        }
    }

    public static void markSectorAsDiscovered(RobotController rc, int sector) throws GameActionException {
        final int sectorRow = sector / Constants.MAX_SECTOR_NUMBER_1D;
        final int sectorCol = sector % Constants.MAX_SECTOR_NUMBER_1D;
        final int channelOffset = sectorRow / 2;
        final int channel = Channels.BAND_SECTOR_DISCOVERY_START + channelOffset;

        final int mask = 1 << 8 * (sectorRow % 2) + sectorCol;

        final int currentVal = rc.readSharedArray(Channels.BAND_SECTOR_DISCOVERY_START + channelOffset);

        rc.writeSharedArray(channel, currentVal | mask);
    }

    public static boolean[] getSectorDiscoveryMap(RobotController rc) throws GameActionException {
        final boolean[] sectorDiscoveryMap = new boolean[Constants.MAX_SECTOR_NUMBER_1D * Constants.MAX_SECTOR_NUMBER_1D];

        for (int i = Channels.BAND_SECTOR_DISCOVERY_START; i < Channels.BAND_SECTOR_DISCOVERY_END; i++) {
            final int channelOffset = i - Channels.BAND_SECTOR_DISCOVERY_START;
            final int channelVal = rc.readSharedArray(i);

            final int evenVal = channelVal & 0xFF;
            final int oddVal = channelVal >> 8 & 0xFF;
            
            for (int j = 0; j < 8; j++) {
                final int firstSectorRow = 2 * channelOffset;
                final int secondSectorRow = firstSectorRow + 1;
                final int mask = 1 << j;

                sectorDiscoveryMap[firstSectorRow * Constants.MAX_SECTOR_NUMBER_1D + j] = (evenVal & mask) != 0;
                sectorDiscoveryMap[secondSectorRow * Constants.MAX_SECTOR_NUMBER_1D + j] = (oddVal & mask) != 0;
            }
        }

        return sectorDiscoveryMap;
    }
}
