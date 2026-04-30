package ru.tehkode.permissions.exceptions;

public class RankingException extends Exception {
    private final Object user;
    private final Object promoter;

    public RankingException(String message, Object user, Object promoter) {
        super(message);
        this.user = user;
        this.promoter = promoter;
    }

    public Object getUser() {
        return user;
    }

    public Object getTarget() {
        return user;
    }

    public Object getPromoter() {
        return promoter;
    }
}
