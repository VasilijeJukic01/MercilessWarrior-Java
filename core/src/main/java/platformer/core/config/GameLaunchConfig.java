package platformer.core.config;

public record GameLaunchConfig(
        String username,
        String authToken,
        boolean cheatsEnabled,
        boolean fullScreen,
        float scaleFactor
) {
}