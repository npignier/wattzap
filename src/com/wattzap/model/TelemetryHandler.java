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

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.MessageCallback;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.Telemetry;

/**
 *
 * @author Jarek
 */
public abstract class TelemetryHandler
    extends SourceDataHandler
    implements MessageCallback
{
    @Override
    public SourceDataHandlerIntf initialize() {
        // activate handler, all telemetries are active (in fact.. some
        // computations are not necessary, but who cares)
        setLastMessageTime(-1);

        // register messages
        MessageBus.INSTANCE.register(Messages.TELEMETRY, this);
		MessageBus.INSTANCE.register(Messages.CONFIG_CHANGED, this);

        // initialize all config properties
        configChanged(UserPreferences.INSTANCE);

        // notify about new telemetryProvider
        MessageBus.INSTANCE.send(Messages.HANDLER, this);
        return this;
    }

    @Override
    public void release() {
        MessageBus.INSTANCE.unregister(Messages.TELEMETRY, this);
		MessageBus.INSTANCE.unregister(Messages.CONFIG_CHANGED, this);

        // not ready anymore
        setLastMessageTime(0);

        // request handler removal
        MessageBus.INSTANCE.send(Messages.HANDLER, this);
        MessageBus.INSTANCE.send(Messages.HANDLER_REMOVED, this);
    }

    public abstract void storeTelemetryData(Telemetry t);

    public abstract void configChanged(UserPreferences pref);

    @Override
    public void callback(Messages m, Object o) {
        switch (m) {
            case TELEMETRY:
                if (getLastMessageTime() != 0) {
                    storeTelemetryData((Telemetry) o);
                }
                break;
            case CONFIG_CHANGED:
                configChanged((UserPreferences) o);
                break;
        }
    }
}
