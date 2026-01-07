package me.thezombiepl.plugin.zcore.command;

import me.thezombiepl.plugin.zcore.messages.MessageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Universal command handler with subcommand support.
 * Platform-agnostic. No overload ambiguity. Future-proof.
 */
public abstract class CommandHandler implements UniversalCommand {

    protected final Map<String, SubCommand> subCommands = new HashMap<>();
    private MessageManager messageManager;

    /* =========================
       Message manager
       ========================= */

    public void setMessageManager(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    protected MessageManager getMessageManager() {
        return messageManager;
    }

    /* =========================
       Registration
       ========================= */

    protected void registerSubCommand(String name, SubCommand command) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(command, "command");
        subCommands.put(name.toLowerCase(), command);
    }

    /* =========================
       Execution
       ========================= */

    @Override
    public boolean execute(CommandContext context) {
        if (context.getArgsLength() == 0) {
            return onNoArgs(context);
        }

        String subName = context.getArg(0).toLowerCase();
        SubCommand sub = subCommands.get(subName);

        if (sub == null) {
            context.sendMessage(getUnknownSubCommandMessage(context));
            return true;
        }

        if (sub.permission() != null && !context.hasPermission(sub.permission())) {
            context.sendMessage(getNoPermissionMessage(context));
            return true;
        }

        String[] trimmedArgs = new String[context.getArgsLength() - 1];
        System.arraycopy(context.getArgs(), 1, trimmedArgs, 0, trimmedArgs.length);

        CommandContext subContext = new CommandContext(
                context.getRawSender(),
                trimmedArgs,
                context.getLabel() + " " + subName,
                context.getPlatform()
        );

        return sub.execute(subContext);
    }

    /* =========================
       Tab Completion
       ========================= */

    /**
     * Returns list of available subcommand names for tab completion
     * (filtered by permission)
     */
    public List<String> getSubCommandNames(CommandContext context) {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            SubCommand sub = entry.getValue();
            if (sub.permission() == null || context.hasPermission(sub.permission())) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    /* =========================
       Hooks
       ========================= */

    protected abstract boolean onNoArgs(CommandContext context);

    protected String getNoPermissionMessage(CommandContext context) {
        if (messageManager != null) {
            return messageManager.getMessage(
                    "messages.no-permission",
                    "&cYou don't have permission!"
            );
        }
        return "&cYou don't have permission!";
    }

    protected String getUnknownSubCommandMessage(CommandContext context) {
        if (messageManager != null) {
            return messageManager.getMessage(
                    "messages.unknown-subcommand",
                    "&cUnknown subcommand."
            );
        }
        return "&cUnknown subcommand.";
    }

    /* =========================
       Core API
       ========================= */

    @FunctionalInterface
    public interface SubCommand {
        boolean execute(CommandContext context);

        default String permission() {
            return null;
        }

        default String description() {
            return "";
        }
    }

    /* =========================
       Adapters
       ========================= */

    public static final class SubCommands {

        private SubCommands() {}

        public static SubCommand simple(SimpleExecutor executor) {
            return executor::execute;
        }

        public static SubCommand withPermission(String permission, SubCommand inner) {
            return new SubCommand() {
                @Override
                public boolean execute(CommandContext context) {
                    return inner.execute(context);
                }

                @Override
                public String permission() {
                    return permission;
                }

                @Override
                public String description() {
                    return inner.description();
                }
            };
        }
        
        /**
         * Creates a subcommand with both permission and description
         */
        public static SubCommand withDetails(String permission, String description, SimpleExecutor executor) {
            return new SubCommand() {
                @Override
                public boolean execute(CommandContext context) {
                    return executor.execute(context);
                }

                @Override
                public String permission() {
                    return permission;
                }

                @Override
                public String description() {
                    return description;
                }
            };
        }
    }

    @FunctionalInterface
    public interface SimpleExecutor {
        boolean execute(CommandContext context);
    }
}