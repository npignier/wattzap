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
package com.wattzap.model.dto;

/**
 * General distance point representation
 * @author Jarek
 */
public class AxisPoint {
    private double dist;

    public AxisPoint(double dist) {
        this.dist = dist;
    }

    public double getDistance() {
        return dist;
    }

    /**
     * Checks the data, if next point is ok
     * @param next
     * @return null if next point is ok, otherwise error message
     */
    public String checkData(AxisPoint next) {
        return null;
    }

    public void normalize(double ratio) {
        this.dist *= ratio;
    }
}
