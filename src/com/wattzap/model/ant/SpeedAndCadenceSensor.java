/*
 * This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wattzap.model.ant;

import com.wattzap.model.SourceDataEnum;
import com.wattzap.model.UserPreferences;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Jarek
 */
public class SpeedAndCadenceSensor extends AntSensor {

	private final static Logger logger = LogManager.getLogger("ASCL");

    private static final int ANT_SPORT_SPEED_PERIOD = 8086;
	private static final int ANT_SPORT_SandC_TYPE = 121; // 0x78

    int wTlast = 0;
    int wRlast = 0;
    int cTlast = 0;
    int cRlast = 0;

    // wheel circumference [mm], taken from configuration
    double wheelSize = 1496.0;

    @Override
    public void configChanged(UserPreferences config) {
        wheelSize = config.getWheelsize();
    }

    @Override
    public String getPrettyName() {
        return "S&C";
    }

    @Override
    public String getSensorName() {
        return "C:SC";
    }

    @Override
    public int getSensorType() {
        return ANT_SPORT_SandC_TYPE;
    }

    @Override
    public int getSensorPeriod() {
        return ANT_SPORT_SPEED_PERIOD;
    }

    @Override
    public void storeReceivedData(long time, int[] data) {
        // Bytes 0 and 1: TTTT tick number [1024/s] when the last crank revolution
        // was detected
		int cT = data[0] + (data[1] << 8);
		// Bytes 2 and 3: crank revolution count
		int cR = data[2] + (data[3] << 8);

        // Bytes 4 and 5: TTTT tick number [1024/s] when the last wheel revolution
        // was detected
		int wT = data[4] + (data[5] << 8);
		// Bytes 6 and 7: wheel revolution count.
		int wR = data[6] + (data[7] << 8);

        String msg = "cT=" + cT + "/" + cTlast + " cR=" + cR + "/" + cRlast
                  + " cM=" + (time - getModificationTime(SourceDataEnum.CADENCE))
                  + " wT=" + wT + "/" + wTlast + " wR=" + wR + "/" + wRlast
                  + " wM=" + (time - getModificationTime(SourceDataEnum.WHEEL_SPEED));


        int wTD = wT - wTlast; // time delta
        if (wTD < 0) {
            wTD += 65536;
        }
		int wRD = wR - wRlast; // wheel rotations within the time
        if (wRD < 0) {
            wRD += 65536;
        }
        // wheel rotation detected.. and time delta is NOT zero
        // Sometimes time doesn't advance when rotation is detected (buggy sensor?)
        if ((wRD != 0) && (wTD != 0)) {
            // first rotation (after pause or in session) might have very short
            // update time, so it must be discarded and speed 0 must be reported.
            // Proper speed is reported just next update.
            if (time > getModificationTime(SourceDataEnum.WHEEL_SPEED) + 10000) {
                setValue(SourceDataEnum.WHEEL_SPEED, 0.0);
                msg += " first speed update";
            } else {
                double speed = // [m/s]
                        ((double) wRD * wheelSize / 1000.0) / ((double) wTD / 1024.0);
                setValue(SourceDataEnum.WHEEL_SPEED, speed);
                msg += " speed=" + speed;
            }
            wTlast = wT;
            wRlast = wR;
        }

        int cTD = cT - cTlast; // time delta
        if (cTD < 0) {
            cTD += 65536;
        }
		int cRD = cR - cRlast; // crank rotations within the time
        if (cRD < 0) {
            cRD += 65536;
        }
        if ((cRD != 0) && (cTD != 0)) {
            if (time > getModificationTime(SourceDataEnum.CADENCE) + 10000) {
                setValue(SourceDataEnum.CADENCE, 0.0);
                msg += " first cadence update";
            } else {
                double cadence = // [RPM]
                        ((double) cRD) / (((double) cTD / 1024.0) / 60.0);
                setValue(SourceDataEnum.CADENCE, cadence);
                msg += " cadence=" + cadence;
            }
            cTlast = cT;
            cRlast = cR;
        }

        logger.debug(msg);
    }

    @Override
    public boolean provides(SourceDataEnum data) {
        switch (data) {
            case WHEEL_SPEED:
            case CADENCE:
                return true;
            default:
                return false;
        }
    }
}
