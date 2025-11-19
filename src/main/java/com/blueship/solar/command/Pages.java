package com.blueship.solar.command;

import com.blueship.solar.util.StringUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.map.MinecraftFont;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public final class Pages {
    public static final @NotNull String PREV_PAGE = " <<< ";
    public static final @NotNull String NEXT_PAGE = " >>> ";
    public static final @NotNull String BAR_FILLER = "-";
    public static final @NotNull String SEPERATOR = "||";
    public static final @NotNull TextComponent SEPERATOR_COMPONENT = Component.text(SEPERATOR);
    public static final @NotNull TextComponent SIDE_SEPERATOR_COMPONENT = Component.text("|");
    public static final int SEPERATOR_LENGTH = SEPERATOR.length();

    public static @NotNull Component createFillerText(@NotNull String text, int length) {
        return Component.text(StringUtil.centerJustify(text, length, BAR_FILLER));
    }

    public static <T> @NotNull Component createPage(@NotNull Collection<T> entries, @Positive int currentPage, @Positive int pageLength,
                                                    @NotNull Component topText, @NotNull Function<T, @NotNull List<Component>> rowTextCreator) {
        return createPage(createPageMap(entries, pageLength), currentPage, pageLength, topText, rowTextCreator);
    }

    public static <T> @NotNull Component createPage(@NotNull Collection<T> entries, @Positive int currentPage, @Positive int pageLength,
                                                    @NotNull Component topText, @NotNull Function<T, @NotNull List<Component>> rowTextCreator,
                                                    @NotNull Function<Component, Component> prevPageEffect,
                                                    @NotNull Function<Component, Component> nextPageEffect) {
        return createPage(createPageMap(entries, pageLength), currentPage, pageLength, topText, rowTextCreator, prevPageEffect, nextPageEffect);
    }

    public static <T> @NotNull Component createPage(@NotNull Int2ObjectMap<List<T>> pages, @Positive int currentPage, @Positive int pageLength,
                                                    @NotNull Component topText, @NotNull Function<T, @NotNull List<Component>> rowTextCreator) {
        return createPage(
                pages,
                currentPage,
                pageLength,
                topText,
                rowTextCreator,
                prevPage -> prevPage.clickEvent(ClickEvent.callback(audience -> audience.sendMessage(createPage(
                        pages,
                        currentPage - 1,
                        pageLength,
                        topText,
                        rowTextCreator
                )))),
                nextPage -> nextPage.clickEvent(ClickEvent.callback(audience -> audience.sendMessage(createPage(
                        pages,
                        currentPage + 1,
                        pageLength,
                        topText,
                        rowTextCreator
                ))))
        );
    }

    public static <T> @NotNull Component createPage(@NotNull Int2ObjectMap<List<T>> pages, @Positive int currentPage, @Positive int pageLength,
                                                    @NotNull Component topText, @NotNull Function<T, @NotNull List<Component>> rowText,
                                                    @NotNull Function<Component, Component> prevPageEffect,
                                                    @NotNull Function<Component, Component> nextPageEffect) {
        var middleComponent = Component.empty().toBuilder();
        for (var entry : pages.get(currentPage)) {
            var rowComponent = SIDE_SEPERATOR_COMPONENT.toBuilder();
            var columns = rowText.apply(entry);
            for (var rowPartIter = columns.iterator(); rowPartIter.hasNext(); ) {
                var component = rowPartIter.next();
                rowComponent.append(component);
                if (rowPartIter.hasNext()) {
                    rowComponent.append(SEPERATOR_COMPONENT);
                }
            }
            middleComponent.append(rowComponent).append(SIDE_SEPERATOR_COMPONENT).appendNewline();
        }

        return topText.append(middleComponent).append(Pages.createPageBottomText(pages, currentPage, pageLength, prevPageEffect, nextPageEffect));
    }

    public static <T> @NotNull Int2ObjectMap<List<T>> createPageMap(@NotNull Collection<T> source, @Positive int entriesPerPage) {
        Int2ObjectMap<List<T>> objectMap = new Int2ObjectOpenHashMap<>();
        int index = 0;
        int currentPage = 1;
        List<T> objectList = new ArrayList<>();
        for (T obj : source) {
            objectList.add(obj);
            if (++index >= entriesPerPage) {
                objectMap.put(currentPage, objectList);
                objectList = new ArrayList<>();
                index = 0;
            }
        }
        if (!objectList.isEmpty() && objectMap.containsKey(currentPage)) {
            objectMap.put(currentPage + 1, objectList);
        } else {
            objectMap.putIfAbsent(currentPage, objectList);
        }
        return objectMap;
    }

    private static <T> @NotNull Component createPageBottomText(@NotNull Int2ObjectMap<@NotNull List<T>> pages, @Positive int currentPage,
                                                               @Positive int pageLength, @NotNull Function<Component, Component> prevPageEffect,
                                                               @NotNull Function<Component, Component> nextPageEffect) {
        final @NotNull String currentPageString = " " + currentPage + " / " + pages.size() + " ";
        @NotNull Component prevPageComponent = Component.text(PREV_PAGE);
        if (currentPage != 1) {
            prevPageComponent = prevPageEffect.apply(prevPageComponent);
        }

        @NotNull Component nextPageComponent = Component.text(NEXT_PAGE);
        if (currentPage != pages.size()) {
            nextPageComponent = nextPageEffect.apply(nextPageComponent);
        }
        final int fillerToMake = Math.max(0, pageLength - (currentPageString.length() + PREV_PAGE.length() + NEXT_PAGE.length()));
        return Component.text(BAR_FILLER.repeat(Math.floorDiv(fillerToMake, 2))).toBuilder()
                        .append(prevPageComponent)
                        .append(Component.text(currentPageString))
                        .append(nextPageComponent)
                        .append(Component.text(BAR_FILLER.repeat(Math.ceilDiv(fillerToMake, 2))))
                        .build();
    }
}
