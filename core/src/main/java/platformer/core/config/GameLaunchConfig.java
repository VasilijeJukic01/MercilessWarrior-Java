package platformer.core.config;

import platformer.core.mode.GameMode;

import java.util.Optional;

public record GameLaunchConfig(
        String username,
        String authToken,
        GameMode gameMode,
        Optional<String> sessionIdToJoin,
        boolean cheatsEnabled,
        boolean fullScreen,
        float scaleFactor
) {
}