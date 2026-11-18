package dev.rono.permissions.core.command;

import lombok.var;

import dev.rono.permissions.api.context.ContextSet;
import dev.rono.permissions.api.group.Group;
import dev.rono.permissions.api.options.OptionNode;
import dev.rono.permissions.api.parent.ParentNode;
import dev.rono.permissions.api.permission.*;
import dev.rono.permissions.api.resolver.CandidateStatus;
import dev.rono.permissions.api.resolver.QueryOptions;
import dev.rono.permissions.api.resolver.ResolutionCandidate;
import dev.rono.permissions.api.user.User;
import dev.rono.permissions.core.PexApiImpl;
import dev.rono.permissions.core.util.Durations;
import java.time.Duration;
import java.time.Instant;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import dev.rono.permissions.core.util.Immutables;

abstract class CommandSupport<C> {
    protected final PexApiImpl<C> core;
    private final BiConsumer<C, String> messages;

    CommandSupport(PexApiImpl<C> core, BiConsumer<C, String> messages) {
        this.core = core;
        this.messages = messages;
    }

    protected void reply(C sender, String message) {
        messages.accept(sender, message);
    }

    protected void audit(C sender, String action) {
        core.logger().audit(String.valueOf(sender), action);
    }

    protected <T> T await(CompletionStage<T> stage) {
        return stage.toCompletableFuture().join();
    }

    /** The named user; replies "not found" to the sender if there is none. */
    protected User user(String name) {
        return await(core.users().find(name)).orElseThrow(() -> new CommandFailure("§cUser not found: " + name));
    }

    /** The named group; replies "not found" to the sender if there is none. */
    protected Group group(String name) {
        return await(core.groups().find(name)).orElseThrow(() -> new CommandFailure("§cGroup not found: " + name));
    }

    protected void heading(C sender, String type, String value) {
        reply(sender, "§6" + type + ": §e" + value);
    }

    protected void section(C sender, String title, Collection<String> values) {
        orderedSection(sender, title, values.stream().sorted().collect(Immutables.toList()));
    }

    protected void orderedSection(C sender, String title, Collection<String> values) {
        reply(sender, "§6" + title + ":");

        if (values.isEmpty()) {
            reply(sender, "§7(none)");
            return;
        }

        values.forEach(value -> reply(sender, "§7- §f" + value));
    }

    protected String permission(PermissionNode node) {
        var value = node.value() == PermissionValue.ALLOW ? node.permission() : "-" + node.permission();
        return contextual(value, node.contexts());
    }

    protected String option(OptionNode node) {
        return contextual(node.key() + ":" + node.value(), node.contexts());
    }

    protected String parent(ParentNode node) {
        var value = contextual(node.group(), node.contexts());

        return node.expiry().map(expiry -> value + " §7(expires in " + Durations.format(Duration.between(Instant.now(), expiry)) + ")").orElse(value);
    }

    protected QueryOptions queryOptions(ContextSet contexts) {
        return QueryOptions.builder().contexts(contexts).build();
    }

    protected void trace(C sender, PermissionHolder holder, String permission, QueryOptions options) {
        var resolution = core.resolvers().permissions().explain(holder, permission, options);

        reply(sender, "§6Tracing permission resolution for " + holderLabel(holder) + "§6...");
        reply(sender, "§6Node: §f" + permission);
        reply(sender, "§6Context: " + contexts(options.contexts()));

        var branches = new LinkedHashMap<PermissionHolder, Collection<ResolutionCandidate>>();

        resolution.candidates().stream().filter(candidate -> candidate.status() != CandidateStatus.PERMISSION_MISMATCH)
                .forEach(candidate -> branches.computeIfAbsent(candidate.source(), ignored -> new java.util.ArrayList<>()).add(candidate));

        if (branches.isEmpty()) {
            reply(sender, "§7(none)");
        }

        branches.forEach((source, candidates) -> {
            var distance = candidates.iterator().next().inheritanceDistance();

            reply(sender, branchLabel(source, distance));
            candidates.forEach(candidate -> reply(sender, "§7↳" + candidateLine(candidate)));
            reply(sender, "");
        });

        reply(sender, "§6Final effective state: " + resultLabel(resolution.result()) + "§7 " + resolution.winner().map(candidate -> "(Resolved via " + sourceLabel(candidate.source()) + "§7)").orElse("(No applicable node)"));
        reply(sender, "§7§m------------------------------");
    }

    protected void check(C sender, PermissionHolder holder, String permission, QueryOptions options) {
        var resolution = core.resolvers().permissions().explain(holder, permission, options);

        reply(sender, "§6Checking permission for " + holderLabel(holder) + "§6...");
        reply(sender, "§6Node: §f" + permission);
        reply(sender, "§6Context: " + contexts(options.contexts()));
        reply(sender, "§6Result: " + resultLabel(resolution.result()) + "§7 " + resolution.winner().map(candidate -> "(" + winnerReason(candidate) + ")").orElse("(No matching node)"));
        resolution.winner().ifPresent(candidate -> reply(sender, "§6Source: " + sourceLabel(candidate.source())));
    }

    private String holderLabel(PermissionHolder holder) {
        if (holder instanceof User) {
            User user = (User) holder;
            return "user §e§l" + user.name();
        }

        if (holder instanceof Group) {
            Group group = (Group) holder;
            return "group §e§l" + group.name();
        }

        return "holder §e§l" + holder;
    }

    private String branchLabel(PermissionHolder source, int distance) {
        if (source instanceof User) {
            return "§6User Profile §7(Explicit Nodes):";
        }

        if (source instanceof Group) {
            Group group = (Group) source;
            var depth = distance > 1 ? "Deep inherited branch" : "Parent branch";
            var weight = group.weight().isPresent() ? Integer.toString(group.weight().getAsInt()) : "none";

            return "§6" + depth + ": Group §e" + group.name() + " §7(Weight: " + weight + ")";
        }

        return "§6Source: " + sourceLabel(source);
    }

    private String candidateLine(ResolutionCandidate candidate) {
        var node = (candidate.node().denied() ? "-" : "") + candidate.node().permission();

        return statusIcon(candidate.status()) + " §f" + node + " §7" + contexts(candidate.node().contexts()) + " §8- " + statusLabel(candidate.status()) + candidate.detail().map(detail -> " §8(" + detail + ")").orElse("");
    }

    private String winnerReason(ResolutionCandidate candidate) {
        var decision = candidate.node().allowed() ? "granted" : "denied";

        return candidate.inheritanceDistance() == 0 ? "Explicitly " + decision : "Inherited permission " + decision;
    }

    private String sourceLabel(PermissionHolder source) {
        if (source instanceof Group) {
            Group group = (Group) source;
            return "§eInherited from group " + group.name();
        }

        if (source instanceof NamedPermissionHolder) {
            NamedPermissionHolder named = (NamedPermissionHolder) source;
            return "§eExplicit node on " + named.name();
        }

        return "§e" + source;
    }

    private String resultLabel(PermissionResult result) {
        switch (result) {
            case ALLOW :
                return "§a§lALLOWED §r";
            case DENY :
                return "§c§lDENIED §r";
            case UNDEFINED :
                return "§e§lUNDEFINED §r";
            default :
                throw new IllegalStateException("Unexpected value: " + result);
        }
    }

    private String statusIcon(CandidateStatus status) {
        return status == CandidateStatus.WINNER ? "§a✔" : "§c✘";
    }

    private String statusLabel(CandidateStatus status) {
        switch (status) {
            case WINNER :
                return "§a§lMATCHED §r";
            case OUTRANKED :
                return "§cSKIPPED";
            case CONFLICT :
                return "§cCONFLICT";
            case EXPIRED :
                return "§cEXPIRED";
            case CONTEXT_MISMATCH :
                return "§cIGNORED (Context mismatch)";
            case PERMISSION_MISMATCH :
                return "§cIGNORED (Permission mismatch)";
            case INHERITANCE_DISABLED :
                return "§cIGNORED (Inheritance disabled)";
            case DEFAULTS_DISABLED :
                return "§cIGNORED (Defaults disabled)";
            default :
                throw new IllegalStateException("Unexpected value: " + status);
        }
    }

    private String contexts(ContextSet contexts) {
        if (contexts.isEmpty()) {
            return "§7(Global)";
        }

        return "§7[" + contexts.asMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(value -> entry.getKey() + "=" + value)).sorted()
                .reduce((left, right) -> left + ", " + right).orElse("") + "]";
    }

    private String contextual(String value, ContextSet contexts) {
        if (contexts.isEmpty()) {
            return value;
        }

        return value + " [" + contexts.asMap().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(item -> entry.getKey() + "=" + item)).sorted()
                .reduce((left, right) -> left + "," + right).orElse("") + "]";
    }
}
