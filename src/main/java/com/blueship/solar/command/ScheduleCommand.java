package com.blueship.solar.command;

import com.blueship.solar.Solar;
import com.blueship.solar.command.arguments.ScheduleArgumentType;
import com.blueship.solar.time.Cycle;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.util.StringUtil;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.checker.units.qual.C;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.BiFunction;

import static com.blueship.solar.command.Pages.createFillerText;
import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@SuppressWarnings("UnstableApiUsage")
public final class ScheduleCommand implements Command {
    private static final SimpleCommandExceptionType NAME_TOO_LONG = new SimpleCommandExceptionType(new LiteralMessage("Schedule name cannot be longer than 16 characters!"));
    private static final DynamicCommandExceptionType INVALID_SCHEDULE_NAME = new DynamicCommandExceptionType(obj -> new LiteralMessage(obj + " is not a valid schedule."));
    private static final DynamicCommandExceptionType INVALID_CYCLE_NAME = new DynamicCommandExceptionType(obj -> new LiteralMessage(obj + " is not a valid cycle."));

    @Override
    public CommandNode<CommandSourceStack> get() {
        return Commands.literal("schedule")
                       .then(Commands.literal("create")
                             .then(Commands.argument("name", StringArgumentType.word())
                                   .executes(ctx -> {
                                       String name = ctx.getArgument("name", String.class);
                                       if (name.length() > 16) {
                                           throw NAME_TOO_LONG.create();
                                       }
                                       Solar.getHandler().createSchedule(name);
                                       ctx.getSource().getSender().sendMessage("Created schedule " + name + ".");
                                       return SINGLE_SUCCESS;
                                   })
                             )
                       )
                       .then(Commands.literal("remove")
                             .then(Commands.argument("schedule", StringArgumentType.word())
                                   .suggests((ctx, builder) -> {
                                       Solar.getHandler().getScheduleNames().forEach(builder::suggest);
                                       return builder.buildFuture();
                                   })
                                   .executes(ctx -> {
                                       String name = ctx.getArgument("schedule", String.class);
                                       if (Solar.getHandler().removeSchedule(name)) {
                                           return SINGLE_SUCCESS;
                                       } else {
                                           throw INVALID_SCHEDULE_NAME.create(name);
                                       }
                                   })
                             )
                       )
                       .then(Commands.literal("modify")
                             .then(Commands.argument("schedule", ScheduleArgumentType.schedule())
                                   .then(Commands.literal("cycle")
                                         .then(Commands.literal("add")
                                               .then(Commands.argument("name", StringArgumentType.word())
                                                     .then(Commands.argument("time", ArgumentTypes.time())
                                                           .executes(ctx -> {
                                                               addCycle(ctx);
                                                               return SINGLE_SUCCESS;
                                                           })
                                                           .then(Commands.argument("days", ArgumentTypes.time())
                                                                 .executes(ctx -> {
                                                                     addCycle(ctx);
                                                                     return SINGLE_SUCCESS;
                                                                 })
                                                                 .then(Commands.argument("index", IntegerArgumentType.integer(0))
                                                                       .executes(ctx -> {
                                                                           addCycle(ctx);
                                                                           return SINGLE_SUCCESS;
                                                                       })
                                                                 )
                                                           )
                                                     )
                                               )
                                         )
                                         .then(Commands.literal("remove")
                                               .then(Commands.argument("cycle", StringArgumentType.word())
                                                     .suggests((ctx, builder) -> {
                                                         var schedule = ctx.getArgument("schedule", Schedule.class);
                                                         schedule.cycles().stream().map(Cycle::name).forEachOrdered(builder::suggest);
                                                         return builder.buildFuture();
                                                     })
                                                     .executes(ctx -> {
                                                        var cycleName = ctx.getArgument("cycle", String.class);
                                                        var schedule = ctx.getArgument("schedule", Schedule.class);
                                                        if (schedule.removeCycle(cycleName)) {
                                                           ctx.getSource().getSender().sendMessage(Component.text("Successfully removed " + cycleName + " from " + schedule.name() + "."));
                                                           return SINGLE_SUCCESS;
                                                       } else {
                                                           throw INVALID_CYCLE_NAME.create(cycleName);
                                                       }
                                                    })
                                               )
                                         )
                                   )
                             )
                       )
                       .then(Commands.literal("list")
                                     .executes(ctx -> {
                                         ctx.getSource().getSender().sendMessage(createScheduleListPage(1, (sched, textComp) -> textComp));
                                         return SINGLE_SUCCESS;
                                     })
                                     .then(Commands.argument("pages", IntegerArgumentType.integer())
                                                   .executes(ctx -> {
                                                     ctx.getSource().getSender().sendMessage(createScheduleListPage(IntegerArgumentType.getInteger(ctx, "pages"), (sched, textComp) -> textComp));
                                                     return SINGLE_SUCCESS;
                                                   })
                                     )
                       )
                       .build();
    }

    private static void addCycle(@NotNull CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        final var schedule = context.getArgument("schedule", Schedule.class);
        String name;
        int time;
        int days;
        int index;
        name = context.getArgument("name", String.class);
        time = context.getArgument("time", int.class);
        try {
            days = context.getArgument("days", int.class);
        } catch (IllegalArgumentException e) {
            days = 1;
        }
        try {
            index = context.getArgument("index", int.class);
        } catch (IllegalArgumentException e) {
            index = -1;
        }

        if (index == -1) {
            schedule.addCycle(new Cycle(name, time, days));
        } else {
            final int scheduleSize = schedule.cycles().size();

            if (index > scheduleSize) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().create(index, scheduleSize);
            }

            if (index < -1) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooLow().create(index, -1);

            }

            schedule.addCycle(new Cycle(name, time, days), index);
        }
        context.getSource().getSender().sendMessage(Component.text("Successfully added " + name + " to " + schedule.name() + "."));
    }

    public static final int SCHEDULES_PER_PAGE = 9;
    public static final int SCHEDULE_NAME_LENGTH = 50;
    public static final @NotNull Component TOP_TEXT_COMPONENT = Component.text("/")
                                                                          .append(createFillerText(" Schedules ", SCHEDULE_NAME_LENGTH))
                                                                          .append(Component.text("\\"))
                                                                          .appendNewline();

    private static @NotNull TextComponent createScheduleHoverText(@NotNull Schedule schedule) {
        var builder = Component.text();
        builder.append(Component.text("Name: ", NamedTextColor.AQUA)).append(Component.text(schedule.name())).appendNewline();
        builder.append(Component.text("Cycles: ", NamedTextColor.AQUA));
        var cycles = schedule.cycles();
        if (cycles.isEmpty()) {
            builder.append(Component.text("NONE"));
        } else {
            builder.appendNewline();
            for (var cycleIter = cycles.iterator(); cycleIter.hasNext();) {
                var cycle = cycleIter.next();
                builder.append(Component.text(" - ")).append(Component.text(cycle.name())).append(Component.text(" : ")).append(Component.text(cycle.cycleTime())).append(Component.text("t, ").append(Component.text(cycle.days() + "d")));
                if (cycleIter.hasNext()) {
                    builder.appendNewline();
                }
            }
        }
        return builder.build();
    }

    static @NotNull Component createScheduleListPage(int currentPage, @NotNull BiFunction<Schedule, TextComponent, TextComponent> componentConsumer) {
        var schedules = Solar.getHandler().getSchedules();
        return createScheduleListPage(Pages.createPageMap(schedules, SCHEDULES_PER_PAGE), currentPage, componentConsumer);
    }

    static @NotNull Component createScheduleListPage(@NotNull Int2ObjectMap<List<Schedule>> pages, int currentPage, @NotNull BiFunction<Schedule, TextComponent, TextComponent> componentConsumer) {
        return Pages.createPage(
                pages, currentPage, SCHEDULES_PER_PAGE, TOP_TEXT_COMPONENT, schedule -> List.of(
                        componentConsumer.apply(schedule, Component.text(StringUtil.centerJustify(schedule.name(), SCHEDULE_NAME_LENGTH)).hoverEvent(HoverEvent.showText(createScheduleHoverText(schedule))))
                ),
                true
        );
    }
}
