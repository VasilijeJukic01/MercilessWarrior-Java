package platformer.bridge.requests

data class AuthenticationRequest(
    val username: String,
    val password: String
)