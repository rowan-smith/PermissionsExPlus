package dev.rono.permissions.api.runtime;

import dev.rono.permissions.api.bus.PermissionDispatch;

/**
 * {@link PlatformEventBus} that starts as a no-op and can be upgraded to a live publisher
 * when legacy hook compatibility is activated.
 */
public final class SwitchablePlatformEventBus implements PlatformEventBus {

    private volatile PlatformEventBus delegate = NoOpPlatformEventBus.INSTANCE;

    public void activate(PlatformEventBus livePublisher) {
        if (livePublisher == null) {
            throw new NullPointerException("livePublisher");
        }
        delegate = livePublisher;
    }

    public boolean isActive() {
        return delegate != NoOpPlatformEventBus.INSTANCE;
    }

    @Override
    public void publish(PermissionDispatch dispatch) {
        delegate.publish(dispatch);
    }
}
