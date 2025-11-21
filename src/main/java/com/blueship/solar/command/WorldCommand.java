package com.blueship.solar.command;

import com.blueship.solar.Solar;
import com.blueship.solar.WorldTime;
import com.blueship.solar.command.arguments.ScheduleArgumentType;
import com.blueship.solar.command.arguments.WorldTimeArgumentType;
import com.blueship.solar.time.Schedule;
import com.blueship.solar.util.AudienceUtil;
import com.blueship.solar.util.StringUtil;
import com.blueship.solar.util.TimeUtil;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public final class WorldCommand implements Command {
    private static final int WORLDS_PER_PAGE = 9;
    private static final SimpleCommandExceptionType LOCATION_REQUIREMENT = new SimpleCommandExceptionType(new LiteralMessage("Command must have a location!"));

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public CommandNode<CommandSourceStack> get() {
        return Commands.literal("world")
                       .then(Commands.literal("list")
                             .executes(ctx -> {
                                 ctx.getSource().getSender().sendMessage(createWorldPage(1));
                                 return SINGLE_SUCCESS;
                             })
                             .then(Commands.argument("pages", IntegerArgumentType.integer())
                                   .executes(ctx -> {
                                       ctx.getSource().getSender().sendMessage(createWorldPage(IntegerArgumentType.getInteger(ctx, "pages")));
                                       return SINGLE_SUCCESS;
                                   })
                             )
                       )
                       .then(Commands.literal("toggle-active")
                             .executes(ctx -> {
                                 var location = ctx.getSource().getLocation();
                                 var worldTime = Solar.getHandler()
                                                      .getWorld(location.getWorld().getName())
                                                      .orElseThrow(() -> WorldTimeArgumentType.INVALID_WORLD.create(location.getWorld().getName()));
                                 worldTime.setTicking(!worldTime.isTicking());
                                 ctx.getSource().getSender().sendMessage(Component.text(worldTime.getWorld().getName() + " is now ",
                                                                                        Style.style(NamedTextColor.YELLOW)).append(worldTime.isTicking() ? Component.text("ACTIVE", NamedTextColor.GREEN) : Component.text("INACTIVE", NamedTextColor.RED)).append(Component.text(".")));
                                 return SINGLE_SUCCESS;
                             })
                             .then(Commands.argument("World", WorldTimeArgumentType.worldTime())
                                   .executes(ctx -> {
                                       var worldTime = ctx.getArgument("World", WorldTime.class);
                                       worldTime.setTicking(!worldTime.isTicking());
                                       ctx.getSource().getSender().sendMessage(Component.text(worldTime.getWorld().getName() + " is now ",
                                                                                              Style.style(NamedTextColor.YELLOW)).append(worldTime.isTicking() ? Component.text("ACTIVE", NamedTextColor.GREEN) : Component.text("INACTIVE", NamedTextColor.RED)).append(Component.text(".")));
                                       return SINGLE_SUCCESS;
                                   })
                             )
                       )
                       .then(Commands.argument("World", WorldTimeArgumentType.worldTime())
                             .executes(ctx -> {
                                 ctx.getSource().getSender().sendMessage(createWorldTimeDescription(ctx.getArgument("World", WorldTime.class)));
                                 return SINGLE_SUCCESS;
                             })
                             .then(Commands.literal("set")
                                   .then(Commands.literal("schedule")
                                         .then(Commands.argument("Schedule", ScheduleArgumentType.schedule())
                                               .executes(ctx -> {
                                                   var world = ctx.getArgument("World", WorldTime.class);
                                                   var schedule = ctx.getArgument("Schedule", Schedule.class);
                                                   world.setSchedule(schedule);
                                                   ctx.getSource().getSender().sendMessage("Set the schedule of world " + world.getWorld().getName() + " to " + schedule.name() + ".");
                                                   return SINGLE_SUCCESS;
                                               })
                                         )
                                   )
                                   .then(Commands.literal("ticking")
                                         .then(Commands.argument("true/false", BoolArgumentType.bool())
                                               .executes(ctx -> {
                                                   var worldTime = ctx.getArgument("World", WorldTime.class);
                                                   boolean isTicking = ctx.getArgument("true/false", boolean.class);
                                                   worldTime.setTicking(isTicking);
                                                   ctx.getSource().getSender().sendMessage(Component.text(worldTime.getWorld().getName() + " is now ", Style.style(NamedTextColor.YELLOW)).append(worldTime.isTicking() ? Component.text("ACTIVE", NamedTextColor.GREEN) : Component.text("INACTIVE", NamedTextColor.RED)).append(Component.text(".")));
                                                   return SINGLE_SUCCESS;
                                               })
                                         )
                                   )
                             )
                             .then(Commands.literal("time")
                                   .executes(ctx -> {
                                       var world = ctx.getArgument("World", WorldTime.class);
                                       ctx.getSource().getSender().sendMessage(Component.text(
                                               "The current time for " + world.getWorld().getName() + " is " + getWorldTime(world) + "."
                                       ));
                                       return SINGLE_SUCCESS;
                                   })
                             )
                             .then(Commands.literal("cycle")
                                   .executes(ctx -> {
                                       var worldTime = ctx.getArgument("World", WorldTime.class);
                                       var cycle = worldTime.getCurrentCycle();
                                       ctx.getSource().getSender().sendMessage(Component.text(
                                               "Current Cycle for " + worldTime.getWorld().getName() + "\n" + cycle +
                                                       "Progress: " + ((String.format(("%." + 0 + "f%%"), worldTime.getCycleProgress() * 100))) + "\n"
                                           )
                                       );
                                       return SINGLE_SUCCESS;
                                   })
                             )
                       )
                       .build();
    }


    private static @NotNull String getWorldTime(@NotNull WorldTime world) {
        boolean displayUsingTicks = Solar.getHandler().getConfig().getBoolean("displayTimeAsTicks", false);
        if (displayUsingTicks) {
            return String.valueOf(world.getTime());
        } else {
            return TimeUtil.getTimeInHHMM(world.getTime());
        }
    }

    private static final @NotNull TextComponent ACTIVE_TIME_COMPONENT = Component.text("  (")
                                                                                 .append(Component.text("ACTIVE", NamedTextColor.GREEN))
                                                                                 .append(Component.text(").  "));
    private static final @NotNull TextComponent INACTIVE_TIME_COMPONENT = Component.text(" (")
                                                                                   .append(Component.text("INACTIVE", NamedTextColor.RED))
                                                                                   .append(Component.text(") "));
    private static final int CYCLES_STATUS_LENGTH = 64;
    private static final int WORLD_NAME_LENGTH = 80;
    private static final int SCHEDULE_NAME_LENGTH = 58;
    private static final int PAGE_WIDTH = CYCLES_STATUS_LENGTH + WORLD_NAME_LENGTH + SCHEDULE_NAME_LENGTH;
    private static final @NotNull Component TOP_TEXT_COMPONENT = Component.text("/")
                                                                          // Cycles
                                                                          .append(Pages.createFillerText(" Cycles ", CYCLES_STATUS_LENGTH))
                                                                          // Worlds
                                                                          .append(Pages.createFillerText(" Worlds ", WORLD_NAME_LENGTH))
                                                                          // Schedule
                                                                          .append(Pages.createFillerText(" Schedules ", SCHEDULE_NAME_LENGTH))
                                                                          .append(Component.text("\\"))
                                                                          .appendNewline();

    private static @NotNull Component createWorldPage(int currentPage) {
        var pages = Pages.createPageMap(Solar.getHandler().getWorlds(), WORLDS_PER_PAGE);
        return createWorldPage(pages, currentPage);
    }

    private static @NotNull Component createWorldPage(@NotNull Int2ObjectMap<@NotNull List<WorldTime>> pages, int currentPage) {
        return Pages.createPage(
                pages, currentPage, PAGE_WIDTH, TOP_TEXT_COMPONENT,
                worldTime -> {
                    String worldName = worldTime.getWorld().getName();
                    Schedule schedule = worldTime.getSchedule();
                    Component cycleComponent;
                    if (worldTime.isTicking()) {
                        cycleComponent = ACTIVE_TIME_COMPONENT.hoverEvent(HoverEvent.showText(Component.text("Toggle ").append(Component.text(
                                worldName)).append(INACTIVE_TIME_COMPONENT))).clickEvent(ClickEvent.callback(audience -> {
                            worldTime.setTicking(false);
                            audience.sendMessage(createWorldPage(pages, currentPage));
                        }));
                    } else {
                        cycleComponent = INACTIVE_TIME_COMPONENT.hoverEvent(HoverEvent.showText(Component.text("Toggle ").append(Component.text(
                                worldName)).append(ACTIVE_TIME_COMPONENT))).clickEvent(ClickEvent.callback(audience -> {
                            worldTime.setTicking(true);
                            audience.sendMessage(createWorldPage(pages, currentPage));
                        }));
                    }
                    final TextComponent worldComponent = Component.text(StringUtil.centerJustify(
                            worldName,
                            WORLD_NAME_LENGTH
                    )).hoverEvent(HoverEvent.showText(createWorldTimeDescription(worldTime)));
                    final TextComponent scheduleComponent = Component.text(StringUtil.centerJustify(
                            schedule.name(),
                            SCHEDULE_NAME_LENGTH
                    )).hoverEvent(HoverEvent.showText(Component.text("Change " + worldName + "'s Schedule"))).clickEvent(ClickEvent.callback(audience -> {
                        audience.sendMessage(ScheduleCommand.createScheduleListPage(
                                currentPage,
                                (sched, textComp) -> textComp.clickEvent(ClickEvent.callback(audience2 -> {
                                    Solar.getHandler().getLogger().info("{} set {}'s schedule to {}", AudienceUtil.getName(audience2), worldName, sched.name());
                                    worldTime.setSchedule(sched);
                                    audience2.sendMessage(createWorldPage(pages, currentPage));
                                }))
                        ));
                    }));

                    return List.of(cycleComponent, worldComponent, scheduleComponent);
                },
                false
        );
    }

    private static @NotNull Component createWorldTimeDescription(@NotNull WorldTime worldTime) {
        return Component.text("Name: " + worldTime.getWorld().getName()).appendNewline()
       .append(Component.text("Day: " + TimeUtil.toDays(worldTime.getTime())).appendNewline())
       .append(Component.text("Time: " + getWorldTime(worldTime)).appendNewline())
       .append(Component.text(worldTime.getCurrentCycle().toString())).appendNewline()
       .append(Component.text("Progress: " + ((String.format(("%." + 0 + "f%%"), worldTime.getCycleProgress() * 100)))));
    }
}
