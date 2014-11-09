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
package com.wattzap.model;

import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Telemetry;

/**
 * Power profile for climbing. At max slope it reports FTP, on flat it
 * reports 50% of FTP.. Just for "standalone" mode.
 * @author Jarek
 */
@SelectableDataSourceAnnotation
public class SimulSpeedPowerProfile extends VirtualPowerProfile {
    private int ftp = 250;
    private double maxSlope = 20.0;

    @Override
    public String getPrettyName() {
        return "simulSpeed";
    }

    @Override
    public void configChanged(UserPreferences prefs) {
        super.configChanged(prefs);

        if ((prefs == UserPreferences.INSTANCE) ||
                (prefs == UserPreferences.MAX_POWER)) {
            ftp = prefs.getMaxPower();
        }
    }

    @Override
    public void callback(Messages m, Object o) {
        super.callback(m, o);
        if (m == Messages.GPXLOAD) {
            maxSlope = ((RouteReader) o).getMaxSlope();
            System.err.println("New maxSlope " + maxSlope);
        }
    }

    @Override
    public void storeTelemetryData(Telemetry t) {
        // doesn't set pause.. if no slope or maxSlope is too small, just
        // report half FTP.
        if ((!t.isAvailable(SourceDataEnum.SLOPE)) || (maxSlope < 0.1)) {
            setValue(SourceDataEnum.POWER, 0.5 * ftp);
        }

        double powerWatts = 0.5 * ftp + (1.0 + (t.getGradient() * 100.0) / maxSlope);
        if (powerWatts < 0) {
            powerWatts = 0;
        }
        setValue(SourceDataEnum.POWER, powerWatts);

        // compute speed as well
        computeSpeed(t);
    }
}
