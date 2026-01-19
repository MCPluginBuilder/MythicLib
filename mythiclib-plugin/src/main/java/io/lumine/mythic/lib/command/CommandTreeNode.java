package io.lumine.mythic.lib.command;

import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.command.argument.Argument;
import io.lumine.mythic.lib.util.lang3.Validate;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class CommandTreeNode {
    private final String id;
    private final CommandTreeNode parent;
    private final int heightInTree;

    private final Map<String, CommandTreeNode> children = new HashMap<>();
    private final List<Argument<?>> arguments = new ArrayList<>();

    protected static final Random RANDOM = new Random();

    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     * @param id     The node id
     */
    public CommandTreeNode(@Nullable CommandTreeNode parent, @NotNull String id) {
        this.id = id;
        this.parent = parent;
        this.heightInTree = parent == null ? 0 : parent.getLevel() + 1;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getPath() {
        return (hasParent() ? parent.getPath() + " " : "") + getId();
    }

    @NotNull
    public Collection<CommandTreeNode> getChildren() {
        return children.values();
    }

    public boolean hasParameters() {
        return !arguments.isEmpty();
    }

    @NotNull
    public List<Argument<?>> getArguments() {
        return arguments;
    }

    @NotNull
    public <T> Argument<T> addArgument(Argument<T> argument) {

        // If last is optional, make sure this one is too
        if (!arguments.isEmpty() && UtilityMethods.getLast(arguments).isOptional())
            Validate.isTrue(argument.isOptional(), "Cannot add non-optional argument after an optional one");

        // Register and return
        final var copy = argument.withIndex(arguments.size());
        arguments.add(copy);
        return copy;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChild(String id) {
        return children.containsKey(id.toLowerCase());
    }

    @NotNull
    public CommandTreeNode getChild(String id) {
        return children.get(id.toLowerCase());
    }

    public int getLevel() {
        return heightInTree;
    }

    public void addChild(CommandTreeNode child) {
        children.put(child.getId(), child);
    }

    @NotNull
    public CommandResult execute(CommandTreeExplorer explorer, CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }

    @NotNull
    public List<String> calculateTabCompletion(CommandTreeExplorer explorer, int parameterIndex) {

        // Add extra child keys
        final var list = new ArrayList<String>();
        getChildren().forEach(child -> list.add(child.getId()));

        /*
         * If the player is at the end of a command branch, display the
         * parameter with the right index that the player must input
         */
        if (getArguments().size() > parameterIndex) getArguments().get(parameterIndex).autoComplete(explorer, list);

        return list;
    }

    @NotNull
    public List<String> calculateUsageList() {
        return calculateUsageList(getPath(), new ArrayList<>());
    }

    /**
     * Recursive method to calculate current usage list
     *
     * @param path   The current tree path explored
     * @param usages List being completed
     * @return The same list with added elements
     */
    @NotNull
    private List<String> calculateUsageList(String path, List<String> usages) {

        /*
         * Add to list either if there are parameters or if there are no more
         * children
         */
        if (hasParameters() || getChildren().isEmpty()) usages.add(path + " " + formatParameters());

        for (var child : getChildren())
            child.calculateUsageList(path + " " + child.getId(), usages);

        return usages;
    }

    public String formatParameters() {
        var str = new StringBuilder();
        for (Argument<?> param : arguments)
            str.append(param.format()).append(" ");
        return (str.length() == 0) ? str.toString() : str.substring(0, str.length() - 1);
    }

    public enum CommandResult {

        /**
         * Command cast successfully, nothing to do
         */
        SUCCESS,

        /**
         * Command cast unsuccessfully, display message handled via command node
         */
        FAILURE,

        /**
         * Send command usage
         */
        THROW_USAGE
    }
}
