package me.dannly.sanic;

import org.jetbrains.annotations.NotNull;

public class Activity {

    public static net.dv8tion.jda.api.entities.Activity getActivity() {
        return net.dv8tion.jda.api.entities.Activity.playing("SMITE");
    }

    public static net.dv8tion.jda.api.entities.Activity streaming(@NotNull String name, @NotNull String url) {
        return net.dv8tion.jda.api.entities.Activity.streaming(name, url);
    }

    public static net.dv8tion.jda.api.entities.Activity listening(@NotNull String name) {
        return net.dv8tion.jda.api.entities.Activity.listening(name);
    }
}
