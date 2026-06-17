package dev.rono.permissions.core.storage.resolution;

import dev.rono.permissions.api.permission.ResolvedPermissionView;

public final class ResolvedPermissionViewAdapter implements ResolvedPermissionView {

    private final ResolvedPermission delegate;

    public ResolvedPermissionViewAdapter(ResolvedPermission delegate) {
        this.delegate = delegate;
    }

    @Override
    public String permission() {
        return delegate.getPermission();
    }

    @Override
    public boolean value() {
        return delegate.isValue();
    }

    @Override
    public int priority() {
        return delegate.getPriority();
    }

    @Override
    public String contextKey() {
        return delegate.getContextKey();
    }

    @Override
    public String source() {
        return delegate.getSource();
    }
}
